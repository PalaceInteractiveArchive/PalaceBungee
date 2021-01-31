package network.palace.bungee.commands.staff;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.Server;

import java.util.ArrayList;
import java.util.List;

public class MultiShowCommand extends PalaceCommand {

    public MultiShowCommand() {
        super("multishow", Rank.MOD);
    }

    @Override
    public void execute(Player player, String[] args) {
        //TODO Doesn't work yet
        try {
            if (args.length == 4) {
                List<String> servers = new ArrayList<>();
                String showName = args[1];
                String server = args[3];
                if (exists(server)) {
                    switch (args[0].toLowerCase()) {
                        case "start":
//                        PacketShowStart startPacket = new PacketShowStart(args[1], args[2]);
//                        for (Object o : WebSocketServerHandler.getGroup()) {
//                            DashboardSocketChannel dash = (DashboardSocketChannel) o;
//                            if (!dash.getType().equals(PacketConnectionType.ConnectionType.INSTANCE)) continue;
//                            if (dash.getServerName().startsWith(server)) {
//                                dash.send(startPacket);
//                                servers.add(dash.getServerName());
//                            }
//                        }
                            PalaceBungee.getMessageHandler().sendStaffMessage(ChatColor.GREEN + "Attempting to start " + ChatColor.AQUA + showName + ChatColor.GREEN + " on " + String.join(ChatColor.GREEN + ", " + ChatColor.AQUA, servers));
                            break;
                        case "stop":
//                        PacketShowStop stopPacket = new PacketShowStop(args[1], args[2]);
//                        for (Object o : WebSocketServerHandler.getGroup()) {
//                            DashboardSocketChannel dash = (DashboardSocketChannel) o;
//                            if (!dash.getType().equals(PacketConnectionType.ConnectionType.INSTANCE)) continue;
//                            if (dash.getServerName().startsWith(server)) {
//                                dash.send(stopPacket);
//                                servers.add(dash.getServerName());
//                            }
//                        }
                            PalaceBungee.getMessageHandler().sendStaffMessage(ChatColor.GREEN + "Attempting to stop " + ChatColor.AQUA + showName + ChatColor.GREEN + " on " + String.join(ChatColor.GREEN + ", " + ChatColor.AQUA, servers));
                            break;
                        default:
                            player.sendMessage(ChatColor.GREEN + "MultiShow Commands:\n" + ChatColor.AQUA + "- /multishow start [show] [server] " +
                                    ChatColor.GREEN + "- Starts the specified show on all instances of the specified server\n" + ChatColor.AQUA + "- /multishow stop [show] [server] " +
                                    ChatColor.GREEN + "- Stops the specified show on all instances of the specified server\n" + ChatColor.AQUA + "- /multishow help " +
                                    ChatColor.GREEN + "- View this help menu");
                            break;
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Please provide a valid server without the server number (WDW not WDW2).");
                }
                return;
            }
            player.sendMessage(ChatColor.GREEN + "MultiShow Commands:\n" + ChatColor.AQUA +
                    "- /multishow start [show] [world] [server] " + ChatColor.GREEN +
                    "- Starts the specified show on all instances of the specified server\n" + ChatColor.AQUA +
                    "- /multishow stop [show] [world] [server] " + ChatColor.GREEN +
                    "- Stops the specified show on all instances of the specified server\n" + ChatColor.AQUA +
                    "- /multishow help " + ChatColor.GREEN + "- View this help menu");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean exists(String s) {
        for (Server server : PalaceBungee.getServerUtil().getServers()) {
            if (server.getName().startsWith(s)) {
                return true;
            }
        }
        return false;
    }
}