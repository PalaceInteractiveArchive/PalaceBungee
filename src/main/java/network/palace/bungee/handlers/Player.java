package network.palace.bungee.handlers;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.utils.LinkUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Marc on 7/16/16
 */
@RequiredArgsConstructor
public class Player {
    private final UUID uuid;
    @Getter private final String username;
    @Getter @Setter @NonNull private Rank rank;
    @NonNull private List<RankTag> tags;
    @Getter private final String address;
    @Getter private final long created = System.currentTimeMillis();
    @Getter private boolean kicking = false;
    @Getter private final int protocolVersion;
    @Getter @Setter private ProxiedPlayer proxiedPlayer;

    public List<RankTag> getTags() {
        return new ArrayList<>(tags);
    }

    public void addTag(RankTag tag) {
        tags.add(tag);
    }

    public boolean removeTag(RankTag tag) {
        return tags.remove(tag);
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public void sendMessage(String message) {
        ProxiedPlayer p = PalaceBungee.getProxyServer().getPlayer(uuid);
        if (p != null) {
            p.sendMessage(LinkUtil.fromString(message));
        }
    }

    public void sendMessage(TextComponent message) {
        ProxiedPlayer p = PalaceBungee.getProxyServer().getPlayer(uuid);
        if (p != null) {
            p.sendMessage(LinkUtil.fromComponent(message));
        }
    }

    public void sendMessage(final BaseComponent[] components) {
        ProxiedPlayer p = PalaceBungee.getProxyServer().getPlayer(uuid);
        for (int i = 0; i < components.length; i++) {
            components[i] = LinkUtil.fromComponent(components[i]);
        }
        if (p != null) {
            p.sendMessage(components);
        }
    }

    public void kickPlayer(String reason) {
        kickPlayer(reason, true);
    }

    public void kickPlayer(String reason, boolean prefix) {
        if (kicking) {
            return;
        }
        kicking = true;
        String pre = "You have been disconnected for: ";
        if (!prefix) {
            pre = "";
        }
        BaseComponent[] r = new ComponentBuilder(pre).color(ChatColor.RED)
                .append(reason).color(ChatColor.AQUA).create();
        PalaceBungee.getProxyServer().getPlayer(uuid).disconnect(r);
    }

    public void kickPlayer(TextComponent reason) {
        if (kicking) {
            return;
        }
        kicking = true;
        PalaceBungee.getProxyServer().getPlayer(uuid).disconnect(reason);
    }

    public void kickPlayer(BaseComponent[] reason) {
        if (kicking) {
            return;
        }
        kicking = true;
        PalaceBungee.getProxyServer().getPlayer(uuid).disconnect(reason);
    }

    public Server getServer() {
        ProxiedPlayer p = PalaceBungee.getProxyServer().getPlayer(uuid);
        if (p != null) {
            return p.getServer();
        } else {
            return null;
        }
    }

    public String getServerName() {
        try {
            ServerInfo info = getServer().getInfo();
            return info.getName();
        } catch (Exception e) {
            return "Unknown";
        }
    }

    public void chat(String msg) {
        ProxiedPlayer p = PalaceBungee.getProxyServer().getPlayer(uuid);
        if (p != null) {
            p.chat(msg);
        }
    }
}