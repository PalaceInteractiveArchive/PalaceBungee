package network.palace.bungee.utils;

import com.google.common.collect.ImmutableMap;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.*;
import network.palace.bungee.messages.packets.ChangeChannelPacket;
import network.palace.bungee.messages.packets.ComponentMessagePacket;
import network.palace.bungee.messages.packets.SendPlayerPacket;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class PartyUtil {

    public Party createParty(Player player) throws Exception {
        if (PalaceBungee.getMongoHandler().getPartyByMember(player.getUniqueId()) != null) {
            // player is already in a party
            player.sendSubsystemMessage(Subsystem.PARTY, ChatColor.AQUA + "You must leave your current party before you can create a new one.");
            return null;
        }
        Party party = PalaceBungee.getMongoHandler().createParty(player.getUniqueId());
        player.sendSubsystemMessage(Subsystem.PARTY, ChatColor.GREEN + "You have created a party! Invite players with " + ChatColor.YELLOW + "/party invite [Username]");
        return party;
    }

    public void inviteToParty(Player player, String invited) throws Exception {
        Party party = PalaceBungee.getMongoHandler().getPartyByMember(player.getUniqueId());
        if (party == null) {
            player.sendSubsystemMessage(Subsystem.PARTY, ChatColor.AQUA + "You aren't in a party! Create one with " + ChatColor.YELLOW + "/party create");
            return;
        }
        if (!party.isLeader(player.getUniqueId())) {
            player.sendSubsystemMessage(Subsystem.PARTY, ChatColor.RED + "Only the party leader can invite players!");
            return;
        }
        if (invited.equalsIgnoreCase(player.getUsername())) {
            player.sendSubsystemMessage(Subsystem.PARTY, ChatColor.AQUA + "Hmm... I don't think it works like that.");
            return;
        }
        UUID uuid = PalaceBungee.getUUID(invited);
        if (uuid == null || !PalaceBungee.getMongoHandler().isPlayerOnline(uuid)) {
            player.sendSubsystemMessage(Subsystem.PARTY, ChatColor.RED + "Player not found!");
            return;
        }
        if (player.getRank().getRankId() < Rank.TRAINEE.getRankId() && PalaceBungee.getMongoHandler().doesPlayerIgnorePlayer(uuid, player.getUniqueId())) {
            player.sendSubsystemMessage(Subsystem.PARTY, ChatColor.RED + "You can't invite that player to a party!");
            return;
        }
        if (PalaceBungee.getMongoHandler().getPartyByMember(uuid) != null) {
            player.sendSubsystemMessage(Subsystem.PARTY, ChatColor.AQUA + "This player is already in a party!");
            return;
        }
        if (PalaceBungee.getMongoHandler().hasPendingInvite(uuid)) {
            player.sendSubsystemMessage(Subsystem.PARTY, ChatColor.AQUA + "This player already has a pending party invite!");
            return;
        }
        long expires = System.currentTimeMillis() + (30 * 1000);
        party.addInvite(uuid, expires);
        PalaceBungee.getMongoHandler().addPartyInvite(party.getPartyID(), uuid, expires);

        BaseComponent[] components = new ComponentBuilder(Subsystem.PARTY.getComponentPrefix()).color(Subsystem.PARTY.getColor())
                .append(player.getUsername()).color(ChatColor.YELLOW).append(" has invited you to their party! ")
                .color(ChatColor.GREEN).append("Click here to join the party.").color(ChatColor.GOLD).bold(true)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Click to join this party!").color(ChatColor.AQUA).create()))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party accept"))
                .append(" This invite will expire in 30 seconds.", ComponentBuilder.FormatRetention.NONE)
                .color(ChatColor.GREEN).create();

        ComponentMessagePacket packet = new ComponentMessagePacket(components, uuid);
        PalaceBungee.getMessageHandler().sendMessage(packet, PalaceBungee.getMessageHandler().ALL_PROXIES);

        party.messageAllMembers(ChatColor.YELLOW + player.getUsername() + " has asked " + PalaceBungee.getUsername(uuid) +
                " to join the party! They have 30 seconds to accept.", true);
    }

    public void removeFromParty(Player player, String username) throws Exception {
        Party party = PalaceBungee.getMongoHandler().getPartyByMember(player.getUniqueId());
        if (party == null) {
            player.sendSubsystemMessage(Subsystem.PARTY, ChatColor.AQUA + "You aren't in a party! Create one with " + ChatColor.YELLOW + "/party create");
            return;
        }
        if (!party.isLeader(player.getUniqueId())) {
            player.sendSubsystemMessage(Subsystem.PARTY, ChatColor.RED + "Only the party leader can remove players!");
            return;
        }
        UUID uuid = PalaceBungee.getUUID(username);
        if (uuid == null || !party.isMember(uuid)) {
            player.sendSubsystemMessage(Subsystem.PARTY, ChatColor.RED + "Player not found!");
            return;
        }
        party.removeMember(uuid);
        PalaceBungee.getMongoHandler().removePartyMember(party.getPartyID(), uuid);

        party.messageAllMembers(ChatColor.AQUA + PalaceBungee.getUsername(uuid) + " has been removed from the party!", true);

        PalaceBungee.getMessageHandler().sendMessageToPlayer(uuid, ChatColor.GREEN + "You have been removed from " + player.getUsername() + "'s party.");
    }

    public void acceptRequest(Player player) throws Exception {
        Party party = PalaceBungee.getMongoHandler().getPartyByMember(player.getUniqueId());
        if (party != null) {
            player.sendSubsystemMessage(Subsystem.PARTY, ChatColor.AQUA + "You're already in a party! Leave this party first with " + ChatColor.YELLOW + "/party leave");
            return;
        }
        if (PalaceBungee.getMongoHandler().acceptPartyInvite(player.getUniqueId())) {
            // Invite accepted
            party = PalaceBungee.getMongoHandler().getPartyByMember(player.getUniqueId());
            party.addMember(player.getUniqueId(), player.getUsername());
            party.messageAllMembers(ChatColor.YELLOW + player.getUsername() + " has joined the party!", true);
        } else {
            // No invite to accept
            player.sendSubsystemMessage(Subsystem.PARTY, ChatColor.AQUA + "You don't have any pending party invites!");
        }
    }

    public void closeParty(Player player) throws Exception {
        Party party = PalaceBungee.getMongoHandler().getPartyByMember(player.getUniqueId());
        if (party == null) {
            player.sendSubsystemMessage(Subsystem.PARTY, ChatColor.AQUA + "You aren't in a party! Create one with " + ChatColor.YELLOW + "/party create");
            return;
        }
        if (!party.isLeader(player.getUniqueId())) {
            player.sendSubsystemMessage(Subsystem.PARTY, ChatColor.RED + "Only the party leader can close the party!");
            return;
        }
        party.messageAllMembers(ChatColor.RED + player.getUsername() + " has closed the party!", true);
        closeParty(party);
    }

    public void closeParty(Party party) throws Exception {
        party.forAllMembers(uuid -> {
            // Move all players to the main chat channel
            try {
                ChangeChannelPacket packet = new ChangeChannelPacket(uuid, "all");
                PalaceBungee.getMessageHandler().sendMessage(packet, PalaceBungee.getMessageHandler().ALL_PROXIES);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        PalaceBungee.getMongoHandler().closeParty(party.getPartyID());
    }

    public void leaveParty(Player player) throws Exception {
        Party party = PalaceBungee.getMongoHandler().getPartyByMember(player.getUniqueId());
        if (party == null) {
            player.sendSubsystemMessage(Subsystem.PARTY, ChatColor.AQUA + "You aren't in a party! Create one with " + ChatColor.YELLOW + "/party create");
            return;
        }
        if (party.isLeader(player.getUniqueId())) {
            player.sendSubsystemMessage(Subsystem.PARTY, ChatColor.RED + "You can't leave the party since you're the party leader!");
            return;
        }
        party.removeMember(player.getUniqueId());
        PalaceBungee.getMongoHandler().removePartyMember(party.getPartyID(), player.getUniqueId());

        party.messageAllMembers(ChatColor.AQUA + player.getUsername() + " has left the party!", true);
        player.sendSubsystemMessage(Subsystem.PARTY, ChatColor.AQUA + "You have left " + PalaceBungee.getUsername(party.getLeader()) + "'s party!");
    }

    public void listParty(Player player) throws Exception {
        Party party = PalaceBungee.getMongoHandler().getPartyByMember(player.getUniqueId());
        if (party == null) {
            player.sendSubsystemMessage(Subsystem.PARTY, ChatColor.AQUA + "You aren't in a party! Create one with " + ChatColor.YELLOW + "/party create");
            return;
        }
        ImmutableMap<UUID, String> members = party.getMemberMap();
        StringBuilder sb = new StringBuilder();

        String leader = members.get(party.getLeader());

        List<String> names = new ArrayList<>(members.values());
        names.sort(Comparator.comparing(String::toLowerCase));

        int i = 0;
        for (String name : names) {
            if (name.equals(leader)) sb.append("*");
            sb.append(name);
            if (i++ < (members.size() - 1)) sb.append(", ");
        }

        player.sendMessage(Party.MESSAGE_BARS);
        player.sendMessage(ChatColor.YELLOW + "Members of your party: " + sb.toString());
        player.sendMessage(Party.MESSAGE_BARS);
    }

    public void warpParty(Player player) throws Exception {
        Party party = PalaceBungee.getMongoHandler().getPartyByMember(player.getUniqueId());
        if (party == null) {
            player.sendSubsystemMessage(Subsystem.PARTY, ChatColor.AQUA + "You aren't in a party! Create one with " + ChatColor.YELLOW + "/party create");
            return;
        }
        if (!party.isLeader(player.getUniqueId())) {
            player.sendSubsystemMessage(Subsystem.PARTY, ChatColor.RED + "Only the party leader can warp players to their server!");
            return;
        }
        party.messageAllMembers(ChatColor.YELLOW + player.getUsername() + " has warped the party to their server!", true);
        party.forAllMembers(uuid -> {
            try {
                if (uuid.equals(player.getUniqueId())) return;
                player.sendPacket(new SendPlayerPacket(uuid.toString(), player.getServerName()), false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void promoteToLeader(Player player, String username) throws Exception {
        Party party = PalaceBungee.getMongoHandler().getPartyByMember(player.getUniqueId());
        if (party == null) {
            player.sendSubsystemMessage(Subsystem.PARTY, ChatColor.AQUA + "You aren't in a party! Create one with " + ChatColor.YELLOW + "/party create");
            return;
        }
        if (!party.isLeader(player.getUniqueId())) {
            player.sendSubsystemMessage(Subsystem.PARTY, ChatColor.RED + "Only the party leader can promote a new party leader!");
            return;
        }
        UUID uuid = PalaceBungee.getUUID(username);
        if (uuid == null || !party.isMember(uuid)) {
            player.sendSubsystemMessage(Subsystem.PARTY, ChatColor.RED + "Player not found!");
            return;
        }
        if (uuid.equals(player.getUniqueId())) {
            player.sendSubsystemMessage(Subsystem.PARTY, ChatColor.AQUA + "That didn't change much! :shrug:");
            return;
        }
        PalaceBungee.getMongoHandler().setPartyLeader(party.getPartyID(), player.getUniqueId(), uuid);
        party.messageAllMembers(ChatColor.YELLOW + PalaceBungee.getUsername(uuid) + " has been promoted to party leader!", true);
    }

    public void chat(Player player, String msg) throws Exception {
        String processed = PalaceBungee.getChatUtil().processChatMessage(player, msg, "PC", false);
        if (processed == null) return;

        Party party = PalaceBungee.getMongoHandler().getPartyByMember(player.getUniqueId());
        if (party == null) {
            player.sendSubsystemMessage(Subsystem.PARTY, ChatColor.AQUA + "You aren't in a party! Create one with " + ChatColor.YELLOW + "/party create");
            return;
        }

        PalaceBungee.getChatUtil().analyzeMessage(player.getUniqueId(), player.getRank(), processed, "PartyChat", () -> {
            try {
                Rank rank = player.getRank();
                String message;
                try {
                    message = EmojiUtil.convertMessage(player, processed);
                } catch (IllegalArgumentException e) {
                    player.sendMessage(ChatColor.RED + e.getMessage());
                    return;
                }
                message = Subsystem.PARTY.getPrefix() + (party.isLeader(player.getUniqueId()) ? ChatColor.YELLOW + "* " : "") +
                        RankTag.format(player.getTags()) + rank.getFormattedName() + ChatColor.GRAY + " " + player.getUsername() + ": " + ChatColor.WHITE +
                        (rank.getRankId() >= Rank.TRAINEE.getRankId() ? ChatColor.translateAlternateColorCodes('&', message) : message);
                party.messageAllMembers(message, false);
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "An error occurred while sending your party chat message! Please try again in a few minutes.");
            }
        });
    }
}
