package network.palace.bungee.party;

import com.google.common.collect.ImmutableMap;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.messages.packets.ComponentMessagePacket;

import java.util.Map;
import java.util.UUID;

public class PartyUtil {

    public Party createParty(Player player) throws Exception {
        if (PalaceBungee.getMongoHandler().getPartyByMember(player.getUniqueId()) != null) {
            // player is already in a party
            player.sendMessage(ChatColor.AQUA + "You must leave your current party before you can create a new one.");
            return null;
        }
        Party party = PalaceBungee.getMongoHandler().createParty(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "You have created a party! Invite players with " + ChatColor.YELLOW + "/party invite [Username]");
        return party;
    }

    public void inviteToParty(Player player, String invited) throws Exception {
        Party party = PalaceBungee.getMongoHandler().getPartyByMember(player.getUniqueId());
        if (party == null) {
            player.sendMessage(ChatColor.AQUA + "You aren't in a party! Create one with " + ChatColor.YELLOW + "/party create");
            return;
        }
        if (!party.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the party leader can invite players!");
            return;
        }
        UUID uuid = PalaceBungee.getUUID(invited);
        if (uuid == null || !PalaceBungee.getMongoHandler().isPlayerOnline(uuid)) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }
        if (PalaceBungee.getMongoHandler().getPartyByMember(uuid) != null) {
            player.sendMessage(ChatColor.AQUA + "This player is already in a party!");
            return;
        }
        if (PalaceBungee.getMongoHandler().hasPendingInvite(uuid)) {
            player.sendMessage(ChatColor.AQUA + "This player already has a pending party invite!");
            return;
        }
        long expires = System.currentTimeMillis();
        party.addInvite(uuid, expires);
        PalaceBungee.getMongoHandler().addPartyInvite(party.getPartyID(), uuid, expires);

        BaseComponent[] components = new ComponentBuilder(player.getUsername()).color(ChatColor.YELLOW)
                .append(" has invited you to their Party! ").color(ChatColor.GREEN).append("Click here to join the Party.")
                .color(ChatColor.GOLD).bold(true).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Click to join this Party!").color(ChatColor.AQUA).create()))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party accept"))
                .append(" This invite will expire in 5 minutes.", ComponentBuilder.FormatRetention.NONE)
                .color(ChatColor.GREEN).create();

        ComponentMessagePacket packet = new ComponentMessagePacket(components, uuid);
        PalaceBungee.getMessageHandler().sendMessage(packet, "all_proxies", "fanout");

        party.messageAllMembers(ChatColor.YELLOW + player.getUsername() + " has asked " + PalaceBungee.getUsername(uuid) +
                " to join the party! They have 5 minutes to accept.", true);
    }

    public void removeFromParty(Player player, String username) throws Exception {
        Party party = PalaceBungee.getMongoHandler().getPartyByMember(player.getUniqueId());
        if (party == null) {
            player.sendMessage(ChatColor.AQUA + "You aren't in a party! Create one with " + ChatColor.YELLOW + "/party create");
            return;
        }
        if (!party.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the party leader can remove players!");
            return;
        }
        UUID uuid = PalaceBungee.getUUID(username);
        if (uuid == null || !party.isMember(uuid)) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }
        party.removeMember(uuid);

//        messageToAllMembers(ChatColor.YELLOW + dashboard.getPlayer(leader).getUsername() + " has removed " +
//                tp.getUsername() + " from the Party!", true);

        PalaceBungee.getMessageHandler().sendMessageToPlayer(uuid, player.getRank().getChatColor() + player.getUsername() + ChatColor.GREEN + " has removed you from their party.");
    }

    public void acceptRequest(Player player) {
    }

    public void denyRequest(Player player) {
    }

    public void closeParty(Player player) {
    }

    public void leaveParty(Player player) {
    }

    public void listParty(Player player) {
        Party party = getParty(player);
        if (party == null) return;
        ImmutableMap<UUID, String> members = party.getMemberMap();
        StringBuilder sb = new StringBuilder();

        int i = 0;
        for (Map.Entry<UUID, String> entry : members.entrySet()) {
            UUID uuid = entry.getKey();
            String name = entry.getValue();
            if (uuid.equals(party.getLeader())) {
                sb.append("*");
            }
            sb.append(name);
            if (i < members.size() - 1) {
                sb.append(", ");
            }
        }
        player.sendMessage(Party.MESSAGE_BARS);
        player.sendMessage(ChatColor.YELLOW + "Members of your Party: " + sb.toString());
        player.sendMessage(Party.MESSAGE_BARS);
    }

    public void warpParty(Player player) {
    }

    public void promoteToLeader(Player player) {
    }

    public void chat(Player player) {
    }

    /**
     * Get a Party object representing the player's current party
     *
     * @param player the player
     * @return the party
     */
    public Party getParty(Player player) {
        return null;
    }
}
