package network.palace.bungee.messages;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.RankTag;
import network.palace.bungee.messages.packets.*;
import network.palace.bungee.utils.ConfigUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class MessageHandler {
    private final ConnectionFactory factory;
    private final HashMap<String, Channel> channels = new HashMap<>();

    public MessageHandler() throws IOException, TimeoutException {
        factory = new ConnectionFactory();
        ConfigUtil.DatabaseConnection connection = PalaceBungee.getConfigUtil().getRabbitMQInfo();
        factory.setVirtualHost(connection.getDatabase());
        factory.setHost(connection.getHost());
        factory.setUsername(connection.getUsername());
        factory.setPassword(connection.getPassword());

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
                        PalaceBungee.getOnlinePlayers().stream().filter(p -> {
                            if (packet.isExact())
                                return p.getRank().equals(packet.getRank()) || (tag != null && p.getTags().contains(packet.getTag()));
                            else
                                return p.getRank().getRankId() >= packet.getRank().getRankId() || (tag != null && p.getTags().contains(packet.getTag()));
                        }).forEach(player -> player.sendMessage(packet.getMessage()));
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
                            tp.sendMessage(packet.getMessage());
                        }
                        break;
                    }
                    // Component Message
                    case 6: {
                        ComponentMessagePacket packet = new ComponentMessagePacket(object);
                        BaseComponent[] components = ComponentSerializer.parse(packet.getSerializedMessage());
                        for (UUID uuid : packet.getPlayers()) {
                            Player tp = PalaceBungee.getPlayer(uuid);
                            tp.sendMessage(components);
                        }
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
                //noinspection SwitchStatementWithTooFewBranches
                switch (object.get("id").getAsInt()) {
                    // DM
                    case 4: {
                        DMPacket packet = new DMPacket(object);
                        Player player = PalaceBungee.getPlayer(packet.getTo());
                        if (packet.isInitialSend()) {
                            // message to target
                            DMPacket response;
                            if (player == null) {
                                response = new DMPacket("", packet.getFrom(), "", PalaceBungee.getProxyID(), false);
                            } else {
                                player.sendMessage(ChatColor.GREEN + packet.getFrom() + ChatColor.LIGHT_PURPLE + " -> " + ChatColor.GREEN + "You: " + ChatColor.WHITE + packet.getMessage());
                                response = new DMPacket(player.getUsername(), packet.getFrom(), packet.getMessage(), PalaceBungee.getProxyID(), false);
                            }
                            sendToProxy(response, packet.getSendingProxy());
                        } else {
                            // confirmation to sender
                            if (player == null) return;
                            if (packet.getFrom().isEmpty()) {
                                player.sendMessage(ChatColor.RED + "Player not found!");
                            } else {
                                player.sendMessage(ChatColor.GREEN + "You" + ChatColor.LIGHT_PURPLE + " -> " + ChatColor.GREEN + packet.getFrom() + ": " + ChatColor.WHITE + packet.getMessage());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                handleError(consumerTag, delivery, e);
            }
        }, doNothing);
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
        JsonObject object = (JsonObject) new JsonParser().parse(s);
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
        Connection connection = factory.newConnection();

        Channel channel = connection.createChannel();
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

    public void sendMessage(MQPacket packet, String exchange, String exchangeType) throws Exception {
        sendMessage(packet, exchange, exchangeType, "");
    }

    public void sendMessage(MQPacket packet, String exchange, String exchangeType, String routingKey) throws Exception {
        try (Connection connection = factory.newConnection()) {
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(exchange, exchangeType);
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().contentEncoding("application/json").build();
            channel.basicPublish(exchange, routingKey, props, packet.toBytes());
        }
    }

    public void sendStaffMessage(String message) throws Exception {
        MessageByRankPacket packet = new MessageByRankPacket("[" + ChatColor.RED + "STAFF" +
                ChatColor.WHITE + "] " + message, Rank.TRAINEE, null, false);
        sendMessage(packet, "all_proxies", "fanout");
    }

    public void sendMessageToPlayer(UUID uuid, String message) throws Exception {
        Player player = PalaceBungee.getPlayer(uuid);
        if (player != null) {
            player.sendMessage(message);
            return;
        }
        MessagePacket packet = new MessagePacket(message, uuid);
        sendMessage(packet, "all_proxies", "fanout");
    }

    public void sendToProxy(DMPacket packet, UUID targetProxy) throws Exception {
        sendMessage(packet, "proxy_direct", "direct", targetProxy.toString());
    }
}
