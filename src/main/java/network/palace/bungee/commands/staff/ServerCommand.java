package network.palace.bungee.commands.staff;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.Server;
import network.palace.bungee.messages.packets.CreateServerPacket;
import network.palace.bungee.messages.packets.DeleteServerPacket;

import java.util.*;

public class ServerCommand extends PalaceCommand {

    public ServerCommand() {
        super("server", Rank.TRAINEE);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.GREEN + "You are currently on " + player.getServerName());
            StringBuilder msg = new StringBuilder("The following servers exist: ");
            Collection<ServerInfo> servers = ProxyServer.getInstance().getServers().values();
            List<String> names = new ArrayList<>();
            for (ServerInfo s : servers) {
                names.add(s.getName());
            }
            Collections.sort(names);
            for (int i = 0; i < names.size(); i++) {
                msg.append(names.get(i));
                if (i < (names.size() - 1)) {
                    msg.append(", ");
                }
            }
            player.sendMessage(ChatColor.GREEN + msg.toString());
            player.sendMessage(ChatColor.GREEN + "Connect to another server with " + ChatColor.YELLOW + "/server [Name]");
            return;
        }
        if (player.getRank().getRankId() < Rank.DEVELOPER.getRankId()) {
            ServerInfo info = PalaceBungee.getServerUtil().getServerInfo(args[0], true);
            if (info == null) {
                player.sendMessage(ChatColor.RED + "Server not found!");
            } else {
                player.sendMessage(ChatColor.GREEN + "Sending you to " + ChatColor.YELLOW + info.getName() + "...");
                player.getProxiedPlayer().connect(info);
            }
            return;
        }
        switch (args[0].toLowerCase()) {
            case "list": {
                StringBuilder msg = new StringBuilder(ChatColor.GREEN + "Server List:\n").append("- ").append(ChatColor.YELLOW).append("[Name]")
                        .append(ChatColor.GREEN).append(" - ").append(ChatColor.YELLOW).append("[IP:Port]")
                        .append(ChatColor.GREEN).append(" - ").append(ChatColor.YELLOW).append("[Type]")
                        .append(ChatColor.GREEN).append(" - ").append(ChatColor.YELLOW).append("[Players]\n");
                List<Server> servers = new ArrayList<>(PalaceBungee.getMongoHandler().getServers(PalaceBungee.isTestNetwork()));
                HashMap<String, Integer> playerCounts = PalaceBungee.getMongoHandler().getServerCounts();
                servers.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
                for (int i = 0; i < servers.size(); i++) {
                    Server s = servers.get(i);
                    ChatColor c = s.isOnline() ? ChatColor.GREEN : ChatColor.RED;
                    msg.append("- ").append(c).append(s.getName())
                            .append(ChatColor.GREEN).append(" - ").append(s.getAddress())
                            .append(" - ").append(s.getServerType())
                            .append(" - ").append(playerCounts.getOrDefault(s.getName(), 0));
                    if (i < (servers.size() - 1)) {
                        msg.append("\n");
                    }
                }
                player.sendMessage(msg.toString());
                break;
            }
            case "add": {
                if (args.length != 5) {
                    player.sendMessage(ChatColor.RED + "/server add [Name] [IP Address:Port] [True/False] [Type]");
                    break;
                }
                try {
                    Server s = new Server(args[1], args[2], Boolean.parseBoolean(args[3]), args[4], false);
                    if (!args[2].contains(":")) throw new IllegalArgumentException("Invalid address format!");
                    PalaceBungee.getMongoHandler().createServer(s);
                    PalaceBungee.getMessageHandler().sendMessage(new CreateServerPacket(s), PalaceBungee.getMessageHandler().ALL_PROXIES);
                    player.sendMessage(ChatColor.GREEN + "Server created successfully! Connect to it with " + ChatColor.YELLOW + "/server " + s.getName());
                } catch (Exception e) {
                    e.printStackTrace();
                    player.sendMessage(ChatColor.RED + "There was an error creating that server! Check your command arguments and console for errors.");
                }
                break;
            }
            case "remove": {
                if (args.length != 2) {
                    player.sendMessage(ChatColor.RED + "/server remove [Name]");
                    break;
                }
                try {
                    Server s = PalaceBungee.getServerUtil().getServer(args[1], true);
                    if (s == null) {
                        player.sendMessage(ChatColor.RED + "Server not found!");
                        return;
                    }
                    PalaceBungee.getMongoHandler().deleteServer(s.getName());
                    PalaceBungee.getMessageHandler().sendMessage(new DeleteServerPacket(s.getName()), PalaceBungee.getMessageHandler().ALL_PROXIES);
                    player.sendMessage(ChatColor.RED + "Server removed successfully!");
                    //TODO Send all players on deleted server to another server
                } catch (Exception e) {
                    e.printStackTrace();
                    player.sendMessage(ChatColor.RED + "There was an error deleting that server! Check your command arguments and console for errors.");
                }
                break;
            }
            case "help": {
                player.sendMessage(ChatColor.GREEN + "Server Commands:");
                player.sendMessage(ChatColor.GREEN + "/server list " + ChatColor.AQUA +
                        "- List all servers and addresses");
                player.sendMessage(ChatColor.GREEN + "/server add [Name] [IP Address:Port] [True/False] [Type] " +
                        ChatColor.AQUA + "- Add a new server to all Bungees");
                player.sendMessage(ChatColor.GREEN + "/server remove [Name] " + ChatColor.AQUA +
                        "- Remove a server from all Bungees");
//                player.sendMessage(ChatColor.GREEN + "/server mute [Name] " + ChatColor.AQUA +
//                        "- Don't display server start/stop (re-enables when you log out)");
                break;
            }
            default:
                ServerInfo info = PalaceBungee.getServerUtil().getServerInfo(args[0], true);
                if (info == null) {
                    player.sendMessage(ChatColor.RED + "Server not found!");
                } else {
                    player.sendMessage(ChatColor.GREEN + "Sending you to " + ChatColor.YELLOW + info.getName() + "...");
                    player.getProxiedPlayer().connect(info);
                }
                break;
        }
    }
