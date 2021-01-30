package network.palace.bungee.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.RankTag;
import network.palace.bungee.handlers.Server;
import network.palace.bungee.messages.packets.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ChatUtil {
    @Getter @Setter private boolean parkChatMuted = false;
    private final HashMap<UUID, ChatAnalysisPacket> analysisPackets = new HashMap<>();
    private final HashMap<UUID, ChatMessage> messageCache = new HashMap<>();

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

    public void muteChat(String server) throws Exception {
        List<String> mutedChats = PalaceBungee.getConfigUtil().getMutedChats();
        if (!mutedChats.contains(server)) {
            mutedChats.add(server);
            PalaceBungee.getConfigUtil().setMutedChats(mutedChats, true);
            PalaceBungee.getMessageHandler().sendMessage(new ChatMutePacket(server, true), PalaceBungee.getMessageHandler().ALL_PROXIES);
        }
//        if (server.equals("Creative")) {
//            PacketMuteChat packet = new PacketMuteChat(server, true, "");
//            sendToServer(server, packet);
//        }
    }

    public void unmuteChat(String server) throws Exception {
        List<String> mutedChats = PalaceBungee.getConfigUtil().getMutedChats();
        if (mutedChats.contains(server)) {
            mutedChats.remove(server);
            PalaceBungee.getConfigUtil().setMutedChats(mutedChats, true);
            PalaceBungee.getMessageHandler().sendMessage(new ChatMutePacket(server, false), PalaceBungee.getMessageHandler().ALL_PROXIES);
        }
//        if (server.equals("Creative")) {
//            PacketMuteChat packet = new PacketMuteChat(server, false, "");
//            sendToServer(server, packet);
//        }
    }

    public boolean isChatMuted(String server) {
        return PalaceBungee.getConfigUtil().getMutedChats().contains(server);
    }

    public boolean chatEvent(Player player, String msg, boolean command) throws Exception {
        switch (player.getChannel()) {
            case "party": {
                PalaceBungee.getProxyServer().getPluginManager().dispatchCommand(player.getProxiedPlayer(), "pchat " + msg);
                return true;
            }
            case "staff": {
                PalaceBungee.getProxyServer().getPluginManager().dispatchCommand(player.getProxiedPlayer(), "sc " + msg);
                return true;
            }
            case "admin": {
                PalaceBungee.getProxyServer().getPluginManager().dispatchCommand(player.getProxiedPlayer(), "ho " + msg);
                return true;
            }
        }

//        if (player.isNewGuest()) TODO new players need chat disabled, allow commands

        Rank rank = player.getRank();
        String serverName = player.getServerName();
        Server server = PalaceBungee.getServerUtil().getServer(serverName, true);
        if (server == null) return true;
        String channel = server.isPark() ? "ParkChat" : serverName;

        if (rank.getRankId() >= Rank.TRAINEE.getRankId()) {
            // TODO AFK timer
        }

//        if (player.isDisabled()) TODO staff members can be disabled until they enter their password

        if (command) {
            String s = msg.toLowerCase().replaceFirst("/", "");
            if (player.getRank().getRankId() < Rank.TRAINEEBUILD.getRankId() && (s.startsWith("/calc") || s.startsWith("/calculate") ||
                    s.startsWith("/eval") || s.startsWith("/evaluate") || s.startsWith("/solve") ||
                    s.startsWith("worldedit:/calc") || s.startsWith("worldedit:/calculate") ||
                    s.startsWith("worldedit:/eval") || s.startsWith("worldedit:/evaluate") ||
                    s.startsWith("worldedit:/solve"))) {
                player.sendMessage(ChatColor.RED + "That command is disabled.");
                return true;
            }
            return false;
        }

        String processed = processChatMessage(player, msg, channel);
        if (processed == null) return true;

        analyzeMessage(player.getUniqueId(), player.getRank(), processed, player.getServerName(), () -> {
            messageCache.put(player.getUniqueId(), new ChatMessage(player.getUniqueId(), processed));

            String emoji;
            try {
                emoji = EmojiUtil.convertMessage(player, processed);
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.RED + e.getMessage());
                return;
            }

            // TODO Log chat message
            if (channel.equals("ParkChat")) {
                try {
                    sendOutgoingParkChatMessage(player, emoji);
                } catch (Exception e) {
                    e.printStackTrace();
                    player.sendMessage(ChatColor.RED + "There was an error sending your chat message! Please try again in a few minutes. If the issue continues, try logging out and back in.");
                }
            } else {
                player.chat(emoji);
            }
        });
        return true;
    }

    /**
     * Process chat message locally
     *
     * @param player  the player
     * @param msg     the message
     * @param channel the chat channel
     * @return the message (modified if necessary), or null if the message should be blocked
     * @implNote this method will return null if the message should be blocked
     */
    public String processChatMessage(Player player, String msg, String channel) {
        return processChatMessage(player, msg, channel, false);
    }


    /**
     * Process chat message locally
     *
     * @param player      the player
     * @param msg         the message
     * @param channel     the chat channel
     * @param ignoreMuted whether to ignore the player's mute state
     * @return the message (modified if necessary), or null if the message should be blocked
     * @implNote this method will return null if the message should be blocked
     */
    public String processChatMessage(Player player, String msg, String channel, boolean ignoreMuted) {
        // Remove multiple spaces between words
        msg = msg.replaceAll(" +", " ");

        if (player.isMuted() && !ignoreMuted) {
            player.sendMessage(PalaceBungee.getModerationUtil().getMuteMessage(player.getMute()));
            return null;
        }

//        if (player.getRank().getRankId() < Rank.CHARACTER.getRankId()) {
        if (isChatMuted(channel) && !channel.equals("Creative")) {
            player.sendMessage(ChatColor.RED + "Chat is currently muted!");
            return null;
        }

        if (strictModeCheck(msg)) {
            player.sendMessage(ChatColor.RED + "Your message was similar to another recently said in chat and was marked as spam. We apologize if this was done in error, we're constantly improving our chat filter.");
            try {
                PalaceBungee.getModerationUtil().announceSpamMessage(player.getUsername(), msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        if (System.currentTimeMillis() - player.getLastChatMessage() < (PalaceBungee.getConfigUtil().getChatDelay() * 1000L)) {
            player.sendMessage(ChatColor.RED + "You must wait " + PalaceBungee.getConfigUtil().getChatDelay() + " seconds before chatting!");
            return null;
        }
        player.setLastChatMessage(System.currentTimeMillis());

        msg = removeCaps(player, msg);

        if (messageCache.containsKey(player.getUniqueId())) {
            ChatMessage cachedMessage = messageCache.get(player.getUniqueId());
            //Block saying the same message within a minute
            if ((System.currentTimeMillis() - cachedMessage.getTime() < 60 * 1000) && msg.equalsIgnoreCase(cachedMessage.getMessage())) {
                player.sendMessage(ChatColor.RED + "Please do not repeat the same message!");
                return null;
            }
        }
//        } else {
//            if (msg.startsWith(":warn-")) {
////                dashboard.getWarningUtil().handle(player, msg.toString());
//                return null;
//            }
//        }

        return msg;
    }

    private String removeCaps(Player player, String msg) {
        int size = msg.toCharArray().length;
        if (size < 10) {
            return msg;
        }
        int amount = 0;
        for (char c : msg.toCharArray()) {
            if (Character.isUpperCase(c)) {
                amount++;
            }
        }
        if (Math.floor(100 * (((float) amount) / size)) >= 50.0) {
            player.sendMessage(ChatColor.RED + "Please do not use excessive capitals in your messages.");
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < msg.length(); i++) {
                if (i == 0) {
                    s.append(msg.charAt(0));
                    continue;
                }
                s.append(Character.toLowerCase(msg.charAt(i)));
            }
            return s.toString();
        }
        return msg;
    }

    public void analyzeMessage(UUID uuid, Rank rank, String message, String server, Runnable callback) throws Exception {
        ChatAnalysisPacket packet = new ChatAnalysisPacket(uuid, PalaceBungee.getProxyID(), rank, message, server, callback);

        PalaceBungee.getMessageHandler().sendMessage(packet, PalaceBungee.getMessageHandler().CHAT_ANALYSIS);

        analysisPackets.put(packet.getRequestId(), packet);
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
                        originalRequest.getMessage() + "', " + ChatColor.RED + "'" + packet.getOffendingText() + "'", Rank.TRAINEE, null, false, false);
                PalaceBungee.getMessageHandler().sendMessage(chatPacket, PalaceBungee.getMessageHandler().ALL_PROXIES);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void handleIncomingChatPacket(ChatPacket packet) throws Exception {
        UUID sender = packet.getSender();
        BaseComponent[] message = packet.getMessage();
        String plainText = ChatColor.stripColor(TextComponent.toPlainText(message)).toLowerCase();
        String channel = packet.getChannel();
        if (channel.equals("ParkChat")) {
            for (Player player : PalaceBungee.getOnlinePlayers()) {
                if (plainText.matches("(.* )?" + player.getUsername().toLowerCase() + "([.,! ].*)?")) {
                    player.sendMessage(new ComponentBuilder("* ").color(ChatColor.BLUE).append(message).create());
                    player.mention();
                } else {
                    player.sendMessage(message);
                }
            }
        }
    }

    public boolean strictModeCheck(String message) {
        //TODO need to find way to distribute this system in the future
        try {
            if (PalaceBungee.getConfigUtil().isStrictChat() && !messageCache.isEmpty() && message.length() >= 10) {
                ChatMessage chatMessage = null;
                for (ChatMessage cached : messageCache.values()) {
                    if (chatMessage == null) {
                        chatMessage = cached;
                        continue;
                    }
                    if (cached.getTime() > chatMessage.getTime()) {
                        chatMessage = cached;
                    }
                }

                //Only strict-check messages said within the last 10 seconds
                if (chatMessage != null && System.currentTimeMillis() - chatMessage.getTime() < 10 * 1000) {
                    String lastMessage = chatMessage.getMessage();
                    double distance = PalaceBungee.getChatAlgorithm().similarity(PalaceBungee.getConfigUtil().getStrictThreshold(), message, lastMessage);
                    return distance >= PalaceBungee.getConfigUtil().getStrictThreshold();
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Getter
    @AllArgsConstructor
    public static class ChatMessage {
        private final UUID uuid;
        private final String message;
        private final long time = System.currentTimeMillis();
    }
}
