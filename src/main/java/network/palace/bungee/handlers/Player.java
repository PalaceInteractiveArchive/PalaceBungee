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
import network.palace.bungee.handlers.moderation.Mute;
import network.palace.bungee.messages.packets.MQPacket;
import network.palace.bungee.messages.packets.MentionPacket;
import network.palace.bungee.utils.LinkUtil;

import java.util.*;

@RequiredArgsConstructor
public class Player {
    private final UUID uuid;
    @Getter @Setter private boolean disabled = false;
    //TODO add tutorial for new players, which changes newGuest to false when done
    @Getter @Setter private boolean newGuest = false;
    @Getter private final String username;
    @Getter @Setter @NonNull private Rank rank;
    @NonNull private final List<RankTag> tags;
    @Getter private final String address;
    @Getter @Setter private String isp;
    @Getter private final long loginTime = System.currentTimeMillis();
    @Getter @Setter private long onlineTime = 0;
    @Getter @Setter private Mute mute = null;
    @Getter private boolean kicking = false;
    @Getter private final int mcVersion;
    @Setter private ProxiedPlayer proxiedPlayer;

    @Getter @Setter private String channel = "all";
    @Getter @Setter private boolean dmEnabled = true;
    @Getter private final List<UUID> ignored = new ArrayList<>();
    @Getter @Setter private long lastChatMessage = 0;

    @Getter private long afkTime = System.currentTimeMillis();
    @Getter @Setter private boolean isAFK = false;
    @Getter private final List<Timer> afkTimers = new ArrayList<>();

    @Getter @Setter private boolean friendRequestToggle = true;

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
            sendPacket(new MentionPacket(uuid), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void afkAction() {
        afkTime = System.currentTimeMillis();
    }

    public boolean hasTag(RankTag tag) {
        return tags.contains(tag);
    }

    public boolean isMuted() {
        if (mute == null || !mute.isMuted()) return false;
        long expires = mute.getExpires();
        if (System.currentTimeMillis() < mute.getExpires()) {
            return true;
        } else {
            PalaceBungee.getMongoHandler().unmutePlayer(uuid);
            return false;
        }
    }

    /**
     * Send a packet to the proxy this player is connected to
     *
     * @param packet   the packet to be sent
     * @param mcServer true if packet should be sent directly to the player's MC server, false if should be sent to proxy
     * @throws Exception if there's an issue communicating over the message queue
     */
    public void sendPacket(MQPacket packet, boolean mcServer) throws Exception {
        if (mcServer) {
            PalaceBungee.getMessageHandler().sendMessage(packet, "mc_direct", "direct", getServerName());
        } else {
            UUID targetProxy = PalaceBungee.getMongoHandler().findPlayer(uuid);
            if (targetProxy != null) PalaceBungee.getMessageHandler().sendToProxy(packet, targetProxy);
        }
    }

    public HashMap<UUID, String> getFriends() {
        return PalaceBungee.getMongoHandler().getFriendList(uuid);
    }

    public HashMap<UUID, String> getRequests() {
        return PalaceBungee.getMongoHandler().getFriendRequestList(uuid);
    }

    public boolean hasFriendToggledOff() {
        return friendRequestToggle;
    }

    public long getTotalOnlineTime() {
        return ((System.currentTimeMillis() - loginTime) / 1000) + onlineTime;
    }
}