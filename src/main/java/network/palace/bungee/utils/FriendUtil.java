package network.palace.bungee.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;

import java.util.*;

public class FriendUtil {

    public static void teleportPlayer(Player player, Player target) {
        if (target == null) return;
        if (player.getServerName().equals(target.getServerName())) {
            player.sendMessage(ChatColor.RED + "You're already on the same server as " + ChatColor.AQUA +
                    target.getUsername() + "!");
            return;
        }
        try {
            PalaceBungee.getServerUtil().sendPlayer(player, target.getServerName());
            player.sendMessage(ChatColor.BLUE + "You connected to the server " + ChatColor.GREEN + target.getUsername() +
                    " " + ChatColor.BLUE + "is on! (" + target.getServerName() + ")");
        } catch (Exception ignored) {
        }
    }

    public static void listFriends(final Player player, int page) {
        HashMap<UUID, String> friends = player.getFriends();
        if (friends.isEmpty()) {
            player.sendMessage(ChatColor.RED + "\nType /friend add [Player] to add someone\n");
            return;
        }
        int listSize = friends.size();
        int maxPage = (int) Math.ceil((double) friends.size() / 8);
        if (page > maxPage) {
            page = maxPage;
        }
        int startAmount = 8 * (page - 1);
        int endAmount;
        if (maxPage > 1) {
            if (page < maxPage) {
                endAmount = (8 * page);
            } else {
                endAmount = listSize;
            }
        } else {
            endAmount = listSize;
        }
        List<String> currentFriends = new ArrayList<>();
        for (Map.Entry<UUID, String> entry : friends.entrySet()) {
            Player tp = PalaceBungee.getPlayer(entry.getKey());
            if (tp == null) {
                currentFriends.add(entry.getValue() == null ? "unknown" : entry.getValue());
            } else {
                String sname = PalaceBungee.getServerUtil().getServer(tp.getServerName(), true).getServerType();
                if (sname.startsWith("New")) {
                    sname = sname.replaceAll("New", "");
                }
                currentFriends.add(entry.getValue() + ":" + sname);
            }
        }
        currentFriends.sort((o1, o2) -> {
            boolean c1 = o1.contains(":");
            boolean c2 = o2.contains(":");
            if (c1 && !c2) {
                return -1;
            } else if (!c1 && c2) {
                return 1;
            } else {
                return o1.toLowerCase().compareTo(o2.toLowerCase());
            }
        });
        ComponentBuilder message = new ComponentBuilder("\nFriend List ").color(ChatColor.YELLOW)
                .append("[Page " + page + " of " + maxPage + "]").color(ChatColor.GREEN);
        for (String str : currentFriends.subList(startAmount, endAmount)) {
            String[] list = str.split(":");
            String user = list[0];
            if (list.length > 1) {
                String server = list[1];
                message.append("\n- ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.AQUA).append(user).color(ChatColor.GREEN)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to join ")
                                .color(ChatColor.GREEN).append(server + "!").color(ChatColor.YELLOW)
                                .create())).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/friend tp " + user));
            } else {
                message.append("\n- ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.AQUA).append(user).color(ChatColor.RED)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("This player is offline!")
                                .color(ChatColor.RED).create()));
            }
        }
        player.sendMessage(message.create());
    }

    public static void listRequests(final Player player) {
        HashMap<UUID, String> requests = player.getRequests();
        if (requests.isEmpty()) {
            player.sendMessage(ChatColor.RED + "\nYou currently have no Friend Requests!\n");
            return;
        }
        player.sendMessage(ChatColor.GREEN + "Request List:");
        for (String s : requests.values()) {
            player.sendMessage(new ComponentBuilder("- ").color(ChatColor.AQUA).append(s)
                    .color(ChatColor.YELLOW).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("Click to Accept the Request!").color(ChatColor.GREEN).create()))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend accept " + s)).create());
        }
        player.sendMessage(new ComponentBuilder(" ").create());
    }

    public static void addFriend(Player player, String name) {
        if (name.equalsIgnoreCase(player.getUsername())) {
            player.sendMessage(ChatColor.RED + "You can't be your own friend, sorry!");
            return;
        }
        HashMap<UUID, String> friendList = player.getFriends();
        for (String s : friendList.values()) {
            if (s.equalsIgnoreCase(name)) {
                player.sendMessage(ChatColor.RED + "That player is already on your Friend List!");
                return;
            }
        }
        try {
            UUID tuuid = PalaceBungee.getMongoHandler().usernameToUUID(name);
            HashMap<UUID, String> requests = PalaceBungee.getMongoHandler().getFriendRequestList(tuuid);
            if (requests.containsKey(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You have already sent this player a Friend Request!");
                return;
            }
            if (player.getRank().getRankId() < Rank.TRAINEE.getRankId()) {
                if (!PalaceBungee.getMongoHandler().getFriendRequestToggle(tuuid) || PalaceBungee.getMongoHandler().doesPlayerIgnorePlayer(tuuid, player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "That player is not currently accepting Friend Requests!");
                    return;
                }
            }
            player.sendMessage(ChatColor.YELLOW + "You have sent " + ChatColor.AQUA + name + ChatColor.YELLOW +
                    " a Friend Request!");
            /* Add request to database */
            PalaceBungee.getMongoHandler().addFriendRequest(player.getUniqueId(), tuuid);
            PalaceBungee.getMessageHandler().sendMessageToPlayer(tuuid,
                    new ComponentBuilder("\n" + player.getUsername()).color(ChatColor.GREEN)
                            .append(" has sent you a Friend Request!\n").color(ChatColor.YELLOW)
                            .append("Click to Accept").color(ChatColor.GREEN).bold(true).event(new
                            ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend accept " + player.getUsername())).append(" or ",
                            ComponentBuilder.FormatRetention.NONE).color(ChatColor.AQUA).append("Click to Deny\n")
                            .color(ChatColor.RED).bold(true).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/friend deny " + player.getUsername())).create()
            );
        } catch (Exception ignored) {
            player.sendMessage(ChatColor.RED + "Player not found!");
        }
    }

    public static void removeFriend(Player player, String name) {
        try {
            UUID tuuid = PalaceBungee.getMongoHandler().usernameToUUID(name);
            if (!player.getFriends().containsKey(tuuid)) {
                player.sendMessage(ChatColor.RED + "That player isn't on your Friend List!");
                return;
            }
            player.getFriends().remove(tuuid);
            player.sendMessage(ChatColor.RED + "You removed " + ChatColor.AQUA + name + ChatColor.RED +
                    " from your Friend List!");
            PalaceBungee.getMongoHandler().removeFriend(player.getUniqueId(), tuuid);
        } catch (Exception ignored) {
            player.sendMessage(ChatColor.RED + "Player not found!");
        }
    }

    public static void acceptFriend(Player player, String name) {
        try {
            HashMap<UUID, String> requestList = player.getRequests();
            UUID tuuid = null;
            for (Map.Entry<UUID, String> entry : requestList.entrySet()) {
                if (entry.getValue().equalsIgnoreCase(name)) {
                    tuuid = entry.getKey();
                    break;
                }
            }
            if (tuuid == null) {
                player.sendMessage(ChatColor.RED + "That player hasn't sent you a Friend Request!");
                return;
            }
            player.getRequests().remove(tuuid);
            player.getFriends().put(tuuid, name);
            player.sendMessage(ChatColor.YELLOW + "You have accepted " + ChatColor.GREEN + name + "'s " + ChatColor.YELLOW +
                    "Friend Request!");
            PalaceBungee.getMongoHandler().acceptFriendRequest(player.getUniqueId(), tuuid);
            PalaceBungee.getMessageHandler().sendMessageToPlayer(tuuid, player.getRank().getTagColor() + player.getUsername() + ChatColor.YELLOW +
                    " has accepted your Friend Request!");
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "We encountered an error while accepting that friend request, try again in a few minutes!");
        }
    }

    public static void denyFriend(Player player, String name) {
        try {
            HashMap<UUID, String> requestList = player.getRequests();
            UUID tuuid = null;
            for (Map.Entry<UUID, String> entry : requestList.entrySet()) {
                if (entry.getValue().equalsIgnoreCase(name)) {
                    tuuid = entry.getKey();
                    break;
                }
            }
            if (tuuid == null) {
                player.sendMessage(ChatColor.RED + "That player hasn't sent you a Friend Request!");
                return;
            }
            player.getRequests().remove(tuuid);
            player.sendMessage(ChatColor.RED + "You have denied " + ChatColor.GREEN + name + "'s " + ChatColor.RED +
                    "Friend Request!");
            PalaceBungee.getMongoHandler().denyFriendRequest(player.getUniqueId(), tuuid);
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "We encountered an error while denying that friend request, try again in a few minutes!");
        }
    }

    public static void helpMenu(Player player) {
        String dash = ChatColor.GREEN + "- " + ChatColor.AQUA;
        String y = ChatColor.YELLOW.toString();
        player.sendMessage(y + "Friend Commands:\n" + dash + "/friend help " + y + "- Shows this help menu\n" + dash +
                "/friend list [Page]" + y + "- Lists all of your friends\n" + dash + "/friend tp [player] " + y +
                "- Brings you to your friend's server\n" + dash + "/friend toggle " + y + "- Toggles friend requests\n"
                + dash + "/friend add [player] " + y + "- Asks a player to be your friend\n" + dash +
                "/friend remove [player] " + y + "- Removes a player as your friend\n" + dash +
                "/friend accept [player] " + y + "- Accepts someone's friend request\n" + dash +
                "/friend deny [player] " + y + "- Denies someone's friend request\n" + dash + "/friend requests " +
                y + "- Lists all of your friend requests");
    }

    public void friendMessage(Player player, HashMap<UUID, String> friendList, String joinMessage) {
        if (player.getRank().getRankId() >= Rank.TRAINEE.getRankId()) {
            for (Map.Entry<UUID, String> entry : friendList.entrySet()) {
                Player tp = PalaceBungee.getPlayer(entry.getKey());
                if (tp != null) {
                    if (tp.getRank().getRankId() < Rank.TRAINEE.getRankId()) {
                        tp.sendMessage(joinMessage);
                    }
                }
            }
        } else {
            for (Map.Entry<UUID, String> entry : friendList.entrySet()) {
                Player tp = PalaceBungee.getPlayer(entry.getKey());
                if (tp != null) {
                    tp.sendMessage(joinMessage);
                }
            }
        }
    }
}