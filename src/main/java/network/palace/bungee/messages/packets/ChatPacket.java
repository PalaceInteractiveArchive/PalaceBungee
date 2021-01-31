package network.palace.bungee.messages.packets;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import network.palace.bungee.handlers.Rank;

import java.util.UUID;

public class ChatPacket extends MQPacket {
    @Getter private final UUID sender;
    @Getter private final Rank rank;
    @Getter private final String serializedMessage;
    // ParkChat, or server name
    @Getter private final String channel;

    public ChatPacket(JsonObject object) {
        super(PacketID.Global.CHAT.getId(), object);
        this.sender = UUID.fromString(object.get("sender").getAsString());
        this.rank = Rank.fromString(object.get("rank").getAsString());
        this.serializedMessage = object.get("serializedMessage").getAsString();
        this.channel = object.get("channel").getAsString();
    }

    public ChatPacket(UUID sender, Rank rank, BaseComponent[] message, String channel) {
        super(PacketID.Global.CHAT.getId(), null);
        this.sender = sender;
        this.rank = rank;
        this.serializedMessage = ComponentSerializer.toString(message);
        this.channel = channel;
    }

    public BaseComponent[] getMessage() {
        return ComponentSerializer.parse(serializedMessage);
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("sender", sender.toString());
        object.addProperty("rank", rank.getDBName());
        object.addProperty("serializedMessage", serializedMessage);
        object.addProperty("channel", channel);
        return object;
    }
}
