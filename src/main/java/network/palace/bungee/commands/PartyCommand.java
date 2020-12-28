package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;

public class PartyCommand extends PalaceCommand {

    public PartyCommand() {
        super("party", "p");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "help":
                    helpMenu(player);
                    return;
                case "create":
                    try {
                        PalaceBungee.getPartyUtil().createParty(player);
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "An error occurred while creating a party! Please try again in a few minutes.");
                    }
                    return;
                case "accept":
                    try {
                        PalaceBungee.getPartyUtil().acceptRequest(player);
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "An error occurred while accepting that party invite!");
                    }
                    return;
                case "close":
                    try {
                        PalaceBungee.getPartyUtil().closeParty(player);
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "An error occurred while closing the party! Please try again in a few minutes.");
                    }
                    return;
                case "leave":
                    try {
                        PalaceBungee.getPartyUtil().leaveParty(player);
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "An error occurred while leaving the party! Please try again in a few minutes.");
                    }
                    return;
                case "list":
                    try {
                        PalaceBungee.getPartyUtil().listParty(player);
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "An error occurred while listing party members! Please try again in a few minutes.");
                    }
                    return;
                case "warp":
                    try {
                        PalaceBungee.getPartyUtil().warpParty(player);
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "An error occurred while warping party members to your server! Please try again in a few minutes.");
                    }
                    return;
                case "remove":
                    if (args.length > 1) {
                        try {
                            PalaceBungee.getPartyUtil().removeFromParty(player, args[1]);
                        } catch (Exception e) {
                            e.printStackTrace();
                            player.sendMessage(ChatColor.RED + "An error occurred while removing that player from your party! Please try again in a few minutes.");
                        }
                    }
                    return;
                case "promote":
                    if (args.length > 1) {
                        try {
                            PalaceBungee.getPartyUtil().promoteToLeader(player, args[1]);
                        } catch (Exception e) {
                            e.printStackTrace();
                            player.sendMessage(ChatColor.RED + "An error occurred while promoting a new party leader! Please try again in a few minutes.");
                        }
                    }
                    return;
                case "chat":
                    try {
                        String message = "";
                        StringBuilder msg = new StringBuilder();
                        for (int i = 1; i < args.length; i++) {
                            msg.append(args[i]).append(" ");
                        }
                        PalaceBungee.getPartyUtil().chat(player, msg.toString().trim());
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "An error occurred while sending your party chat message! Please try again in a few minutes.");
                    }
                    return;
                case "invite":
                    if (args.length > 1) {
                        try {
                            PalaceBungee.getPartyUtil().inviteToParty(player, args[1]);
                        } catch (Exception e) {
                            e.printStackTrace();
                            player.sendMessage(ChatColor.RED + "An error occurred while inviting that player to your party! Please try again in a few minutes.");
                        }
                        return;
                    }
            }
        }
        helpMenu(player);
    }

    public void helpMenu(Player player) {
        String dash = "\n" + ChatColor.GREEN + "- " + ChatColor.AQUA;
        String y = ChatColor.YELLOW.toString();
        player.sendMessage(y + "Party Commands:" +
                dash + "/party help " + y + "- Shows this help menu" +
                dash + "/party create " + y + "- Create a new Party" +
                dash + "/party invite [player] " + y + "- Invite a player to your Party" +
                dash + "/party leave " + y + "- Leave your current Party" +
                dash + "/party list " + y + "- List all of the members in your Party" +
                dash + "/party promote [player] " + y + "- Promote a player to Party Leader" +
                dash + "/party accept " + y + "- Accept a Party invite from a player" +
                dash + "/party warp " + y + "- Brings the members of your Party to your server" +
                dash + "/party remove [player] " + y + "- Removes a player from your Party" +
                dash + "/pchat [message] " + y + "- Message members of your Party" +
                dash + "/party close " + y + "- Close your Party");
    }
}