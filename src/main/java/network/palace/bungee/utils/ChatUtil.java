package network.palace.bungee.utils;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.RankTag;
import network.palace.bungee.handlers.Server;
import network.palace.bungee.messages.packets.ChatAnalysisPacket;
import network.palace.bungee.messages.packets.ChatAnalysisResponsePacket;
import network.palace.bungee.messages.packets.ChatPacket;
import network.palace.bungee.messages.packets.MessageByRankPacket;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ChatUtil {
    @Getter @Setter private boolean parkChatMuted = false;
    private final HashMap<UUID, ChatAnalysisPacket> analysisPackets = new HashMap<>();

    public ChatUtil() {
        BungeeCord.getInstance().getScheduler().schedule(PalaceBungee.getInstance(), () -> {
            List<UUID> toRemove = new ArrayList<>();
            long currentTime = System.currentTimeMillis();
            for (Map.Entry<UUID, ChatAnalysisPacket> entry : analysisPackets.entrySet()) {
                if (currentTime - entry.getValue().getCreated() >= 3000) {
                    ChatAnalysisPacket packet = entry.getValue();
                    Player player = PalaceBungee.getPlayer(packet.getSender());
                    if (player != null) {
                        player.sendMessage(ChatColor.RED + "There was an error sending your chat message! Please try again in a few minutes. If the issue continues, try logging out and back in.");
                    }
                    toRemove.add(entry.getKey());
                }
            }
            toRemove.forEach(analysisPackets::remove);
        }, 5000, 500, TimeUnit.MILLISECONDS);
    }

    public void processIncomingChatMessage(ChatPacket packet) throws Exception {
        UUID sender = packet.getSender();
        BaseComponent[] message = packet.getMessage();
        String channel = packet.getChannel();
        if (channel.equals("ParkChat")) {
            for (Player player : PalaceBungee.getOnlinePlayers()) {
                player.sendMessage(message);
            }
        }
    }

    public void sendOutgoingParkChatMessage(Player player, String message) throws Exception {
        if (parkChatMuted && player.getRank().getRankId() < Rank.TRAINEE.getRankId()) {
            player.sendMessage(ChatColor.RED + "Chat is currently muted!");
            return;
        }
        Rank rank = player.getRank();
        String msg = player.getRank().getRankId() >= Rank.MOD.getRankId() ? ChatColor.translateAlternateColorCodes('&', message) : message;

        String serverName;
        Server s;
        if ((s = PalaceBungee.getServerUtil().getServer(player.getServerName(), true)) != null) {
            serverName = s.getServerType();
        } else {
            serverName = player.getServerName();
        }

        analyzeMessage(player.getUniqueId(), player.getRank(), msg, player.getServerName(), () -> {
            BaseComponent[] components = new ComponentBuilder("[").color(ChatColor.WHITE).event(getPlayerHover(player, player.getServerName()))
                    .append(serverName).color(ChatColor.GREEN)
                    .append("] ").color(ChatColor.WHITE)
                    .append(RankTag.format(player.getTags()))
                    .append(rank.getFormattedName() + " ")
                    .append(player.getUsername() + ": ").color(ChatColor.GRAY)
                    .append(msg, ComponentBuilder.FormatRetention.NONE).color(rank.getChatColor()).create();

            ChatPacket packet = new ChatPacket(player.getUniqueId(), components, "ParkChat");
            try {
                PalaceBungee.getMessageHandler().sendMessage(packet, PalaceBungee.getMessageHandler().ALL_PROXIES);
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "There was an error sending your chat message! Please try again in a few minutes. If the issue continues, try logging out and back in.");
            }
        });
    }

    public void analyzeMessage(UUID uuid, Rank rank, String message, String server, Runnable callback) throws Exception {
        ChatAnalysisPacket packet = new ChatAnalysisPacket(uuid, PalaceBungee.getProxyID(), rank, message, server, callback);

        PalaceBungee.getMessageHandler().sendMessage(packet, PalaceBungee.getMessageHandler().CHAT_ANALYSIS);

        analysisPackets.put(packet.getRequestId(), packet);
    }

    private HoverEvent getPlayerHover(Player player, String server) {
        ComponentBuilder builder = new ComponentBuilder(player.getRank().getFormattedName())
                .append(" " + player.getUsername() + "\n").color(ChatColor.GRAY);
        for (RankTag tag : player.getTags()) {
            builder.append(tag.getName() + "\n").color(tag.getColor()).italic(true);
        }
        builder.append("Server: ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.AQUA).append(server).color(ChatColor.GREEN);
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, builder.create());
    }

    public void handleAnalysisResponse(ChatAnalysisResponsePacket packet) {
        ChatAnalysisPacket originalRequest = analysisPackets.remove(packet.getRequestId());
        if (originalRequest == null) {
            PalaceBungee.getProxyServer().getLogger().warning("Received chat analysis response for unknown request! Sent to wrong proxy?");
            return;
        }
        if (packet.isOkay()) {
            // Empty message field means it passed analysis
            originalRequest.getCallback().run();
        } else {
            // A response message was provided and should be sent to the message sender
            Player player = PalaceBungee.getPlayer(originalRequest.getSender());
            if (player == null) return;

            player.sendMessage(new ComponentBuilder("Your chat message was blocked for the following reason: ").color(ChatColor.RED)
                    .append(packet.getFilterCaught() + "\n").color(ChatColor.AQUA)
                    .append("Please review our server rules at ").color(ChatColor.RED)
                    .append("palnet.us/rules").color(ChatColor.AQUA)
                    .create());

            try {
                MessageByRankPacket chatPacket = new MessageByRankPacket("[" + ChatColor.RED + "CHAT" +
                        ChatColor.WHITE + "] " + ChatColor.GREEN + "Message from " + ChatColor.AQUA + player.getUsername() +
                        ChatColor.GREEN + " blocked: " + ChatColor.RED + packet.getFilterCaught() + ", " + ChatColor.AQUA + "'" +
                        originalRequest.getMessage() + "', " + ChatColor.RED + "'" + packet.getOffendingText() + "'", Rank.TRAINEE, null, false);
                PalaceBungee.getMessageHandler().sendMessage(chatPacket, PalaceBungee.getMessageHandler().ALL_PROXIES);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
