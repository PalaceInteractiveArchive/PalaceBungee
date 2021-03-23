package network.palace.bungee.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.chat.ComponentSerializer;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.RankTag;
import network.palace.bungee.messages.packets.BroadcastPacket;
import network.palace.bungee.messages.packets.ComponentMessagePacket;
import network.palace.bungee.messages.packets.MentionByRankPacket;
import network.palace.bungee.messages.packets.MessageByRankPacket;
import org.bson.Document;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class GuideUtil {

    /**
     * Check whether a player can submit a help request
     *
     * @param uuid the uuid of the player
     * @return true if the player hasn't submitted an unanswered request in the last 30 seconds
     */
    public boolean canSubmitHelpRequest(UUID uuid) {
        return System.currentTimeMillis() - PalaceBungee.getMongoHandler().lastHelpRequest(uuid) >= 30 * 1000;
    }

    /**
     * Submit a help request to online staff and Guides
     *
     * @param player  the player submitting the help request
     * @param request the request being submitted
     */
    public void sendHelpRequest(Player player, String request) {
        BaseComponent[] components = new ComponentBuilder("[").color(ChatColor.WHITE)
                .append("HELP").color(ChatColor.GREEN).append("] ").color(ChatColor.WHITE)
                .append(player.getUsername()).color(player.getRank().getTagColor())
                .append(" submitted a help request: ").color(ChatColor.AQUA)
                .append(request + "\n").color(ChatColor.GREEN)
                .append("Accept Request").color(ChatColor.DARK_GREEN).italic(true)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Click to accept this help request!").color(ChatColor.AQUA).create()))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/h accept " + player.getUsername())).create();
        boolean staff = PalaceBungee.getMongoHandler().areStaffOnline(true);
        try {
            PalaceBungee.getMessageHandler().sendMessage(new MessageByRankPacket(ComponentSerializer.toString(components), Rank.TRAINEE, RankTag.GUIDE, false, true), PalaceBungee.getMessageHandler().ALL_PROXIES);
            PalaceBungee.getMessageHandler().sendMessage(new MentionByRankPacket(null, RankTag.GUIDE, true), PalaceBungee.getMessageHandler().ALL_PROXIES);
        } catch (IOException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "We encountered an error while processing your help request, try again in a few minutes!");
            return;
        }
        if (!staff) {
            player.sendMessage(new ComponentBuilder("Unfortunately, there isn't anyone online right now to help with your request. In the meantime, you could ask for help on our ").color(ChatColor.AQUA)
                    .append("Discord server.").color(ChatColor.BLUE)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("Click to visit ").color(ChatColor.GREEN)
                                    .append("https://palnet.us/Discord").color(ChatColor.YELLOW).create()))
                    .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://palnet.us/Discord")).create());
        } else {
            PalaceBungee.getMongoHandler().setPendingHelpRequest(player.getUniqueId(), true);
            PalaceBungee.getMongoHandler().setLastHelpRequest(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Your help request has been sent!");
        }
    }

    /**
     * Accept the help request submitted by the target player
     *
     * @param player   the staff/guide accepting the request
     * @param tpUUID   the uuid of the target player
     * @param username the username of the target player
     */
    public void acceptHelpRequest(Player player, UUID tpUUID, String username) throws IOException {
        if (!PalaceBungee.getMongoHandler().hasPendingHelpRequest(tpUUID) || System.currentTimeMillis() - PalaceBungee.getMongoHandler().lastHelpRequest(tpUUID) >= 10 * 60 * 1000) {
            player.sendMessage(ChatColor.RED + "That player hasn't submitted a help request recently!");
            return;
        }
        BaseComponent[] components = new ComponentBuilder("[").color(ChatColor.WHITE)
                .append("HELP").color(ChatColor.GREEN).append("] ").color(ChatColor.WHITE)
                .append(player.getUsername()).color(player.getRank().getTagColor())
                .append(" accepted ").color(ChatColor.AQUA)
                .append(username + "'s help request").color(ChatColor.AQUA).create();
        PalaceBungee.getMessageHandler().sendMessage(new MessageByRankPacket(ComponentSerializer.toString(components),
                Rank.TRAINEE, RankTag.GUIDE, false, true), PalaceBungee.getMessageHandler().ALL_PROXIES);

        Rank rank = player.getRank();
        BaseComponent[] tpMessage = new ComponentBuilder(rank.getName()).color(rank.getTagColor()).bold(true)
                .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + player.getUsername() + " "))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to start your message with " + player.getUsername()).color(ChatColor.GREEN).create()))
                .append(" " + player.getUsername()).bold(false)
                .append(" has accepted your help request. Contact them by typing ").color(ChatColor.AQUA)
                .append("/msg " + player.getUsername() + " [Your Message]").color(ChatColor.YELLOW)
                .append(" (or click on this message)").color(ChatColor.GREEN).create();
        PalaceBungee.getMessageHandler().sendMessage(new ComponentMessagePacket(tpMessage, tpUUID), PalaceBungee.getMessageHandler().ALL_PROXIES);
        PalaceBungee.getMongoHandler().setPendingHelpRequest(tpUUID, false);
        PalaceBungee.getMongoHandler().logHelpRequest(tpUUID, player.getUniqueId());
    }

    /**
     * Teleport the player to the target player, across servers if necessary
     *
     * @param player       the staff/guide teleporting
     * @param tpName       the username of the player being teleported to
     * @param targetServer the name of the server the target player is on
     */
    public void teleport(Player player, String tpName, String targetServer) {
        if (player.getServerName().equals(targetServer)) {
            player.sendMessage(ChatColor.GREEN + "You're already on the same server as this player! Teleporting you to " + tpName + "...");
            player.chat("/tp " + tpName);
            return;
        }
        player.sendMessage(ChatColor.GREEN + "Sending you to " + targetServer + "...");
        PalaceBungee.getServerUtil().sendPlayer(player, targetServer);
        new Timer().scheduleAtFixedRate(new TimerTask() {
            int counts = 0;
            boolean lastRun = false;

            @Override
            public void run() {
                try {
                    if (lastRun) {
                        cancel();
                        player.sendMessage(ChatColor.GREEN + "Teleporting you to " + tpName + "...");
                        player.chat("/tp " + tpName);
                        return;
                    }
                    if (player.getServerName().equals(targetServer)) {
                        lastRun = true;
                    }
                    if (counts++ >= 10 && !lastRun) {
                        cancel();
                        player.sendMessage(ChatColor.RED + "Request timed out!\nThere was an issue sending you to " +
                                targetServer + ", so your teleport request couldn't be completed.");
                    }
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "There was an issue handling this request!");
                    cancel();
                }
            }
        }, 500, 500);
    }

    /**
     * Submit an announcement request
     *
     * @param player       the guide/trainee submitting the help request
     * @param announcement the announcement to be broadcasted
     */
    public void sendAnnouncementRequest(Player player, String announcement) {
        BaseComponent[] components = new ComponentBuilder("[").color(ChatColor.WHITE).append("STAFF").color(ChatColor.RED)
                .append("] ").color(ChatColor.WHITE).append(player.getUsername() + " wants to send the announcement: " + announcement + "\n").color(ChatColor.GREEN)
                .append("Accept Request").color(ChatColor.DARK_GREEN).italic(true)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Click to accept this announcement request").color(ChatColor.AQUA).create()))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gannounce accept " + player.getUsername()))
                .append(" - ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GREEN)
                .append("Decline Request").color(ChatColor.RED).italic(true)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Click to decline this announcement request").color(ChatColor.AQUA).create()))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gannounce decline " + player.getUsername())).create();
        boolean staff = PalaceBungee.getMongoHandler().areStaffOnline(true);
        if (!staff) {
            player.sendMessage(new ComponentBuilder("Unfortunately, there aren't any staff online to accept this announcement request.").color(ChatColor.AQUA).create());
        } else {
            try {
                PalaceBungee.getMessageHandler().sendMessage(new MessageByRankPacket(ComponentSerializer.toString(components), RANK.CM, null, false, true), PalaceBungee.getMessageHandler().ALL_PROXIES);
                PalaceBungee.getMongoHandler().makeAnnouncementRequest(player.getUniqueId(), announcement);
            } catch (IOException e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "We encountered an error while processing your help request, try again in a few minutes!");
            }
        }
    }

    /**
     * Accept an announcement request submitted by a guide/trainee
     *
     * @param player   the staff member accepting the request
     * @param username the guide/trainee who submitted the request
     */
    public void acceptAnnouncementRequest(Player player, String username) {
        Player tp = PalaceBungee.getPlayer(username);
        UUID tpUUID;
        if (tp == null) {
            tpUUID = PalaceBungee.getMongoHandler().usernameToUUID(username);
        } else {
            tpUUID = tp.getUniqueId();
            username = tp.getUsername();
        }
        if (tpUUID == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }
        Document doc;
        if ((doc = PalaceBungee.getMongoHandler().findAnnouncementRequest(tpUUID)) == null) {
            player.sendMessage(ChatColor.RED + "That player hasn't submitted an announcement request recenty!");
            return;
        }
        PalaceBungee.getMongoHandler().removeAnnouncementRequest(tpUUID);
        String message = doc.getString("announcement");
        try {
            PalaceBungee.getMessageHandler().sendStaffMessage(ChatColor.GREEN + player.getUsername() + ChatColor.AQUA + " accepted " + ChatColor.GREEN +
                    username + "'s " + ChatColor.AQUA + "announcement request");
            PalaceBungee.getMessageHandler().sendMessageToPlayer(tpUUID, player.getRank().getTagColor() + player.getUsername() + ChatColor.AQUA +
                    " has accepted your announcement request.");
            PalaceBungee.getMessageHandler().sendMessage(new BroadcastPacket(username, ChatColor.translateAlternateColorCodes('&', message)), PalaceBungee.getMessageHandler().ALL_PROXIES);
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred while processing that announcement request. Try again in a few minutes!");
        }
    }

    /**
     * Decline an announcement request submitted by a guide/trainee
     *
     * @param player   the staff member declining the request
     * @param username the guide/trainee who submitted the request
     */
    public void declineAnnouncementRequest(Player player, String username) {
        Player tp = PalaceBungee.getPlayer(username);
        UUID tpUUID;
        if (tp == null) {
            tpUUID = PalaceBungee.getMongoHandler().usernameToUUID(username);
        } else {
            tpUUID = tp.getUniqueId();
            username = tp.getUsername();
        }
        if (tpUUID == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }
        if (PalaceBungee.getMongoHandler().findAnnouncementRequest(tpUUID) == null) {
            player.sendMessage(ChatColor.RED + "That player hasn't submitted an announcement request recenty!");
            return;
        }
        PalaceBungee.getMongoHandler().removeAnnouncementRequest(tpUUID);
        try {
            PalaceBungee.getMessageHandler().sendStaffMessage(ChatColor.GREEN + player.getUsername() + ChatColor.AQUA + " declined " + ChatColor.GREEN +
                    username + "'s " + ChatColor.AQUA + "announcement request");
            PalaceBungee.getMessageHandler().sendMessageToPlayer(tpUUID, player.getRank().getTagColor() + player.getUsername() + ChatColor.AQUA +
                    " has declined your announcement request.");
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred while processing that announcement request. Try again in a few minutes!");
        }
    }


    public boolean overloaded() {
        return PalaceBungee.getMongoHandler().areHelpRequestsOverloaded();
    }
}
