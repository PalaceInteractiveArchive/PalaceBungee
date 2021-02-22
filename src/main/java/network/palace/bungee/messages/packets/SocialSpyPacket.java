package network.palace.bungee.messages.packets;

import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.UUID;

public class SocialSpyPacket extends MQPacket {
    @Getter private final UUID sender, receiver;
    @Getter private final String message, channel;

    public SocialSpyPacket(JsonObject object) {
        super(PacketID.Global.SOCIAL_SPY.getId(), object);
        this.sender = UUID.fromString(object.get("sender").getAsString());
        this.message = object.get("message").getAsString();
        this.channel = object.get("channel").getAsString();
        if (object.has("receiver")) this.receiver = UUID.fromString(object.get("receiver").getAsString());
        else this.receiver = null;
    }

    public SocialSpyPacket(UUID sender, UUID receiver, String message, String channel) {
        super(PacketID.Global.SOCIAL_SPY.getId(), null);
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.channel = channel;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("sender", sender.toString());
        if (receiver != null) object.addProperty("receiver", receiver.toString());
        object.addProperty("message", message);
        object.addProperty("channel", channel);
        return object;
    }
}
