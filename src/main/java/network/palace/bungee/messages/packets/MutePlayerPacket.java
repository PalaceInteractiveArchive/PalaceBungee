package network.palace.bungee.messages.packets;

import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.UUID;

@Getter
public class MutePlayerPacket extends MQPacket {
    private final UUID uuid;

    public MutePlayerPacket(JsonObject object) {
        super(PacketID.Global.MUTE_PLAYER.getId(), object);
        this.uuid = UUID.fromString(object.get("uuid").getAsString());
    }

    public MutePlayerPacket(UUID uuid) {
        super(PacketID.Global.MUTE_PLAYER.getId(), null);
        this.uuid = uuid;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("uuid", uuid.toString());
        return object;
    }
}
