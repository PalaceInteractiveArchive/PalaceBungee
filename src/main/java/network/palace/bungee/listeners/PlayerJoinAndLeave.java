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
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.RankTag;
import org.bson.Document;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class PlayerJoinAndLeave implements Listener {

    @EventHandler
    public void onPlayerJoin(LoginEvent event) {
        PendingConnection connection = event.getConnection();
        String address = ((InetSocketAddress) connection.getSocketAddress()).getAddress().toString().replaceAll("/", "");

        Document doc = PalaceBungee.getMongoHandler().getPlayer(connection.getUniqueId(), new Document("rank", true).append("tags", true).append("online", true));

        if (doc != null && doc.containsKey("online") && doc.getBoolean("online")) {
            event.setCancelled(true);
            event.setCancelReason(new ComponentBuilder("This account is already connected to this server!").color(ChatColor.RED).create());
            return;
        }

        Player player;
        if (doc == null) {
            // new player
            player = null;
        } else {
            List<RankTag> tagList = new ArrayList<>();
            if (doc.containsKey("tags")) {
                var tags = doc.get("tags", ArrayList.class);
                for (Object s : tags) {
                    RankTag tag = RankTag.fromString((String) s);
                    if (tag != null) tagList.add(tag);
                }
            }
            player = new Player(connection.getUniqueId(), connection.getName(), Rank.fromString(doc.getString("rank")), tagList, address, connection.getVersion());
        }
        PalaceBungee.login(player);
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer pl = event.getPlayer();
        Player player = PalaceBungee.getPlayer(pl.getUniqueId());
        if (player == null) {
            pl.disconnect(TextComponent.fromLegacyText(ChatColor.RED + "We are currently experiencing some server-side issues. Please check back soon!"));
        } else {
            player.setProxiedPlayer(pl);
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
        Player player = PalaceBungee.getPlayer(pl.getUniqueId());
        if (player != null) {
            PalaceBungee.logout(player.getUniqueId());
            if (player.getRank().getRankId() >= Rank.SPECIALGUEST.getRankId()) {
                try {
                    PalaceBungee.getMessageHandler().sendStaffMessage(player.getRank().getFormattedName() + " " + ChatColor.YELLOW + player.getUsername() + " has clocked out");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
