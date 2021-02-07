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
        tabComplete = true;
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
                    List<Server> servers = new ArrayList<>(PalaceBungee.getMongoHandler().getServers(PalaceBungee.isTestNetwork()));
                    Optional<Server> opt = servers.stream().filter(server -> server.getName().equals(args[1])).findFirst();
                    if (opt.isEmpty()) {
                        player.sendMessage(ChatColor.RED + "Server not found!");
                        return;
                    }
                    Server s = opt.get();
                    PalaceBungee.getMongoHandler().deleteServer(s.getName());
                    PalaceBungee.getMessageHandler().sendMessage(new DeleteServerPacket(s.getName()), PalaceBungee.getMessageHandler().ALL_PROXIES);
                    player.sendMessage(ChatColor.RED + "Server removed successfully!");
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

    @Override
    public Iterable<String> onTabComplete(Player sender, List<String> args) {
        List<String> list = new ArrayList<>();
        for (Server server : PalaceBungee.getServerUtil().getServers()) {
            list.add(server.getName());
        }
        Collections.sort(list);
        if (args.size() == 0) {
            return list;
        }
        List<String> l2 = new ArrayList<>();
        String arg = args.get(args.size() - 1);
        for (String s : list) {
            if (s.toLowerCase().startsWith(arg.toLowerCase())) {
                l2.add(s);
            }
        }
        Collections.sort(l2);
        return l2;
    }
}