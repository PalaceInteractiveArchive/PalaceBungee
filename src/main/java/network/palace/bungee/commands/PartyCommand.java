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
                case "accept":
                    PalaceBungee.getPartyUtil().acceptRequest(player);
                    return;
                case "deny":
                    PalaceBungee.getPartyUtil().denyRequest(player);
                    return;
                case "close":
                    PalaceBungee.getPartyUtil().closeParty(player);
                    return;
                case "leave":
                    PalaceBungee.getPartyUtil().leaveParty(player);
                    return;
                case "list":
                    PalaceBungee.getPartyUtil().listParty(player);
                    return;
                case "warp":
                    PalaceBungee.getPartyUtil().warpParty(player);
                    return;
                case "remove":
                    PalaceBungee.getPartyUtil().removeFromParty(player);
                    return;
                case "promote":
                    PalaceBungee.getPartyUtil().promoteToLeader(player);
                    return;
                case "chat":
                    PalaceBungee.getPartyUtil().chat(player);
                    return;
                case "invite":
                    PalaceBungee.getPartyUtil().inviteToParty(player);
                    return;
            }
        }
        helpMenu(player);
    }

    public void helpMenu(Player player) {
        String dash = ChatColor.GREEN + "- " + ChatColor.AQUA;
        String y = ChatColor.YELLOW.toString();
        player.sendMessage(y + "Party Commands:\n" + dash + "/party help " + y + "- Shows this help menu\n" + dash +
                "/party invite [player] " + y + "- Invite a player to your Party\n" + dash + "/party leave " + y +
                "- Leave your current Party\n" + dash + "/party list " + y + "- List all of the members in your Party\n"
                + dash + "/party promote [player] " + y + "- Promote a player to Party Leader\n" + dash +
                "/party accept " + y + "- Accept a Party invite from a player\n" + dash + "/party deny " + y +
                "- Deny a Party Request\n" + dash + "/party warp " + y +
                "- Brings the members of your Party to your server\n" + dash + "/party remove [player] " + y +
                "- Removes a player from your Party\n" + dash + "/pchat [message] " + y +
                "- Message members of your Party\n" + dash + "/party close " + y +
                "- Close your Party");
    }
}