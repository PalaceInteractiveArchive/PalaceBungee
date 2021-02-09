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
import net.md_5.bungee.api.scheduler.ScheduledTask;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.moderation.Mute;
import network.palace.bungee.messages.packets.MQPacket;
import network.palace.bungee.messages.packets.MentionPacket;
import network.palace.bungee.utils.LinkUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class Player {
    private final UUID uuid;
    @Getter @Setter private boolean disabled = false;
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
    private ScheduledTask tutorial = null;

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

    public void runTutorial() {
        tutorial = PalaceBungee.getProxyServer().getScheduler().schedule(PalaceBungee.getInstance(), new Runnable() {
            int i = 0;

            @Override
            public void run() {
                switch (i) {
                    case 0: {
                        sendMessage(ChatColor.GREEN + "\nWelcome to the " + ChatColor.AQUA + "Palace Network" +
                                ChatColor.GREEN + ", we're happy you're here!");
                        mention();
                        break;
                    }
                    case 4: {
                        sendMessage(ChatColor.GREEN + "\nWe are an all-inclusive family-friendly " +
                                ChatColor.DARK_GREEN + "Minecraft " + ChatColor.GREEN + "gaming network!");
                        mention();
                        break;
                    }
                    case 7: {
                        sendMessage(ChatColor.GREEN + "\nRight now you're at the " + ChatColor.AQUA +
                                "Hub. " + ChatColor.GREEN + "From here, you can get to all of the different parts of our network.");
                        mention();
                        break;
                    }
                    case 15: {
                        sendMessage(ChatColor.GREEN + "\nArcade Games, Theme Parks, and a Creative server to name a few.");
                        mention();
                        break;
                    }
                    case 21: {
                        sendMessage(ChatColor.GREEN + "\nYou can also use your " + ChatColor.AQUA +
                                "Navigation Star " + ChatColor.GREEN + "to get to the different parts of our server.");
                        mention();
                        break;
                    }
                    case 28: {
                        sendMessage(ChatColor.GREEN + "\nInstall our Resource Pack for the " +
                                ChatColor.AQUA + "best " + ChatColor.GREEN +
                                "experience possible! All you have to do is type " + ChatColor.AQUA +
                                "/pack " + ChatColor.GREEN + "on a Park server and select " + ChatColor.DARK_GREEN +
                                "Yes. " + ChatColor.GRAY + "" + ChatColor.ITALIC +
                                "(You can set this up when the tutorial finishes)");
                        mention();
                        break;
                    }
                    case 36: {
                        sendMessage(ChatColor.GREEN + "\nAlso, connect to our " + ChatColor.BLUE +
                                "Audio Server " + ChatColor.GREEN + "for an immersive experience! You will hear the " +
                                ChatColor.AQUA + "sounds from rides, music from shows, and so much more! " +
                                ChatColor.GREEN + "Just type " + ChatColor.AQUA + "/audio " + ChatColor.GREEN +
                                "and click the message to connect. " + ChatColor.GRAY + "" + ChatColor.ITALIC +
                                "(You can set this up when the tutorial finishes)");
                        mention();
                        break;
                    }
                    case 49: {
                        sendMessage(ChatColor.GREEN + "\nBefore you start exploring, please take a " +
                                "few minutes to review our rules: " + ChatColor.AQUA +
                                "palace.network/rules " + ChatColor.GREEN + "\nWe are a " +
                                "family-friendly server with a goal of providing a safe, fun experience " +
                                "to all of our settlers.");
                        mention();
                        break;
                    }
                    case 58: {
                        sendMessage(ChatColor.GREEN + "\nAfter you finish reviewing our rules, " +
                                "you're finished with the tutorial! " + ChatColor.DARK_AQUA +
                                "Note: New settlers must wait " + ChatColor.BOLD + "10 minutes " +
                                ChatColor.DARK_AQUA + "before using chat. Read why: " +
                                ChatColor.AQUA + "palace.network/rules#chat");
                        mention();
                        setNewGuest(false);
                        PalaceBungee.getMongoHandler().completeTutorial(getUniqueId());
                        cancelTutorial();
                    }
                }
                i++;
            }
        }, 2, 1, TimeUnit.SECONDS);
    }

    public void cancelTutorial() {
        tutorial.cancel();
    }
}