package network.palace.bungee.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.ComponentSerializer;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.RankTag;
import network.palace.bungee.handlers.Server;
import network.palace.bungee.messages.packets.*;

import java.io.IOException;
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
        String msg = player.getRank().getRankId() >= Rank.CM.getRankId() ? ChatColor.translateAlternateColorCodes('&', message) : message;

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
                .append(" [" + rank.getFormattedName() + "] ")
                .append(player.getUsername() + ": ").color(ChatColor.GRAY)
                .append(msg, ComponentBuilder.FormatRetention.NONE).color(rank.getChatColor()).create();

        ChatPacket packet = new ChatPacket(player.getUniqueId(), player.getRank(), components, "ParkChat");
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

    public void muteChat(String server, String source) throws Exception {
        List<String> mutedChats = PalaceBungee.getConfigUtil().getMutedChats();
        if (!mutedChats.contains(server)) {
            mutedChats.add(server);
            PalaceBungee.getConfigUtil().setMutedChats(mutedChats, true);
            PalaceBungee.getMessageHandler().sendMessage(new ChatMutePacket(server, source, true), PalaceBungee.getMessageHandler().ALL_PROXIES);
            String msg = ChatColor.WHITE + "[" + ChatColor.DARK_AQUA + "Palace Chat" + ChatColor.WHITE + "] " +
                    ChatColor.YELLOW + "Chat has been muted";
            String msgname = msg + " by " + source;
            for (Player tp : PalaceBungee.getOnlinePlayers()) {
                if ((server.equals("ParkChat") && PalaceBungee.getServerUtil().isOnPark(tp)) || tp.getServerName().equals(server)) {
                    tp.sendMessage(tp.getRank().getRankId() >= Rank.TRAINEE.getRankId() ? msgname : msg);
                }
            }
        }
        if (server.equals("Creative")) {
            PalaceBungee.getMessageHandler().sendDirectServerMessage(new ChatMutePacket(server, source, true), "Creative");
        }
    }

    public void unmuteChat(String server, String source) throws Exception {
        List<String> mutedChats = PalaceBungee.getConfigUtil().getMutedChats();
        if (mutedChats.contains(server)) {
            mutedChats.remove(server);
            PalaceBungee.getConfigUtil().setMutedChats(mutedChats, true);
            PalaceBungee.getMessageHandler().sendMessage(new ChatMutePacket(server, source, false), PalaceBungee.getMessageHandler().ALL_PROXIES);
            String msg = ChatColor.WHITE + "[" + ChatColor.DARK_AQUA + "Palace Chat" + ChatColor.WHITE + "] " +
                    ChatColor.YELLOW + "Chat has been unmuted";
            String msgname = msg + " by " + source;
            for (Player tp : PalaceBungee.getOnlinePlayers()) {
                if ((server.equals("ParkChat") && PalaceBungee.getServerUtil().isOnPark(tp)) || tp.getServerName().equals(server)) {
                    tp.sendMessage(tp.getRank().getRankId() >= Rank.TRAINEE.getRankId() ? msgname : msg);
                }
            }
        }
        if (server.equals("Creative")) {
            PalaceBungee.getMessageHandler().sendDirectServerMessage(new ChatMutePacket(server, source, false), "Creative");
        }
    }

    public boolean isChatMuted(String server) {
        return PalaceBungee.getConfigUtil().getMutedChats().contains(server);
    }

    public boolean chatEvent(Player player, String msg, boolean command) throws Exception {
        if (player.isDisabled()) {
            if (command) {
                String m = msg.replaceFirst("/", "");
                return !m.startsWith("staff");
            }
            return true;
        }
        if (player.isNewGuest() && !command) return true;
        Optional<ProxiedPlayer> proxiedPlayer = player.getProxiedPlayer();
        if (proxiedPlayer.isEmpty()) return true;

        if (!command) {
            switch (player.getChannel()) {
                case "party": {
                    PalaceBungee.getProxyServer().getPluginManager().dispatchCommand(proxiedPlayer.get(), "pchat " + msg);
                    return true;
                }
                case "guide": {
                    PalaceBungee.getProxyServer().getPluginManager().dispatchCommand(proxiedPlayer.get(), "gc " + msg);
                    return true;
                }
                case "staff": {
                    PalaceBungee.getProxyServer().getPluginManager().dispatchCommand(proxiedPlayer.get(), "sc " + msg);
                    return true;
                }
                case "admin": {
                    PalaceBungee.getProxyServer().getPluginManager().dispatchCommand(proxiedPlayer.get(), "ho " + msg);
                    return true;
                }
            }
        }

        Rank rank = player.getRank();
        String serverName = player.getServerName();
        Server server = PalaceBungee.getServerUtil().getServer(serverName, true);
        if (server == null) return true;
        String channel = server.isPark() ? "ParkChat" : serverName;

        if (rank.getRankId() >= Rank.TRAINEE.getRankId()) {
            try {
                if (player.isAFK()) {
                    player.setAFK(false);
                    player.getAfkTimers().forEach(Timer::cancel);
                    player.getAfkTimers().clear();
                    player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Your AFK Timer has been reset!");
                    proxiedPlayer.get().sendTitle(
                            BungeeCord.getInstance().createTitle()
                                    .title(new ComponentBuilder("Confirmed").color(ChatColor.GREEN).bold(true).create())
                                    .subTitle(new ComponentBuilder("Your AFK Timer has been reset!").color(ChatColor.GREEN).bold(true).create())
                                    .fadeIn(10)
                                    .stay(100)
                                    .fadeOut(20)
                    );
                    player.afkAction();
                    return true;
                }
                player.afkAction();
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "An error occurred while sending your chat message! Please try again in a few minutes. If the issue continues, try logging out and back in.");
                return true;
            }
        }

        if (command) {
            String s = msg.toLowerCase().replaceFirst("/", "");
            if (player.getRank().getRankId() < Rank.TRAINEEBUILD.getRankId() && (s.startsWith("/calc") || s.startsWith("/calculate") ||
                    s.startsWith("/eval") || s.startsWith("/evaluate") || s.startsWith("/solve") ||
                    s.startsWith("worldedit:/calc") || s.startsWith("worldedit:/calculate") ||
                    s.startsWith("worldedit:/eval") || s.startsWith("worldedit:/evaluate") ||
                    s.startsWith("worldedit:/solve") || s.startsWith("train") || s.startsWith("cart"))) {
                player.sendMessage(ChatColor.RED + "That command is disabled.");
                return true;
            }
            return false;
        }

        if (player.getTotalOnlineTime() < 600) {
            player.sendMessage(ChatColor.RED + "New guests must be on the server for at least 10 minutes before talking in chat." +
                    ChatColor.DARK_AQUA + " Learn more at palnet.us/rules");
            return true;
        }

        String processed = processChatMessage(player, msg, channel);
        if (processed == null) return true;

        analyzeMessage(player.getUniqueId(), player.getRank(), processed, channel, () -> {
            saveMessageCache(player.getUniqueId(), processed);

            String emoji;
            try {
                emoji = EmojiUtil.convertMessage(player, processed);
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.RED + e.getMessage());
                return;
            }

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

    public void saveMessageCache(UUID uuid, String msg) {
        messageCache.put(uuid, new ChatMessage(msg));
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
        return processChatMessage(player, msg, channel, false, true);
    }


    /**
     * Process chat message locally
     *
     * @param player         the player
     * @param msg            the message
     * @param channel        the chat channel
     * @param ignoreMuted    whether to ignore the player's mute state
     * @param checkChatDelay whether to perform the chat delay check
     * @return the message (modified if necessary), or null if the message should be blocked
     * @implNote this method will return null if the message should be blocked
     */
    public String processChatMessage(Player player, String msg, String channel, boolean ignoreMuted, boolean checkChatDelay) {
        // Remove multiple spaces between words
        msg = msg.replaceAll(" +", " ");

        if (player.isMuted() && !ignoreMuted) {
            player.sendMessage(PalaceBungee.getModerationUtil().getMuteMessage(player.getMute()));
            return null;
        }

        if (player.getRank().getRankId() < Rank.CHARACTER.getRankId()) {
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

            if (checkChatDelay && System.currentTimeMillis() - player.getLastChatMessage() < (PalaceBungee.getConfigUtil().getChatDelay() * 1000L)) {
                player.sendMessage(ChatColor.RED + "You must wait " + PalaceBungee.getConfigUtil().getChatDelay() + " seconds before chatting!");
                return null;
            }

            msg = removeCapsAndCheckSpam(player, msg);

            if (msg == null) {
                player.sendMessage(ChatColor.RED + "Please do not spam chat with excessive amounts of characters.");
                return null;
            }
            player.setLastChatMessage(System.currentTimeMillis());

            if (messageCache.containsKey(player.getUniqueId())) {
                ChatMessage cachedMessage = messageCache.get(player.getUniqueId());
                //Block saying the same message within 20 seconds
                if ((System.currentTimeMillis() - cachedMessage.getTime() < 20 * 1000) && msg.equalsIgnoreCase(cachedMessage.getMessage())) {
                    player.sendMessage(ChatColor.RED + "Please do not repeat the same message!");
                    return null;
                }
            }
        }

        return msg;
    }

    /**
     * Check for excessive capitalization and spam characters
     * - If the message is shorter than 10 characters, it is ignored and passes both checks
     * - If the message contains at least 5 of the same character in sequence (ignoring case), it fails the spam check
     * -- 'Hellooooo' fails
     * -- 'Heyyyy thereeee' doesn't fail
     * <p>
     * - If at least 50% of the characters are uppercase, it fails the excessive caps check
     * -- 'WHATS GOING on' fails
     * -- 'HellO THEre' doesn't fail
     *
     * @param player the player
     * @param msg    the message
     * @return null if it failed the spam check, otherwise the String message (potentially with excessive caps removed)
     */
    private String removeCapsAndCheckSpam(Player player, String msg) {
        int size = msg.toCharArray().length;
        if (size < 10) {
            return msg;
        }
        int capsAmount = 0;
        Character last = null;
        int charAmount = 0;
        for (char c : msg.toCharArray()) {
            if (last == null) {
                last = Character.toLowerCase(c);
            } else if (charAmount >= 5) {
                return null;
            }
            if (Character.isUpperCase(c)) {
                capsAmount++;
                if (Character.toLowerCase(c) == last) {
                    charAmount++;
                } else {
                    charAmount = 0;
                    last = null;
                }
            } else if (c == last) {
                charAmount++;
            } else {
                charAmount = 0;
                last = Character.toLowerCase(c);
            }
        }
        if (Math.floor(100 * (((float) capsAmount) / size)) >= 50.0) {
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

    public void analyzeMessage(UUID uuid, Rank rank, String message, String channel, Runnable callback) throws Exception {
        ChatAnalysisPacket packet = new ChatAnalysisPacket(uuid, PalaceBungee.getProxyID(), rank, message, channel, callback);
        analysisPackets.put(packet.getRequestId(), packet);

        if (rank.getRankId() >= Rank.CHARACTER.getRankId()) {
            handleAnalysisResponse(new ChatAnalysisResponsePacket(packet.getRequestId(), true, message));
            return;
        }

        PalaceBungee.getMessageHandler().sendMessage(packet, PalaceBungee.getMessageHandler().CHAT_ANALYSIS);
    }

    public void handleAnalysisResponse(ChatAnalysisResponsePacket packet) {
        ChatAnalysisPacket originalRequest = analysisPackets.remove(packet.getRequestId());
        if (originalRequest == null) {
            PalaceBungee.getProxyServer().getLogger().warning("Received chat analysis response for unknown request! Sent to wrong proxy?");
            return;
        }
        PalaceBungee.getMongoHandler().logChatMessage(originalRequest.getSender(), originalRequest.getMessage(), originalRequest.getChannel(),
                System.currentTimeMillis(), packet.isOkay(), packet.getFilterCaught(), packet.getOffendingText(), originalRequest.getRank().getRankId() >= Rank.CHARACTER.getRankId());
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
                BaseComponent[] components = new ComponentBuilder("[").color(ChatColor.WHITE)
                        .append("CHAT").color(ChatColor.RED)
                        .append("] ").color(ChatColor.WHITE)
                        .append("Message from ").color(ChatColor.GREEN)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to warn this player").color(ChatColor.GREEN).create()))
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warn " + player.getUsername() + " " + getWarningText(packet.getFilterCaught())))
                        .append(player.getUsername()).color(ChatColor.AQUA)
                        .append(" blocked: ").color(ChatColor.GREEN)
                        .append(packet.getFilterCaught() + ", ").color(ChatColor.RED)
                        .append("'" + originalRequest.getMessage() + "', ").color(ChatColor.AQUA)
                        .append("'" + packet.getOffendingText() + "'", ComponentBuilder.FormatRetention.NONE).color(ChatColor.RED)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to visit ").color(ChatColor.GREEN).append(packet.getOffendingText()).color(ChatColor.AQUA)
                                .append("\nBE CAREFUL OF ANY LINKS YOU CLICK!").color(ChatColor.RED).create()))
                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, packet.getOffendingText()))
                        .create();
                MessageByRankPacket chatPacket = new MessageByRankPacket(ComponentSerializer.toString(components), Rank.TRAINEE, null, false, true);
                PalaceBungee.getMessageHandler().sendMessage(chatPacket, PalaceBungee.getMessageHandler().ALL_PROXIES);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getWarningText(String filterCaught) {
        switch (filterCaught) {
            case "inappropriate content":
                return "Please keep chat appropriate.";
            case "link sharing":
                return "Please do not advertise or share links.";
//            case "blocked character":
//            case "blocked characters":
//                return "";
            default:
                return "";
        }
    }

    public void socialSpyMessage(UUID sender, UUID receiver, String from, String to, String channel, String message, String command) throws IOException {
        PalaceBungee.getMessageHandler().sendMessage(new SocialSpyPacket(sender, receiver,
                ChatColor.WHITE + from + ": /" + command + " " + to + " " + message,
                channel), PalaceBungee.getMessageHandler().ALL_PROXIES);
    }

    public void socialSpyParty(UUID sender, String from, String leader, String channel, String message) throws IOException {
        PalaceBungee.getMessageHandler().sendMessage(new SocialSpyPacket(sender, null,
                ChatColor.BOLD + "" + ChatColor.YELLOW + "[P] " + ChatColor.LIGHT_PURPLE + from + ": /pchat" + " " + leader + " " + message,
                channel), PalaceBungee.getMessageHandler().ALL_PROXIES);
    }

    public void handleIncomingChatPacket(ChatPacket packet) throws Exception {
        UUID sender = packet.getSender();
        BaseComponent[] message = packet.getMessage();
        String plainText = ChatColor.stripColor(TextComponent.toPlainText(message)).toLowerCase();
        String channel = packet.getChannel();
        if (channel.equals("ParkChat")) {
            for (Player player : PalaceBungee.getOnlinePlayers()) {
                if (player.isDisabled()) continue;
                if (!PalaceBungee.getServerUtil().getChannel(player).equals("ParkChat")) continue;
                if (packet.getRank().getRankId() < Rank.TRAINEE.getRankId() && player.isIgnored(sender)) continue;
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

    public void logout(UUID uuid) {
        messageCache.remove(uuid);
    }

    @Getter
    @AllArgsConstructor
    public static class ChatMessage {
        private final String message;
        private final long time = System.currentTimeMillis();
    }
}
