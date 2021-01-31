package network.palace.bungee.messages.packets;

import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.UUID;

@Getter
public class DisablePlayerPacket extends MQPacket {
    private final UUID uuid;
    private final boolean disabled;

    public DisablePlayerPacket(JsonObject object) {
        super(PacketID.Global.MUTE_PLAYER.getId(), object);
        this.uuid = UUID.fromString(object.get("uuid").getAsString());
        this.disabled = object.get("disabled").getAsBoolean();
    }

    public DisablePlayerPacket(UUID uuid, boolean disabled) {
        super(PacketID.Global.MUTE_PLAYER.getId(), null);
        this.uuid = uuid;
        this.disabled = disabled;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("uuid", uuid.toString());
        object.addProperty("disabled", disabled);
        return object;
    }
}
