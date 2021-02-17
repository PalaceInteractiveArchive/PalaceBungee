package network.palace.bungee.commands.chat;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.Server;

public class ChatStatusCommand extends PalaceCommand {

    public ChatStatusCommand() {
        super("chatstatus", Rank.TRAINEE);
    }

    @Override
    public void execute(Player player, String[] args) {
        Server s = PalaceBungee.getServerUtil().getServer(player.getServerName(), true);
        if (s == null) {
            player.sendMessage(ChatColor.RED + "An error occurred while retrieving chat status - unknown server '" + player.getServerName() + "'!");
            return;
        }
        boolean park = s.isPark();
        String name;
        int count = 0;
        boolean muted;
        if (park) {
            int c = 0;
            for (Server sr : PalaceBungee.getServerUtil().getServers()) {
                if (!sr.isPark() || sr.getCount() == 0) continue;
                c++;
                count += sr.getCount();
            }
            name = "ParkChat (" + c + " servers)";
            muted = PalaceBungee.getChatUtil().isChatMuted("ParkChat");
        } else {
            name = s.getName();
            count = s.getCount();
            muted = PalaceBungee.getChatUtil().isChatMuted(s.getName());
        }
        player.sendMessage(ChatColor.GREEN + "Name: " + ChatColor.YELLOW + name + "\n" + ChatColor.GREEN +
                "Players: " + ChatColor.YELLOW + count + "\n" + ChatColor.GREEN + "Status: " +
                (muted ? (ChatColor.RED + "Muted") : (ChatColor.YELLOW + "Unmuted")));
    }
}