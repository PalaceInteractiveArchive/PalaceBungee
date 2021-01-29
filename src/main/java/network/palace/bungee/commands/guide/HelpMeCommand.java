package network.palace.bungee.commands.guide;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;

public class HelpMeCommand extends PalaceCommand {

    public HelpMeCommand() {
        super("helpme");
    }

    @Override
    public void execute(Player player, String[] args) {
//        if (ChatUtil.notEnoughTime(player)) { TODO new guests need to wait 10 minutes
//            player.sendMessage(DashboardConstants.NEW_GUEST);
//            return;
//        }
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
//        if (player.getRank().getRankId() < Rank.TRAINEE.getRankId()) {
        try {
            PalaceBungee.getChatUtil().analyzeMessage(player.getUniqueId(), player.getRank(), request.toString(), player.getServerName(), () -> {
//                    if (PalaceBungee.getChatUtil().strictModeCheck(request.toString())) { TODO strict mode
//                        player.sendMessage(ChatColor.RED + "Your message was similar to another recently said in chat and was marked as spam. We apologize if this was done in error, we're constantly improving our chat filter.");
//                        PalaceBungee.getModerationUtil().announceSpamMessage(player.getUsername(), request.toString());
//                        PalaceBungee.getLogger().info("CANCELLED CHAT EVENT STRICT MODE");
//                        return;
//                    }
                PalaceBungee.getGuideUtil().sendHelpRequest(player, request.toString());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
//        } else {
//            PalaceBungee.getGuideUtil().sendHelpRequest(player, request.toString());
//        }
    }
}