package network.palace.bungee.listeners;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.*;
import network.palace.bungee.handlers.moderation.AddressBan;
import network.palace.bungee.handlers.moderation.Ban;
import network.palace.bungee.handlers.moderation.ProviderBan;
import network.palace.bungee.handlers.moderation.ProviderData;
import network.palace.bungee.messages.packets.FriendJoinPacket;
import network.palace.bungee.slack.SlackAttachment;
import network.palace.bungee.slack.SlackMessage;
import network.palace.bungee.utils.IPUtil;
import org.bson.Document;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.logging.Level;

public class PlayerJoinAndLeave implements Listener {

    @EventHandler
    public void onPlayerJoin(LoginEvent event) {
        PendingConnection connection = event.getConnection();
        if (connection.getVersion() < ProtocolConstants.LOWEST_VERSION.getProtocolId() || connection.getVersion() > ProtocolConstants.HIGHEST_VERSION.getProtocolId()) {
            event.setCancelled(true);
            event.setCancelReason(new ComponentBuilder("You must connect with ").color(ChatColor.RED)
                    .append(ProtocolConstants.getVersionString(), ComponentBuilder.FormatRetention.ALL)
                    .append("!", ComponentBuilder.FormatRetention.ALL).create());
            return;
        }

        String address = ((InetSocketAddress) connection.getSocketAddress()).getAddress().toString().replaceAll("/", "");

        Document doc = PalaceBungee.getMongoHandler().getPlayer(connection.getUniqueId(), new Document("rank", true).append("tags", true).append("online", true).append("settings", true));

        if (doc != null && doc.containsKey("online") && doc.getBoolean("online")) {
            event.setCancelled(true);
            event.setCancelReason(new ComponentBuilder("You are already connected to this server!").color(ChatColor.RED).create());
            return;
        }

        Player player;
        Rank rank;
        if (doc == null) {
            // new player
            rank = Rank.GUEST;
            player = new Player(connection.getUniqueId(), connection.getName(), rank, new ArrayList<>(), address, connection.getVersion(), true);
        } else {
            AddressBan addressBan = PalaceBungee.getMongoHandler().getAddressBan(address);
            if (addressBan != null) {
                event.setCancelled(true);
                event.setCancelReason(PalaceBungee.getModerationUtil().getBanMessage(addressBan));
                return;
            }
            String[] list = address.split("\\.");
            String range = list[0] + "." + list[1] + "." + list[2] + ".*";
            AddressBan rangeBan = PalaceBungee.getMongoHandler().getAddressBan(range);
            if (rangeBan != null) {
                event.setCancelled(true);
                event.setCancelReason(PalaceBungee.getModerationUtil().getBanMessage(rangeBan));
                return;
            }
            Ban ban = PalaceBungee.getMongoHandler().getCurrentBan(connection.getUniqueId(), connection.getName());
            if (ban != null) {
                if (ban.isPermanent()) {
                    event.setCancelled(true);
                    event.setCancelReason(PalaceBungee.getModerationUtil().getBanMessage(ban));
                    return;
                } else {
                    if (ban.getExpires() > System.currentTimeMillis()) {
                        event.setCancelled(true);
                        event.setCancelReason(PalaceBungee.getModerationUtil().getBanMessage(ban));
                        return;
                    }
                    PalaceBungee.getMongoHandler().unbanPlayer(connection.getUniqueId());
                }
            }

            rank = Rank.fromString(doc.getString("rank"));

            List<RankTag> tagList = new ArrayList<>();
            if (doc.containsKey("tags")) {
                var tags = doc.get("tags", ArrayList.class);
                for (Object s : tags) {
                    RankTag tag = RankTag.fromString((String) s);
                    if (tag != null) tagList.add(tag);
                }
            }

            Document settings = (Document) doc.get("settings");

            player = new Player(connection.getUniqueId(), connection.getName(), rank, tagList, address, connection.getVersion(), settings.getBoolean("mentions"));
            player.setMute(PalaceBungee.getMongoHandler().getCurrentMute(connection.getUniqueId()));
        }
        if (PalaceBungee.getConfigUtil().isMaintenance() && rank.getRankId() < Rank.DEVELOPER.getRankId()) {
            event.setCancelled(true);
            event.setCancelReason(new ComponentBuilder("We are currently performing maintenance on our servers.\nFollow ")
                    .color(ChatColor.AQUA).append("@PalaceDev ").color(ChatColor.BLUE).append("on Twitter for updates!")
                    .color(ChatColor.AQUA).create());
            return;
        }
        PalaceBungee.login(player);
        PalaceBungee.getProxyServer().getScheduler().runAsync(PalaceBungee.getInstance(), () -> {
            ProviderData data = IPUtil.getProviderData(player.getAddress());
            if (data != null) {
                player.setIsp(data.getIsp());
                if (!player.getIsp().isEmpty()) {
                    ProviderBan ban = PalaceBungee.getMongoHandler().getProviderBan(player.getIsp());
                    if (ban != null) {
                        player.kickPlayer(PalaceBungee.getModerationUtil().getBanMessage(ban));
                    }
                }
                PalaceBungee.getMongoHandler().updateProviderData(player.getUniqueId(), data);
            }
        });
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer pl = event.getPlayer();
        Player player = PalaceBungee.getPlayer(pl.getUniqueId());
        if (player == null) {
            pl.disconnect(TextComponent.fromLegacyText(ChatColor.RED + "We are currently experiencing some server-side issues. Please check back soon!"));
        } else {
            Rank rank = player.getRank();
            boolean disable = player.isDisabled();
            if (PalaceBungee.getConfigUtil().isStrictChat() && rank.getRankId() >= Rank.TRAINEE.getRankId())
                player.sendMessage(ChatColor.RED + "\nChat is currently in strict mode!\n");
            if (rank.getRankId() >= Rank.CHARACTER.getRankId()) {
                String msg = rank.getFormattedName() + " " + ChatColor.YELLOW + player.getUsername() + " has clocked in";
                if (disable) msg += ChatColor.GRAY + " (not logged in)";
                try {
                    PalaceBungee.getMessageHandler().sendStaffMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                PalaceBungee.getMongoHandler().staffClock(player.getUniqueId(), true);
                if (rank.getRankId() >= Rank.TRAINEE.getRankId() && PalaceBungee.getChatUtil().isChatMuted("ParkChat")) {
                    player.sendMessage(ChatColor.RED + "\n\n\nChat is currently muted!\n\n\n");
                }
            }
            if (disable) {
                SlackMessage m = new SlackMessage("");
                SlackAttachment a = new SlackAttachment("*" + rank.getName() + "* `" + player.getUsername() +
                        "` connected from a new IP address `" + player.getAddress() + "`");
                a.color("warning");
                PalaceBungee.getSlackUtil().sendDashboardMessage(m, Collections.singletonList(a), false);
                player.sendMessage(ChatColor.YELLOW + "\n\n" + ChatColor.BOLD +
                        "You connected with a new IP address, type " + ChatColor.GREEN + "" + ChatColor.BOLD +
                        "/staff login [password]" + ChatColor.YELLOW + "" + ChatColor.BOLD + " to verify your account.\n");
            }
            try {
                HashMap<UUID, String> requests = PalaceBungee.getMongoHandler().getFriendRequestList(player.getUniqueId());
                if (requests.size() > 0) {
                    player.sendMessage(ChatColor.AQUA + "You have " + ChatColor.YELLOW + "" + ChatColor.BOLD +
                            requests.size() + " " + ChatColor.AQUA +
                            "pending friend request" + (requests.size() > 1 ? "s" : "") + "! View them with " +
                            ChatColor.YELLOW + ChatColor.BOLD + "/friend requests");
                }
                HashMap<UUID, String> friends = PalaceBungee.getMongoHandler().getFriendList(player.getUniqueId());
                if (friends.size() > 0) {
                    PalaceBungee.getMessageHandler().sendMessage(new FriendJoinPacket(player.getUniqueId(), rank.getTagColor() + player.getUsername(),
                            new ArrayList<>(friends.keySet()), true, rank.getRankId() >= Rank.CHARACTER.getRankId()), PalaceBungee.getMessageHandler().ALL_PROXIES);
                }
            } catch (Exception e) {
                PalaceBungee.getProxyServer().getLogger().log(Level.SEVERE, "Error sending friend/request notifications", e);
            }
        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        ProxiedPlayer pl = event.getPlayer();
        try {
            Party party = PalaceBungee.getMongoHandler().getPartyByLeader(pl.getUniqueId());
            if (party != null) {
                party.messageAllMembers("The party has been closed because " + pl.getName() + " has disconnected!", true, false);
                PalaceBungee.getPartyUtil().closeParty(party);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Player player = PalaceBungee.getPlayer(pl.getUniqueId());
        PalaceBungee.logout(pl.getUniqueId(), player);
        PalaceBungee.getChatUtil().logout(pl.getUniqueId());
        PalaceBungee.getMongoHandler().staffClock(pl.getUniqueId(), false);
        if (player != null) {
            if (player.getRank().getRankId() >= Rank.CHARACTER.getRankId()) {
                try {
                    PalaceBungee.getMessageHandler().sendStaffMessage(player.getRank().getFormattedName() + " " + ChatColor.YELLOW + player.getUsername() + " has clocked out");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                PalaceBungee.getMessageHandler().sendMessage(new FriendJoinPacket(player.getUniqueId(), player.getRank().getTagColor() + player.getUsername(),
                        new ArrayList<>(player.getFriends().keySet()), false, player.getRank().getRankId() >= Rank.CHARACTER.getRankId()), PalaceBungee.getMessageHandler().ALL_PROXIES);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
