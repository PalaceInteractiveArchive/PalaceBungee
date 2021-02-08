package network.palace.bungee.messages;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.chat.ComponentSerializer;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.*;
import network.palace.bungee.handlers.moderation.Mute;
import network.palace.bungee.handlers.moderation.ProviderBan;
import network.palace.bungee.messages.packets.*;
import network.palace.bungee.utils.ConfigUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

public class MessageHandler {
    public static final AMQP.BasicProperties JSON_PROPS = new AMQP.BasicProperties.Builder().contentEncoding("application/json").build();

    public Connection PUBLISHING_CONNECTION, CONSUMING_CONNECTION;
    public MessageClient ALL_PROXIES, CHAT_ANALYSIS;

    private final ConnectionFactory factory;
    private final HashMap<String, Channel> channels = new HashMap<>();

    /* Chat Clear Data */
    private final HashMap<String, Long> lastCleared = new HashMap<>();
    private final String clearMessage = "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n";

    public MessageHandler() throws IOException, TimeoutException {
        factory = new ConnectionFactory();
        ConfigUtil.DatabaseConnection connection = PalaceBungee.getConfigUtil().getRabbitMQInfo();
        factory.setVirtualHost(connection.getDatabase());
        factory.setHost(connection.getHost());
        factory.setUsername(connection.getUsername());
        factory.setPassword(connection.getPassword());

        PUBLISHING_CONNECTION = factory.newConnection();
        CONSUMING_CONNECTION = factory.newConnection();

        PUBLISHING_CONNECTION.addShutdownListener(e -> {
            PalaceBungee.getProxyServer().getLogger().warning("Publishing connection has been closed - reinitializing!");
            try {
                PUBLISHING_CONNECTION = factory.newConnection();
            } catch (IOException | TimeoutException ioException) {
                PalaceBungee.getProxyServer().getLogger().severe("Failed to reinitialize publishing connection!");
                ioException.printStackTrace();
            }
        });
        CONSUMING_CONNECTION.addShutdownListener(e -> {
            PalaceBungee.getProxyServer().getLogger().warning("Consuming connection has been closed - reinitializing!");
            try {
                CONSUMING_CONNECTION = factory.newConnection();
            } catch (IOException | TimeoutException ioException) {
                PalaceBungee.getProxyServer().getLogger().severe("Failed to reinitialize consuming connection!");
                ioException.printStackTrace();
            }
        });
    }

