package network.palace.bungee.commands.chat;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.RankTag;
import network.palace.bungee.messages.packets.MessageByRankPacket;
import network.palace.bungee.utils.EmojiUtil;

public class GuideChatCommand extends PalaceCommand {

    public GuideChatCommand() {
        super("gc", Rank.TRAINEE, RankTag.GUIDE);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/gc [Message]");
            return;
        }
        String msg = String.join(" ", args);

        String processed = PalaceBungee.getChatUtil().processChatMessage(player, msg, "GC", false);
        if (processed == null) return;
        try {
            PalaceBungee.getChatUtil().analyzeMessage(player.getUniqueId(), player.getRank(), processed, player.getServerName(), () -> {
                try {
                    String message;
                    try {
                        message = EmojiUtil.convertMessage(player, processed);
                    } catch (IllegalArgumentException e) {
                        player.sendMessage(ChatColor.RED + e.getMessage());
                        return;
                    }
                    MessageByRankPacket packet = new MessageByRankPacket("[" + ChatColor.DARK_GREEN + "GUIDE" +
                            ChatColor.WHITE + "] " + RankTag.format(player.getTags()) + player.getRank().getFormattedName() +
                            " " + ChatColor.GRAY + player.getUsername() + ": " + ChatColor.DARK_AQUA + message,
                            Rank.TRAINEE, RankTag.GUIDE, false, false);
                    PalaceBungee.getMessageHandler().sendMessage(packet, PalaceBungee.getMessageHandler().ALL_PROXIES);
                } catch (Exception e) {
                    e.printStackTrace();
                    player.sendMessage(ChatColor.RED + "There was an error executing this command!");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "There was an error executing this command!");
        }
    }
}
