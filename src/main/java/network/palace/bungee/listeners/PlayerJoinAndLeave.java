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
import network.palace.bungee.handlers.Party;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.RankTag;
import network.palace.bungee.handlers.moderation.AddressBan;
import network.palace.bungee.handlers.moderation.Ban;
import network.palace.bungee.handlers.moderation.ProviderBan;
import network.palace.bungee.handlers.moderation.ProviderData;
import network.palace.bungee.utils.IPUtil;
import org.bson.Document;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class PlayerJoinAndLeave implements Listener {

    @EventHandler
    public void onPlayerJoin(LoginEvent event) {
        PendingConnection connection = event.getConnection();
        String address = ((InetSocketAddress) connection.getSocketAddress()).getAddress().toString().replaceAll("/", "");

        Document doc = PalaceBungee.getMongoHandler().getPlayer(connection.getUniqueId(), new Document("rank", true).append("tags", true).append("online", true).append("settings", true));

        if (doc != null && doc.containsKey("online") && doc.getBoolean("online")) {
            event.setCancelled(true);
            event.setCancelReason(new ComponentBuilder("This account is already connected to this server!").color(ChatColor.RED).create());
            return;
        }

        Player player;
        Rank rank;
        if (doc == null) {
            // new player
            player = null;
            rank = Rank.SETTLER;
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
            assert player != null;
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
            if (player.getRank().getRankId() >= Rank.SPECIALGUEST.getRankId()) {
                try {
                    PalaceBungee.getMessageHandler().sendStaffMessage(player.getRank().getFormattedName() + " " + ChatColor.YELLOW + player.getUsername() + " has clocked in");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        ProxiedPlayer pl = event.getPlayer();
        try {
            Party party = PalaceBungee.getMongoHandler().getPartyByLeader(pl.getUniqueId());
            if (party != null) {
                party.messageAllMembers("The party has been closed because " + pl.getName() + " has disconnected!", true);
                PalaceBungee.getPartyUtil().closeParty(party);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Player player = PalaceBungee.getPlayer(pl.getUniqueId());
        if (player != null) {
            if (player.getRank().getRankId() >= Rank.SPECIALGUEST.getRankId()) {
                try {
                    PalaceBungee.getMessageHandler().sendStaffMessage(player.getRank().getFormattedName() + " " + ChatColor.YELLOW + player.getUsername() + " has clocked out");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            PalaceBungee.logout(player.getUniqueId());
        }
    }
}
