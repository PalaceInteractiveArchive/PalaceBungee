package network.palace.bungee.messages.packets;

import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.UUID;

public class ChangeChannelPacket extends MQPacket {
    @Getter private final UUID uuid;
    @Getter private final String channel;

    public ChangeChannelPacket(JsonObject object) {
        super(PacketID.Global.CHANGE_CHANNEL.getId(), object);
        this.uuid = UUID.fromString(object.get("uuid").getAsString());
        this.channel = object.get("channel").getAsString();
    }

    public ChangeChannelPacket(UUID uuid, String channel) {
        super(PacketID.Global.CHANGE_CHANNEL.getId(), null);
        this.uuid = uuid;
        this.channel = channel;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("uuid", uuid.toString());
        object.addProperty("channel", channel);
        return object;
    }
}