    public void initialize() throws IOException, TimeoutException {
        try {
            ALL_PROXIES = new MessageClient(ConnectionType.PUBLISHING, "all_proxies", "fanout");
            CHAT_ANALYSIS = new MessageClient(ConnectionType.PUBLISHING, "chat_analysis", true);
        } catch (Exception e) {
            e.printStackTrace();
            PalaceBungee.getProxyServer().getLogger().severe("There was an error initializing essential message publishing queues!");
        }

        CancelCallback doNothing = consumerTag -> {
        };

        registerConsumer("all_proxies", "fanout", "", (consumerTag, delivery) -> {
            try {
                JsonObject object = parseDelivery(delivery);
                PalaceBungee.getProxyServer().getLogger().warning(object.toString());
                switch (object.get("id").getAsInt()) {
                    // Broadcast
                    case 1: {
                        BroadcastPacket packet = new BroadcastPacket(object);
                        String message = ChatColor.WHITE + "[" + ChatColor.AQUA + "Information" + ChatColor.WHITE + "] " + ChatColor.GREEN + packet.getMessage();
                        String staffMessage = ChatColor.WHITE + "[" + ChatColor.AQUA + packet.getSender() + ChatColor.WHITE + "] " + ChatColor.GREEN + packet.getMessage();
                        PalaceBungee.getOnlinePlayers().forEach(player -> {
                            if (player.getRank().getRankId() >= Rank.TRAINEE.getRankId()) {
                                player.sendMessage(staffMessage);
                            } else {
                                player.sendMessage(message);
                            }
                        });
                        break;
                    }
                    // Message by rank
                    case 2: {
                        MessageByRankPacket packet = new MessageByRankPacket(object);
                        RankTag tag = packet.getTag();
                        BaseComponent[] components = packet.isComponentMessage() ? ComponentSerializer.parse(packet.getMessage()) : null;
                        PalaceBungee.getOnlinePlayers().stream().filter(p -> {
                            if (packet.isExact())
                                return p.getRank().equals(packet.getRank()) || (tag != null && p.getTags().contains(packet.getTag()));
                            else
                                return p.getRank().getRankId() >= packet.getRank().getRankId() || (tag != null && p.getTags().contains(packet.getTag()));
                        }).forEach(player -> {
                            if (packet.isComponentMessage()) player.sendMessage(components);
                            else player.sendMessage(packet.getMessage());
                        });
                        break;
                    }
                    // Proxy reload
                    case 3: {
                        ProxyReloadPacket packet = new ProxyReloadPacket(object);
                        PalaceBungee.getConfigUtil().reload();
                        break;
                    }
                    // Message
                    case 5: {
                        MessagePacket packet = new MessagePacket(object);
                        for (UUID uuid : packet.getPlayers()) {
                            Player tp = PalaceBungee.getPlayer(uuid);
                            if (tp != null) tp.sendMessage(packet.getMessage());
                        }
                        break;
                    }
                    // Component Message
                    case 6: {
                        ComponentMessagePacket packet = new ComponentMessagePacket(object);
                        BaseComponent[] components = ComponentSerializer.parse(packet.getSerializedMessage());
                        if (packet.getPlayers() == null) {
                            for (Player tp : PalaceBungee.getOnlinePlayers()) {
                                tp.sendMessage(components);
                            }
                        } else {
                            for (UUID uuid : packet.getPlayers()) {
                                Player tp = PalaceBungee.getPlayer(uuid);
                                if (tp != null) tp.sendMessage(components);
                            }
                        }
                        break;
                    }
                    // Chat Clear
                    case 7: {
                        ClearChatPacket packet = new ClearChatPacket(object);
                        String chat = packet.getChat();
                        UUID target = packet.getTarget();
                        if (target != null) {
                            String username = PalaceBungee.getUsername(target);
                            for (Player tp : PalaceBungee.getOnlinePlayers()) {
                                if (tp.getRank().getRankId() >= Rank.TRAINEE.getRankId()) {
                                    tp.sendMessage("\n" + Subsystem.CHAT.getPrefix() + ChatColor.DARK_AQUA + username + "'s chat has been cleared by " + packet.getSource());
                                }
                            }
                            Player tp = PalaceBungee.getPlayer(target);
                            if (tp != null && tp.getRank().getRankId() < Rank.TRAINEE.getRankId())
                                tp.sendMessage(clearMessage + Subsystem.CHAT.getPrefix() + ChatColor.DARK_AQUA + "Chat has been cleared");
                            return;
                        }
                        if (System.currentTimeMillis() - (lastCleared.getOrDefault(chat, 0L)) < 2000) {
                            //if this chat was last cleared up to 2 seconds ago, prevent it from being cleared again
                            Player player = PalaceBungee.getPlayer(packet.getSource());
                            if (player != null)
                                player.sendMessage(ChatColor.YELLOW + "It hasn't been 2 seconds since the last chat clear!");
                            return;
                        }
                        lastCleared.put(chat, System.currentTimeMillis());
                        for (Player tp : PalaceBungee.getOnlinePlayers()) {
                            if (tp.getServerName().equals(chat)) {
                                if (tp.getRank().getRankId() < Rank.TRAINEE.getRankId()) {
                                    tp.sendMessage(clearMessage + Subsystem.CHAT.getPrefix() + ChatColor.DARK_AQUA + "Chat has been cleared");
                                } else {
                                    tp.sendMessage("\n" + Subsystem.CHAT.getPrefix() + ChatColor.DARK_AQUA + "Chat has been cleared by " + packet.getSource());
                                }
                            }
                        }
                        break;
                    }
                    // Create Server
                    case 8: {
                        CreateServerPacket packet = new CreateServerPacket(object);
                        String[] addressList = packet.getAddress().split(":");
                        ServerInfo info = ProxyServer.getInstance().constructServerInfo(packet.getName(), new InetSocketAddress(addressList[0], Integer.parseInt(addressList[1])), "", false);
                        ProxyServer.getInstance().getServers().put(packet.getName(), info);
                        PalaceBungee.getServerUtil().createServer(new Server(packet.getName(), packet.getAddress(), packet.isPark(), packet.getType(), false));
                        PalaceBungee.getProxyServer().getLogger().info("New server created: " + object.toString());
                        break;
                    }
                    // Delete Server
                    case 9: {
                        DeleteServerPacket packet = new DeleteServerPacket(object);
                        ProxyServer.getInstance().getServers().remove(packet.getName());
                        PalaceBungee.getServerUtil().deleteServer(packet.getName());
                        PalaceBungee.getProxyServer().getLogger().info("Server deleted: " + packet.getName());
                        break;
                    }
                    // Chat
                    case 12: {
                        ChatPacket packet = new ChatPacket(object);
                        PalaceBungee.getChatUtil().handleIncomingChatPacket(packet);
                        break;
                    }
                    case 15: {
                        handleSendPacket(new SendPlayerPacket(object));
                        break;
                    }
                    case 16: {
                        ChangeChannelPacket packet = new ChangeChannelPacket(object);
                        UUID uuid = packet.getUuid();
                        String channel = packet.getChannel();

                        Player player = PalaceBungee.getPlayer(uuid);
                        if (player == null || player.getChannel().equals(channel)) return;

                        player.setChannel(channel);
                        player.sendMessage(ChatColor.GREEN + "You have been moved to the " + ChatColor.AQUA + channel +
                                ChatColor.GREEN + " channel");
                        break;
                    }
                    case 17: {
                        ChatMutePacket packet = new ChatMutePacket(object);
                        List<String> mutedChats = PalaceBungee.getConfigUtil().getMutedChats();
                        if (packet.isMuted() && !mutedChats.contains(packet.getChannel())) {
                            mutedChats.add(packet.getChannel());
                        } else if (!packet.isMuted() && mutedChats.contains(packet.getChannel())) {
                            mutedChats.remove(packet.getChannel());
                        } else {
                            return;
                        }
                        PalaceBungee.getConfigUtil().setMutedChats(mutedChats, false);
                        String msg;
                        if (packet.isMuted()) {
                            msg = ChatColor.WHITE + "[" + ChatColor.DARK_AQUA + "Palace Chat" + ChatColor.WHITE + "] " +
                                    ChatColor.YELLOW + "Chat has been muted";
                        } else {
                            msg = ChatColor.WHITE + "[" + ChatColor.DARK_AQUA + "Palace Chat" + ChatColor.WHITE + "] " +
                                    ChatColor.YELLOW + "Chat has been unmuted";
                        }
                        String msgname = msg + " by " + packet.getSource();
                        for (Player tp : PalaceBungee.getOnlinePlayers()) {
                            if ((packet.getChannel().equals("ParkChat") && PalaceBungee.getServerUtil().getServer(tp.getServerName(), true).isPark()) || tp.getServerName().equals(packet.getChannel())) {
                                tp.sendMessage(tp.getRank().getRankId() >= Rank.TRAINEE.getRankId() ? msgname : msg);
                            }
                        }
                        break;
                    }
                    case 18: {
                        MentionByRankPacket packet = new MentionByRankPacket(object);
                        Rank rank = packet.getRank();
                        RankTag tag = packet.getTag();
                        PalaceBungee.getOnlinePlayers().stream().filter(p -> {
                            if (packet.isExact())
                                return (rank != null && p.getRank().equals(packet.getRank())) || (tag != null && p.getTags().contains(packet.getTag()));
                            else
                                return (rank != null && p.getRank().getRankId() >= packet.getRank().getRankId()) || (tag != null && p.getTags().contains(packet.getTag()));
                        }).forEach(Player::mention);
                        break;
                    }
                    case 19: {
                        KickPlayerPacket packet = new KickPlayerPacket(object);
                        Player tp = PalaceBungee.getPlayer(packet.getUuid());
                        if (tp != null) {
                            if (packet.isComponentMessage()) {
                                tp.kickPlayer(ComponentSerializer.parse(packet.getReason()));
                            } else {
                                tp.kickPlayer(packet.getReason(), false);
                            }
                        }
                        break;
                    }
                    case 20: {
                        KickIPPacket packet = new KickIPPacket(object);
                        String address = packet.getAddress();
                        if (!address.contains("*")) {
                            for (Player tp : PalaceBungee.getOnlinePlayers()) {
                                if (tp.getAddress().equals(address)) {
                                    if (packet.isComponentMessage()) {
                                        tp.kickPlayer(ComponentSerializer.parse(packet.getReason()));
                                    } else {
                                        tp.kickPlayer(packet.getReason());
                                    }
                                }
                            }
                        } else {
                            for (Player tp : PalaceBungee.getOnlinePlayers()) {
                                String[] list = tp.getAddress().split("\\.");
                                String range = list[0] + "." + list[1] + "." + list[2] + ".*";
                                if (range.equalsIgnoreCase(address)) {
                                    if (packet.isComponentMessage()) {
                                        tp.kickPlayer(ComponentSerializer.parse(packet.getReason()));
                                    } else {
                                        tp.kickPlayer(packet.getReason());
                                    }
                                }
                            }
                        }
                        break;
                    }
                    case 21: {
                        MutePlayerPacket packet = new MutePlayerPacket(object);
                        Player tp = PalaceBungee.getPlayer(packet.getUuid());
                        if (tp == null) return;
                        Mute mute = PalaceBungee.getMongoHandler().getCurrentMute(tp.getUniqueId());
                        if (mute != null) {
                            tp.setMute(mute);
                            tp.sendMessage(PalaceBungee.getModerationUtil().getMuteMessage(mute));
                        } else {
                            if (tp.getMute() != null && tp.getMute().isMuted()) {
                                tp.sendMessage(ChatColor.RED + "You have been unmuted.");
                            }
                            tp.setMute(null);
                        }
                        break;
                    }
                    case 22: {
                        BanProviderPacket packet = new BanProviderPacket(object);
                        for (Player tp : PalaceBungee.getOnlinePlayers()) {
                            if (tp.getIsp().trim().equalsIgnoreCase(packet.getProvider().trim())) {
                                try {
                                    tp.kickPlayer(PalaceBungee.getModerationUtil().getBanMessage(new ProviderBan(packet.getProvider(), "")));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        break;
                    }
                    case 23: {
                        FriendJoinPacket packet = new FriendJoinPacket(object);
                        String phrase = packet.isJoin() ? "joined" : "left";
                        for (Player tp : PalaceBungee.getOnlinePlayers()) {
                            if ((packet.isStaff() && tp.getRank().getRankId() >= Rank.CHARACTER.getRankId()) ||
                                    !packet.getPlayers().contains(tp.getUniqueId()))
                                continue;
                            tp.sendMessage(packet.getUsername() + ChatColor.LIGHT_PURPLE + " has " + phrase + ".");
                        }
                        break;
                    }
                    case 32: {
                        BroadcastComponentPacket packet = new BroadcastComponentPacket(object);
                        BaseComponent[] message = new ComponentBuilder("[").color(ChatColor.WHITE)
                                .append("Information").color(ChatColor.AQUA)
                                .append("] ").color(ChatColor.WHITE)
                                .append(ComponentSerializer.parse(packet.getSerializedMessage())).create();
                        BaseComponent[] staffMessage = new ComponentBuilder("[").color(ChatColor.WHITE)
                                .append(packet.getSender()).color(ChatColor.AQUA)
                                .append("] ").color(ChatColor.WHITE)
                                .append(ComponentSerializer.parse(packet.getSerializedMessage())).create();
                        PalaceBungee.getOnlinePlayers().forEach(player -> {
                            if (player.getRank().getRankId() >= Rank.TRAINEE.getRankId()) {
                                player.sendMessage(staffMessage);
                            } else {
                                player.sendMessage(message);
                            }
                        });
                        break;
                    }
                    case 33: {
                        EmptyServerPacket packet = new EmptyServerPacket(object);
                        String name = packet.getServer();
                        Server server = PalaceBungee.getServerUtil().getServer(name, true);
                        if (server == null) {
                            return;
                        }
                        for (Player tp : PalaceBungee.getOnlinePlayers()) {
                            if (tp.getServerName().equals(server.getName())) {
                                Server target = PalaceBungee.getServerUtil().getServerByType(name.replaceAll("\\d*$", ""), server.getUniqueId());
                                if (target == null) {
                                    if (server.getServerType().equalsIgnoreCase("hub")) {
                                        target = PalaceBungee.getServerUtil().getServerByType("Creative");
                                    } else {
                                        target = PalaceBungee.getServerUtil().getServerByType("Hub");
                                    }
                                }
                                if (target == null) {
                                    target = PalaceBungee.getServerUtil().getEmptyParkServer(server.isPark() ? server.getUniqueId() : null);
                                }
                                if (!target.getName().toLowerCase().startsWith("hub") && !target.getName().toLowerCase().startsWith("arcade")) {
                                    tp.sendMessage(ChatColor.RED + "No fallback servers are available, so you were sent to a Park server.");
                                }
                                PalaceBungee.getServerUtil().sendPlayer(tp, target);
                            }
                        }
                        break;
                    }
                    case 34: {
                        RankChangePacket packet = new RankChangePacket(object);
                        UUID uuid = packet.getUuid();
                        Rank rank = packet.getRank();
                        List<String> tags = packet.getTags();
                        String source = packet.getSource();
                        Player player = PalaceBungee.getPlayer(uuid);

                        PalaceBungee.getProxyServer().getScheduler().runAsync(PalaceBungee.getInstance(), () -> {
                            String name;
                            if (player == null) {
                                name = PalaceBungee.getMongoHandler().uuidToUsername(uuid);
                            } else {
                                player.setRank(rank);
                                player.getTags().forEach(player::removeTag);
                                for (String tag : tags) {
                                    player.addTag(RankTag.fromString(tag));
                                }
                                name = player.getUsername();
                                //TODO discord syncing
                            }
                            List<RankTag> realTags = new ArrayList<>();
                            for (String s : tags) {
                                realTags.add(RankTag.fromString(s));
                            }
                            try {
                                PalaceBungee.getModerationUtil().announceRankChange(name, rank, realTags, source);
                            } catch (Exception e) {
                                PalaceBungee.getInstance().getLogger().log(Level.SEVERE, "Error announcing rank change", e);
                            }

                            try {
                                int member_id = PalaceBungee.getMongoHandler().getForumMemberId(uuid);
                                if (member_id != -1) {
                                    PalaceBungee.getForumUtil().updatePlayerRank(uuid, member_id, rank, player);
                                }
                            } catch (Exception e) {
                                PalaceBungee.getInstance().getLogger().log(Level.SEVERE, "Error processing rank change", e);
                            }
                        });
                        break;
                    }
                }
            } catch (Exception e) {
                handleError(consumerTag, delivery, e);
            }
        }, doNothing);

        registerConsumer("proxy_direct", "direct", PalaceBungee.getProxyID().toString(), (consumerTag, delivery) -> {
            try {
                JsonObject object = parseDelivery(delivery);
                PalaceBungee.getProxyServer().getLogger().severe(object.toString());
                switch (object.get("id").getAsInt()) {
                    // DM
                    case 4: {
                        DMPacket packet = new DMPacket(object);
                        Player player = PalaceBungee.getPlayer(packet.getTo());
                        if (packet.isInitialSend()) {
                            // message to target
                            DMPacket response;
                            if (player == null) {
                                response = new DMPacket("", packet.getFrom(), "", packet.getFromUUID(), packet.getToUUID(), PalaceBungee.getProxyID(), false, packet.isSenderIsStaff());
                            } else {
                                if (!packet.isSenderIsStaff() && (!player.isDmEnabled() || (player.isIgnored(packet.getFromUUID()) && player.getRank().getRankId() < Rank.CHARACTER.getRankId()))) {
                                    // if sender is not staff, and the target player either has dm's disabled or has ignored the sender
                                    response = new DMPacket("", packet.getFrom(), ChatColor.RED + "This person has messages disabled!", packet.getFromUUID(), packet.getToUUID(), PalaceBungee.getProxyID(), false, packet.isSenderIsStaff());
                                } else {
                                    player.sendMessage(ChatColor.GREEN + packet.getFrom() + ChatColor.LIGHT_PURPLE + " -> " + ChatColor.GREEN + "You: " + ChatColor.WHITE + packet.getMessage());
                                    player.mention();
                                    response = new DMPacket(player.getUsername(), packet.getFrom(), packet.getMessage(), packet.getFromUUID(), player.getUniqueId(), PalaceBungee.getProxyID(), false, packet.isSenderIsStaff());
                                    player.setReplyTo(packet.getFromUUID());
                                    player.setReplyTime(System.currentTimeMillis());
                                }
                            }
                            sendToProxy(response, packet.getSendingProxy());
                        } else {
                            // confirmation to sender
                            if (player == null) return;
                            if (packet.getFrom().isEmpty()) {
                                if (packet.getMessage().isEmpty()) {
                                    player.sendMessage(ChatColor.RED + "Player not found!");
                                } else {
                                    player.sendMessage(packet.getMessage());
                                }
                            } else {
                                player.sendMessage(ChatColor.GREEN + "You" + ChatColor.LIGHT_PURPLE + " -> " + ChatColor.GREEN + packet.getFrom() + ": " + ChatColor.WHITE + packet.getMessage());
                                player.setReplyTo(packet.getToUUID());
                                player.setReplyTime(System.currentTimeMillis());
                            }
                        }
                        break;
                    }
                    // Chat Analysis (Response)
                    case 14: {
                        ChatAnalysisResponsePacket packet = new ChatAnalysisResponsePacket(object);
                        PalaceBungee.getChatUtil().handleAnalysisResponse(packet);
                        break;
                    }
                    case 15: {
                        handleSendPacket(new SendPlayerPacket(object));
                        break;
                    }
                }
            } catch (Exception e) {
                handleError(consumerTag, delivery, e);
            }
        }, doNothing);
    }

    private void handleSendPacket(SendPlayerPacket packet) {
        String target = packet.getTargetPlayer();
        Server server = PalaceBungee.getServerUtil().getServer(packet.getTargetServer(), true);
        if (server == null) {
            server = PalaceBungee.getServerUtil().getServerByType(packet.getTargetServer());
            if (server == null) return;
        }
        if (target.contains(":")) {
            String fromServer = target.split(":")[1];
            // send all on fromServer to targetServer
            for (Player player : PalaceBungee.getOnlinePlayers()) {
                if (player.getServerName().equals(fromServer)) {
                    server.join(player);
                }
            }
        } else if (target.contains("-")) {
            try {
                UUID playerUUID = UUID.fromString(target);
                Player player = PalaceBungee.getPlayer(playerUUID);
                server.join(player);
            } catch (Exception ignored) {
            }
        } else if (target.equals("all")) {
            // send all players to targetServer
            for (Player player : PalaceBungee.getOnlinePlayers()) {
                server.join(player);
            }
        } else {
            try {
                Player player = PalaceBungee.getPlayer(target);
                server.join(player);
            } catch (Exception ignored) {
            }
        }
    }

    private void handleError(String consumerTag, Delivery delivery, Exception e) {
        PalaceBungee.getProxyServer().getLogger().severe("[MessageHandler] Error processing message: " + e.getClass().getName() + " - " + e.getMessage());
        PalaceBungee.getProxyServer().getLogger().severe("consumerTag: " + consumerTag);
        PalaceBungee.getProxyServer().getLogger().severe("envelope: " + delivery.getEnvelope().toString());
        PalaceBungee.getProxyServer().getLogger().severe("body (bytes): " + Arrays.toString(delivery.getBody()));
        PalaceBungee.getProxyServer().getLogger().severe("body (string): " + new String(delivery.getBody(), StandardCharsets.UTF_8));
        e.printStackTrace();
    }

    private JsonObject parseDelivery(Delivery delivery) {
        byte[] bytes = delivery.getBody();
        String s = new String(bytes, StandardCharsets.UTF_8);
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(s);
        if (!element.isJsonObject()) throw new IllegalArgumentException("Json is not an object: " + element.toString());
        JsonObject object = element.getAsJsonObject();
        if (!object.has("id")) throw new IllegalArgumentException("Missing 'id' field from message packet");
        return object;
    }

    /**
     * Register a MessageQueue consumer
     *
     * @param exchange        the exchange name
     * @param exchangeType    the exchange type (i.e. fanout)
     * @param deliverCallback what to do when a message is received
     * @param cancelCallback  what to do when the consumer is closed
     * @return the queue name created (used to cancel the consumer)
     * @throws IOException      on IOException
     * @throws TimeoutException on TimeoutException
     */
    public String registerConsumer(String exchange, String exchangeType, String routingKey, DeliverCallback deliverCallback, CancelCallback cancelCallback) throws IOException, TimeoutException {
        Channel channel = CONSUMING_CONNECTION.createChannel();
        channel.exchangeDeclare(exchange, exchangeType);

        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, exchange, routingKey);

        channel.basicConsume(queueName, true, deliverCallback, cancelCallback);

        channels.put(queueName, channel);

        return queueName;
    }

    public void unregisterConsumer(String queueName) throws IOException {
        Channel channel = channels.remove(queueName);
        if (channel != null) {
            channel.basicCancel(queueName);
        }
    }

    public void shutdown() {
        channels.forEach((queueName, channel) -> {
            try {
                Connection connection = channel.getConnection();
                if (connection.isOpen()) connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        channels.clear();
    }

    public void sendMessage(MQPacket packet, MessageClient client) throws IOException {
        sendMessage(packet, client, "");
    }

    public void sendMessage(MQPacket packet, MessageClient client, String routingKey) throws IOException {
        client.basicPublish(packet.toBytes(), routingKey);
    }

    public void sendMessage(MQPacket packet, String exchange, String exchangeType, String routingKey) throws Exception {
        MessageClient client = new MessageClient(ConnectionType.PUBLISHING, exchange, exchangeType);
        client.basicPublish(packet.toBytes(), routingKey);
    }

    public void sendStaffMessage(String message) throws Exception {
        MessageByRankPacket packet = new MessageByRankPacket("[" + ChatColor.RED + "STAFF" +
                ChatColor.WHITE + "] " + message, Rank.TRAINEE, null, false, false);
        sendMessage(packet, ALL_PROXIES);
    }

    public void sendMessageToPlayer(UUID uuid, BaseComponent[] message) throws Exception {
        Player player = PalaceBungee.getPlayer(uuid);
        if (player != null) {
            player.sendMessage(message);
            return;
        }
        ComponentMessagePacket packet = new ComponentMessagePacket(message, uuid);
        sendMessage(packet, ALL_PROXIES);
    }

    public void sendMessageToPlayer(UUID uuid, String message) throws Exception {
        Player player = PalaceBungee.getPlayer(uuid);
        if (player != null) {
            player.sendMessage(message);
            return;
        }
        MessagePacket packet = new MessagePacket(message, uuid);
        sendMessage(packet, ALL_PROXIES);
    }

    public void sendToProxy(MQPacket packet, UUID targetProxy) throws Exception {
        sendMessage(packet, new MessageClient(ConnectionType.PUBLISHING, "proxy_direct", "direct"), targetProxy.toString());
    }

    public Connection getConnection(ConnectionType type) throws IOException, TimeoutException {
        switch (type) {
            case PUBLISHING:
                return PUBLISHING_CONNECTION;
            case CONSUMING:
                return CONSUMING_CONNECTION;
            default:
                return factory.newConnection();
        }
    }
}