//        Dashboard dashboard = Launcher.getDashboard();
//        if (args.length == 5 && args[0].equalsIgnoreCase("add") && player.getRank().getRankId() >= Rank.DEVELOPER.getRankId()) {
//            final String name = args[1];
//            final String address = args[2];
//            final boolean park;
//            final String type = args[4];
//            try {
//                park = Boolean.parseBoolean(args[3]);
//            } catch (Exception ignored) {
//                player.sendMessage(ChatColor.RED + "Please use true or false to state if it is a Park server or not!");
//                return;
//            }
//            if (dashboard.getServerUtil().getServer(name) != null) {
//                player.sendMessage(ChatColor.RED + "A server already exists called '" + name + "'!");
//                return;
//            }
//            Server s = new Server(name, address, park, 0, type);
//            dashboard.getServerUtil().addServer(s);
//            dashboard.getSchedulerManager().runAsync(() -> {
//                try {
//                    dashboard.getMongoHandler().addServer(s);
//                    player.sendMessage(ChatColor.GREEN + "'" + name + "' successfully created! Notifying Bungees...");
//                    PacketAddServer packet = new PacketAddServer(name, address);
//                    for (Object o : WebSocketServerHandler.getGroup()) {
//                        DashboardSocketChannel dash = (DashboardSocketChannel) o;
//                        if (!dash.getType().equals(PacketConnectionType.ConnectionType.BUNGEECORD)) {
//                            continue;
//                        }
//                        dash.send(packet);
//                    }
//                    player.sendMessage(ChatColor.GREEN + "All Bungees notified! Server '" + name + "' can now be joined.");
//                } catch (Exception e) {
//                    Launcher.getDashboard().getLogger().error("Error registering server", e);
//                }
//            });
//            return;
//        }
//        if (args.length == 2 && args[0].equalsIgnoreCase("remove") && player.getRank().getRankId() >= Rank.DEVELOPER.getRankId()) {
//            final String name = args[1];
//            final Server s = dashboard.getServerUtil().getServer(name);
//            if (s == null) {
//                player.sendMessage(ChatColor.RED + "No server exists called '" + name + "'!");
//                return;
//            }
//            player.sendMessage(ChatColor.GREEN + "Emptying server " + s.getName() + "...");
//            s.emptyServer();
//            new Timer().schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    if (s.getCount() <= 0) {
//                        player.sendMessage(ChatColor.GREEN + s.getName() + " has been emptied! Removing server...");
//                        cancel();
//                        dashboard.getServerUtil().removeServer(name);
//                        dashboard.getSchedulerManager().runAsync(() -> {
//                            dashboard.getMongoHandler().removeServer(s.getName());
//                            player.sendMessage(ChatColor.GREEN + "'" + name + "' successfully removed! Notifying Bungees...");
//                            PacketRemoveServer packet = new PacketRemoveServer(name);
//                            for (Object o : WebSocketServerHandler.getGroup()) {
//                                DashboardSocketChannel dash = (DashboardSocketChannel) o;
//                                if (!dash.getType().equals(PacketConnectionType.ConnectionType.BUNGEECORD)) {
//                                    continue;
//                                }
//                                dash.send(packet);
//                            }
//                            player.sendMessage(ChatColor.GREEN + "All Bungees notified! Server '" + name + "' has been removed.");
//                        });
//                    }
//                }
//            }, 1000);
//            return;
//        } else if (args.length == 2 && args[0].equalsIgnoreCase("mute") && player.getRank().getRankId() >= Rank.DEVELOPER.getRankId()) {
//            final String serverName = args[1];
//            if (dashboard.getServerUtil().isMuted(serverName)) {
//                dashboard.getServerUtil().unmuteServer(serverName);
//                return;
//            }
//            dashboard.getServerUtil().muteServer(player.getUniqueId(), serverName);
//        }
//        if (args.length == 1) {
//            if (player.getRank().getRankId() >= Rank.DEVELOPER.getRankId()) {
//                if (args[0].equalsIgnoreCase("help")) {
//                    player.sendMessage(ChatColor.GREEN + "Server Commands:");
//                    player.sendMessage(ChatColor.GREEN + "/server list " + ChatColor.AQUA +
//                            "- List all servers and addresses");
//                    player.sendMessage(ChatColor.GREEN + "/server add [Name] [IP Address:Port] [True/False] [Type] " +
//                            ChatColor.AQUA + "- Add a new server to all Bungees");
//                    player.sendMessage(ChatColor.GREEN + "/server remove [Name] " + ChatColor.AQUA +
//                            "- Remove a server from all Bungees");
//                    player.sendMessage(ChatColor.GREEN + "/server mute [Name] " + ChatColor.AQUA +
//                            "- Don't display server start/stop (re-enables when you log out)");
//                    return;
//                } else if (args[0].equalsIgnoreCase("list")) {
//                    StringBuilder msg = new StringBuilder(ChatColor.GREEN + "Server List:\n");
//                    List<Server> servers = new ArrayList<>(dashboard.getServers());
//                    servers.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
//                    for (int i = 0; i < servers.size(); i++) {
//                        Server s = servers.get(i);
//                        ChatColor c = s.isOnline() ? ChatColor.GREEN : ChatColor.RED;
//                        msg.append("- ").append(c).append(s.getName()).append(ChatColor.GREEN).append(" - ")
//                                .append(s.getAddress()).append(" - ").append(s.getServerType());
//                        if (i < (servers.size() - 1)) {
//                            msg.append("\n");
//                        }
//                    }
//                    player.sendMessage(msg.toString());
//                    return;
//                }
//            }
//            Server server = dashboard.getServerUtil().getServer(args[0]);
//            if (server == null) {
//                player.sendMessage(ChatColor.RED + "That server doesn't exist!");
//                return;
//            }
//            dashboard.getServerUtil().sendPlayer(player, server);
//            return;
//        }
//        if (args.length == 0) {
//            player.sendMessage(ChatColor.GREEN + "You are currently on " + player.getServer());
//            StringBuilder msg = new StringBuilder("The following servers exist: ");
//            List<Server> servers = dashboard.getServers();
//            List<String> names = new ArrayList<>();
//            for (Server s : servers) {
//                names.add(s.getName());
//            }
//            Collections.sort(names);
//            for (int i = 0; i < names.size(); i++) {
//                msg.append(names.get(i));
//                if (i < (names.size() - 1)) {
//                    msg.append(", ");
//                }
//            }
//            player.sendMessage(ChatColor.GREEN + msg.toString());
//        }
//    }
//
//    @Override
//    public Iterable<String> onTabComplete(Player sender, List<String> args) {
//        Dashboard dashboard = Launcher.getDashboard();
//        List<String> list = new ArrayList<>();
//        for (Server server : dashboard.getServers()) {
//            list.add(server.getName());
//        }
//        Collections.sort(list);
//        if (args.size() == 0) {
//            return list;
//        }
//        List<String> l2 = new ArrayList<>();
//        String arg = args.get(args.size() - 1);
//        for (String s : list) {
//            if (s.toLowerCase().startsWith(arg.toLowerCase())) {
//                l2.add(s);
//            }
//        }
//        Collections.sort(l2);
//        return l2;
//    }
//
//    @Override
//    public boolean doesTabComplete() {
//        return true;
//    }
}