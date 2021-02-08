package network.palace.bungee.commands.guide;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;

public class HelpMeCommand extends PalaceCommand {

    public HelpMeCommand() {
        super("helpme");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (player.getTotalOnlineTime() < 600) {
            player.sendMessage(ChatColor.RED + "New guests must be on the server for at least 10 minutes before talking in chat." +
                    ChatColor.DARK_AQUA + " Learn more at palnet.us/rules");
            return;
        }
        if (args.length < 1) {
            player.sendMessage(ChatColor.AQUA + "To get help, explain what you need help with:");
            player.sendMessage(ChatColor.AQUA + "/helpme [Reason]");
            return;
        }
        player.sendMessage(ChatColor.GREEN + "Processing your help request...");
        if (!PalaceBungee.getGuideUtil().canSubmitHelpRequest(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You need to wait a little bit before sending another help request.");
            return;
        }
        if (PalaceBungee.getGuideUtil().overloaded()) {
            player.sendMessage(ChatColor.AQUA + "We're currently receiving a high volume of help requests. We apologize for the inconvenience.");
            return;
        }
        StringBuilder request = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            request.append(args[i]);
            if (i <= (args.length - 1)) {
                request.append(" ");
            }
        }
        try {
            PalaceBungee.getChatUtil().analyzeMessage(player.getUniqueId(), player.getRank(), request.toString(), "/helpme", () -> {
                try {
                    if (player.getRank().getRankId() < Rank.CHARACTER.getRankId() && PalaceBungee.getChatUtil().strictModeCheck(request.toString())) {
                        player.sendMessage(ChatColor.RED + "Your message was similar to another recently said in chat and was marked as spam. We apologize if this was done in error, we're constantly improving our chat filter.");
                        PalaceBungee.getModerationUtil().announceSpamMessage(player.getUsername(), request.toString());
                        PalaceBungee.getInstance().getLogger().info("CANCELLED CHAT EVENT STRICT MODE");
                        return;
                    }
                    PalaceBungee.getGuideUtil().sendHelpRequest(player, request.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}