package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.utils.FriendUtil;
import network.palace.bungee.utils.MiscUtil;

public class FriendCommand extends PalaceCommand {

    public FriendCommand() {
        super("friend", "f");
    }

    @Override
    public void execute(Player player, String[] args) {
        switch (args.length) {
            case 1:
                switch (args[0].toLowerCase()) {
                    case "help":
                        FriendUtil.helpMenu(player);
                        return;
                    case "list":
                        FriendUtil.listFriends(player, 1);
                        return;
                    case "toggle":
                        PalaceBungee.getProxyServer().getScheduler().runAsync(PalaceBungee.getInstance(), () -> {
                            player.setFriendRequestToggle(!player.hasFriendToggledOff());
                            if (player.hasFriendToggledOff()) {
                                player.sendMessage(ChatColor.YELLOW + "Friend Requests have been toggled " + ChatColor.RED + "OFF");
                            } else {
                                player.sendMessage(ChatColor.YELLOW + "Friend Requests have been toggled " + ChatColor.GREEN + "ON");
                            }
                            PalaceBungee.getMongoHandler().setFriendRequestToggle(player.getUniqueId(), !player.hasFriendToggledOff());
                        });
                        return;
                    case "requests":
                        FriendUtil.listRequests(player);
                        return;
                }
                return;
            case 2:
                switch (args[0].toLowerCase()) {
                    case "list":
                        if (!MiscUtil.isInt(args[1])) {
                            FriendUtil.listFriends(player, 1);
                            return;
                        }
                        FriendUtil.listFriends(player, Integer.parseInt(args[1]));
                        return;
                    case "tp":
                        String user = args[1];
                        Player tp = PalaceBungee.getPlayer(user);
                        if (tp == null) {
                            player.sendMessage(ChatColor.RED + "Player not found!");
                            return;
                        }
                        if (!player.getFriends().containsKey(tp.getUniqueId())) {
                            player.sendMessage(ChatColor.GREEN + tp.getUsername() + ChatColor.RED +
                                    " is not on your Friend List!");
                            return;
                        }
                        FriendUtil.teleportPlayer(player, tp);
                        return;
                    case "add":
                        FriendUtil.addFriend(player, args[1]);
                        return;
                    case "remove":
                        FriendUtil.removeFriend(player, args[1]);
                        return;
                    case "accept":
                        FriendUtil.acceptFriend(player, args[1]);
                        return;
                    case "deny":
                        FriendUtil.denyFriend(player, args[1]);
                        return;
                }
        }
        FriendUtil.helpMenu(player);
    }
}