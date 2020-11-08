package network.palace.bungee.messages;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.*;
import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.messages.packets.BroadcastPacket;
import network.palace.bungee.messages.packets.MQPacket;
import network.palace.bungee.messages.packets.MessageByRankPacket;
import network.palace.bungee.utils.ConfigUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
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

        registerConsumer("all_proxies", "fanout", (consumerTag, delivery) -> {
            try {
                JsonObject object = parseDelivery(delivery);
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
                        PalaceBungee.getOnlinePlayers().stream().filter(p -> packet.isExact() ? p.getRank().equals(packet.getRank()) : p.getRank().getRankId() >= packet.getRank().getRankId()).forEach(player -> player.sendMessage(packet.getMessage()));
                        break;
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
    public String registerConsumer(String exchange, String exchangeType, DeliverCallback deliverCallback, CancelCallback cancelCallback) throws IOException, TimeoutException {
        String routingKey = "";

        Connection connection = factory.newConnection();

        Channel channel = connection.createChannel();
        channel.exchangeDeclare(exchange, exchangeType);

        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, exchange, routingKey);

        channel.basicConsume(queueName, true, deliverCallback, cancelCallback);

        channels.put(queueName, channel);

        return queueName;
    }

    public void unregisterListener(String queueName) throws IOException {
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
        try (Connection connection = factory.newConnection()) {
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(exchange, exchangeType);
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().contentEncoding("application/json").build();
            channel.basicPublish(exchange, "", props, packet.toBytes());
        }
    }

    public void sendStaffMessage(String message) throws Exception {
        MessageByRankPacket packet = new MessageByRankPacket("[" + ChatColor.RED + "STAFF" +
                ChatColor.WHITE + "] " + message, Rank.TRAINEE, false);
        sendMessage(packet, "all_proxies", "fanout");
    }
}
