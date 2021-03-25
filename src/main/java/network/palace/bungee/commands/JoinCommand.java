package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Server;

import java.util.*;
import java.util.logging.Level;

public class JoinCommand extends PalaceCommand {
    private static final LinkedList<String> servers = new LinkedList<>(Arrays.asList("Hub", "WDW", "USO", "Seasonal", "Creative"));

    public JoinCommand() {
        super("join");
        tabComplete = true;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 1) {
            if (exists(args[0])) {
                Server currentServer;
                if ((currentServer = PalaceBungee.getServerUtil().getServer(player.getServerName(), true)) != null && currentServer.getServerType().equalsIgnoreCase(args[0])) {
                    player.sendMessage(ChatColor.RED + "You are already on this server!");
                    return;
                }
                try {
                    String type = formatName(args[0]);
                    Server server = PalaceBungee.getServerUtil().getServerByType(type);
                    if (server == null) {
                        player.sendMessage(ChatColor.RED + "No '" + type + "' server is available right now! Please try again soon.");
                        return;
                    }
                    server.join(player);
                } catch (Exception e) {
                    PalaceBungee.getProxyServer().getLogger().log(Level.SEVERE, "Error sending player to server", e);
                    player.sendMessage(ChatColor.RED + "There was a problem joining that server!");
                }
                return;
            }
            Server server;
            if (endsInNumber(args[0]) && exists(args[0].substring(0, args[0].length() - 1)) &&
                    (server = PalaceBungee.getServerUtil().getServer(formatName(args[0]), true)) != null) {
                try {
                    PalaceBungee.getServerUtil().sendPlayer(player, server.getName());
                } catch (Exception e) {
                    PalaceBungee.getProxyServer().getLogger().log(Level.SEVERE, "Error sending player to server", e);
                    player.sendMessage(ChatColor.RED + "There was a problem joining that server!");
                }
                return;
            }
        }
        TextComponent top = new TextComponent(ChatColor.GREEN + "Here is a list of servers you can join: " +
                ChatColor.GRAY + "(Click to join)");
        player.sendMessage(top);
        for (String server : servers) {
            if (server.trim().isEmpty()) continue;
            TextComponent txt = new TextComponent(ChatColor.GREEN + "- " + ChatColor.AQUA + server);
            txt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(ChatColor.GREEN + "Click to join the " + ChatColor.AQUA +
                            server + ChatColor.GREEN + " server!").create()));
            txt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/join " + server));
            player.sendMessage(txt);
        }
    }

    private boolean endsInNumber(String s) {
        try {
            Integer.parseInt(s.substring(s.length() - 1));
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private boolean exists(String s) {
        for (String server : servers) {
            if (server.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    private String formatName(String s) {
        StringBuilder ns = new StringBuilder();
        String t = s.replaceAll("\\d", "");
        if (t.length() < 4 && !t.equalsIgnoreCase("hub")) {
            for (char c : s.toCharArray()) {
                ns.append(Character.toUpperCase(c));
            }
            return ns.toString();
        }
        Character last = null;
        for (char c : s.toCharArray()) {
            if (last == null) {
                last = c;
                ns.append(Character.toUpperCase(c));
                continue;
            }
            if (Character.toString(last).equals(" ")) {
                ns.append(Character.toUpperCase(c));
            } else {
                ns.append(c);
            }
            last = c;
        }
        return ns.toString();
    }

    @Override
    public Iterable<String> onTabComplete(Player sender, List<String> args) {
        List<String> list = servers;
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