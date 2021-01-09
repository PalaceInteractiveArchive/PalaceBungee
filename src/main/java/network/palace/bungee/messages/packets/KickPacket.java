package network.palace.bungee.messages.packets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class KickPacket extends MQPacket {
    @Getter private final String reason;
    @Getter private final UUID uuid;

    public KickPacket(JsonObject object) {
        super(PacketID.Global.KICK.getId(), object);
        this.reason = object.get("reason").getAsString();
        this.uuid = UUID.fromString(object.get("uuid").getAsString());
    }

    public KickPacket(UUID uuid, String reason) {
        super(PacketID.Global.KICK.getId(), null);
        this.reason = reason;
        this.uuid = uuid;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("reason", reason);
        object.addProperty("uuid", uuid.toString());
        return object;
    }
}
