package network.palace.bungee.commands.staff;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.messages.packets.BroadcastPacket;

public class BroadcastCommand extends PalaceCommand {

    public BroadcastCommand() {
        super("broadcast", Rank.MOD, "bc");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "/bc [Message]");
            return;
        }
        try {
            StringBuilder message = new StringBuilder();
            for (String arg : args) {
                message.append(arg).append(" ");
            }
            String msg = ChatColor.translateAlternateColorCodes('&', message.substring(0, message.length() - 1));
            BroadcastPacket packet = new BroadcastPacket(player.getUsername(), msg);
            PalaceBungee.getMessageHandler().sendMessage(packet, "all_proxies", "fanout");
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "There was an error executing this command!");
        }
    }
}
