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
import network.palace.bungee.messages.packets.MentionPacket;
import network.palace.bungee.utils.LinkUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class Player {
    private final UUID uuid;
    @Getter private final String username;
    @Getter @Setter @NonNull private Rank rank;
    @NonNull private List<RankTag> tags;
    @Getter private final String address;
    @Getter private final long loginTime = System.currentTimeMillis();
    @Getter private Mute mute;
    @Getter private boolean kicking = false;
    @Getter private final int protocolVersion;
    @Setter private ProxiedPlayer proxiedPlayer;

    @Getter @Setter private String channel = "all";
    @Getter @Setter private boolean dmEnabled = true;
    @Getter private final List<UUID> ignored = new ArrayList<>();

    // The UUID of the player that messaged them last
    @Getter @Setter private UUID replyTo = null;
    // The last time the player had their replyTo value updated
    @Getter @Setter private long replyTime = 0;
    // Whether the player has mention pings enabled
    @NonNull @Setter private boolean mentions;

    public boolean isIgnored(UUID uuid) {
        return ignored.contains(uuid);
    }

    public void setIgnored(UUID uuid, boolean b) {
        if (b) ignored.add(uuid);
        else ignored.remove(uuid);
    }

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

    public ProxiedPlayer getProxiedPlayer() {
        if (proxiedPlayer == null) this.proxiedPlayer = PalaceBungee.getProxyServer().getPlayer(uuid);
        return proxiedPlayer;
    }

    public void sendMessage(String message) {
        getProxiedPlayer().sendMessage(LinkUtil.fromString(message));
    }

    public void sendMessage(TextComponent message) {
        getProxiedPlayer().sendMessage(LinkUtil.fromComponent(message));
    }

    public void sendMessage(BaseComponent[] components) {
        sendMessage(components, true);
    }

    public void sendMessage(final BaseComponent[] components, boolean parseLinks) {
        if (parseLinks) {
            for (int i = 0; i < components.length; i++) {
                components[i] = LinkUtil.fromComponent(components[i]);
            }
        }
        getProxiedPlayer().sendMessage(components);
    }

    public void sendSubsystemMessage(Subsystem subsystem, String message) {
        sendMessage(subsystem.getPrefix() + message);
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
        getProxiedPlayer().disconnect(r);
    }

    public void kickPlayer(TextComponent reason) {
        if (kicking) {
            return;
        }
        kicking = true;
        getProxiedPlayer().disconnect(reason);
    }

    public void kickPlayer(BaseComponent[] reason) {
        if (kicking) {
            return;
        }
        kicking = true;
        getProxiedPlayer().disconnect(reason);
    }

    public Server getServer() {
        return getProxiedPlayer().getServer();
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

    public boolean hasMentions() {
        return mentions;
    }

    public void mention() {
        if (!hasMentions()) return;
        try {
            PalaceBungee.getMessageHandler().sendMessage(new MentionPacket(uuid), "mc_direct", "direct", getServerName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}