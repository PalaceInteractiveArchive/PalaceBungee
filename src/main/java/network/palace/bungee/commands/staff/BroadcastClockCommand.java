package network.palace.bungee.commands.staff;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.utils.MiscUtil;

import java.util.List;

public class BroadcastClockCommand extends PalaceCommand {

    public BroadcastClockCommand() {
        super("bclock", Rank.DEVELOPER);
    }

    @Override
    public void execute(Player player, String[] args) {
        try {
            if (args.length < 1) {
                helpMenu(player);
                return;
            }
            switch (args[0].toLowerCase()) {
                case "list": {
                    List<String> announcements = PalaceBungee.getConfigUtil().getAnnouncements();
                    player.sendMessage(ChatColor.GREEN + "Announcements:");
                    for (int i = 0; i < announcements.size(); i++) {
                        player.sendMessage(ChatColor.YELLOW + "" + (i + 1) + ": " + ChatColor.WHITE + announcements.get(i));
                    }
                    break;
                }
                case "create": {
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "/bclock create [Message]");
                        return;
                    }
                    String[] argsMinusOne = new String[args.length - 1];
                    System.arraycopy(args, 1, argsMinusOne, 0, args.length - 1);
                    List<String> announcements = PalaceBungee.getConfigUtil().getAnnouncements();
                    String text = ChatColor.translateAlternateColorCodes('&', String.join(" ", argsMinusOne));
                    announcements.add(text);
                    PalaceBungee.getConfigUtil().setAnnouncements(announcements, true);
                    player.sendMessage(ChatColor.GREEN + "Added a new announcement with the message '" + ChatColor.WHITE + text + ChatColor.GREEN + "'.");
                    break;
                }
                case "remove": {
                    if (args.length < 2 || !MiscUtil.isInt(args[1])) {
                        player.sendMessage(ChatColor.RED + "/bclock remove [ID]");
                        return;
                    }
                    List<String> announcements = PalaceBungee.getConfigUtil().getAnnouncements();
                    int id = Integer.parseInt(args[1]) - 1;
                    if (id >= announcements.size() || id < 0) {
                        player.sendMessage(ChatColor.RED + "That isn't a valid announcement ID! Run /bclock list to view announcement IDs.");
                        return;
                    }
                    String text = announcements.remove(id);
                    PalaceBungee.getConfigUtil().setAnnouncements(announcements, true);
                    player.sendMessage(ChatColor.RED + "Removed the announcement with the message '" + ChatColor.WHITE + text + ChatColor.RED + "'.");
                    break;
                }
                case "reload": {
                    player.sendMessage(ChatColor.AQUA + "Run /proxyreload to reload announcements!");
                    break;
                }
                default: {
                    helpMenu(player);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred while running that command. Check console for errors.");
        }
//        switch (args.length) {
//            case 1:
//                switch (args[0].toLowerCase()) {
//                    case "help":
//                        FriendUtil.helpMenu(player);
//                        return;
//                    case "list":
//                        FriendUtil.listFriends(player, 1);
//                        return;
//                    case "toggle":
//                        PalaceBungee.getProxyServer().getScheduler().runAsync(PalaceBungee.getInstance(), () -> {
//                            player.setFriendRequestToggle(!player.hasFriendToggledOff());
//                            if (player.hasFriendToggledOff()) {
//                                player.sendMessage(ChatColor.YELLOW + "Friend Requests have been toggled " + ChatColor.RED + "OFF");
//                            } else {
//                                player.sendMessage(ChatColor.YELLOW + "Friend Requests have been toggled " + ChatColor.GREEN + "ON");
//                            }
//                            PalaceBungee.getMongoHandler().setFriendRequestToggle(player.getUniqueId(), !player.hasFriendToggledOff());
//                        });
//                        return;
//                    case "requests":
//                        FriendUtil.listRequests(player);
//                        return;
//                }
//                return;
//            case 2:
//                switch (args[0].toLowerCase()) {
//                    case "list":
//                        if (!MiscUtil.isInt(args[1])) {
//                            FriendUtil.listFriends(player, 1);
//                            return;
//                        }
//                        FriendUtil.listFriends(player, Integer.parseInt(args[1]));
//                        return;
//                    case "tp":
//                        String user = args[1];
//                        Player tp = PalaceBungee.getPlayer(user);
//                        if (tp == null) {
//                            player.sendMessage(ChatColor.RED + "Player not found!");
//                            return;
//                        }
//                        if (!player.getFriends().containsKey(tp.getUniqueId())) {
//                            player.sendMessage(ChatColor.GREEN + tp.getUsername() + ChatColor.RED +
//                                    " is not on your Friend List!");
//                            return;
//                        }
//                        FriendUtil.teleportPlayer(player, tp);
//                        return;
//                    case "add":
//                        FriendUtil.addFriend(player, args[1]);
//                        return;
//                    case "remove":
//                        FriendUtil.removeFriend(player, args[1]);
//                        return;
//                    case "accept":
//                        FriendUtil.acceptFriend(player, args[1]);
//                        return;
//                    case "deny":
//                        FriendUtil.denyFriend(player, args[1]);
//                        return;
//                }
//        }
//        FriendUtil.helpMenu(player);
    }

    private void helpMenu(Player player) {
        String dash = ChatColor.GREEN + "- " + ChatColor.AQUA;
        String y = ChatColor.YELLOW.toString();
        player.sendMessage(y + "Broadcast Clock Commands:\n" + dash +
                "/bclock help " + y + "- Shows this help menu\n" + dash +
                "/bclock list " + y + "- List broadcast clock messages\n" + dash +
                "/bclock create [Message] " + y + "- Create a new broadcast clock message\n" + dash +
                "/bclock remove [ID] " + y + "- Remove a broadcast clock message\n" + dash +
                "/bclock reload " + y + "- Reload broadcast messages");
    }
}