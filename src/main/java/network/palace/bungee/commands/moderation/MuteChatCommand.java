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
            if (PalaceBungee.getServerUtil().getServer(server, true).isPark()) server = "ParkChat";
            boolean muted = PalaceBungee.getChatUtil().isChatMuted(server);
            if (muted) {
                PalaceBungee.getChatUtil().unmuteChat(server, player.getUsername());
            } else {
                PalaceBungee.getChatUtil().muteChat(server, player.getUsername());
            }
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred while muting chat. Check console for errors.");
        }
    }
}