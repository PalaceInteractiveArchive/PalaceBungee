package network.palace.bungee.messages.packets;

import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.UUID;

public class SocialSpyPacket extends MQPacket {
    @Getter private final UUID sender;
    @Getter private final String message, channel;

    public SocialSpyPacket(JsonObject object) {
        super(PacketID.Global.SOCIAL_SPY.getId(), object);
        this.sender = UUID.fromString(object.get("sender").getAsString());
        this.message = object.get("message").getAsString();
        this.channel = object.get("channel").getAsString();
    }

    public SocialSpyPacket(UUID sender, String message, String channel) {
        super(PacketID.Global.SOCIAL_SPY.getId(), null);
        this.sender = sender;
        this.message = message;
        this.channel = channel;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("sender", sender.toString());
        object.addProperty("message", message);
        object.addProperty("channel", channel);
        return object;
    }
}
