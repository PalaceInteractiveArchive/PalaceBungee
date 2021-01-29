package network.palace.bungee.commands.moderation;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;

public class MuteChatCommand extends PalaceCommand {

    public MuteChatCommand() {
        super("mutechat", Rank.TRAINEE, "chatmute");
    }

    @Override
    public void execute(Player player, String[] args) {
        try {
            String server = player.getServerName();
            boolean parkchat = PalaceBungee.getServerUtil().getServer(server, true).isPark();
            if (parkchat) server = "ParkChat";
            boolean muted = PalaceBungee.getChatUtil().isChatMuted(server);
            String msg;
            if (muted) {
                PalaceBungee.getChatUtil().unmuteChat(server);
                msg = ChatColor.WHITE + "[" + ChatColor.DARK_AQUA + "Palace Chat" + ChatColor.WHITE + "] " +
                        ChatColor.YELLOW + "Chat has been unmuted";
            } else {
                PalaceBungee.getChatUtil().muteChat(server);
                msg = ChatColor.WHITE + "[" + ChatColor.DARK_AQUA + "Palace Chat" + ChatColor.WHITE + "] " +
                        ChatColor.YELLOW + "Chat has been muted";
            }
            String msgname = msg + " by " + player.getUsername();
            for (Player tp : PalaceBungee.getOnlinePlayers()) {
                if ((parkchat && PalaceBungee.getServerUtil().getServer(tp.getServerName(), true).isPark()) || tp.getServerName().equals(server)) {
                    tp.sendMessage(tp.getRank().getRankId() >= Rank.TRAINEE.getRankId() ? msgname : msg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred while muting chat. Check console for errors.");
        }
    }
}