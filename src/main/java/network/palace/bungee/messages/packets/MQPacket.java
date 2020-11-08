package network.palace.bungee.messages.packets;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public abstract class MQPacket {
    @Getter private int id;

    protected MQPacket(int id, JsonObject object) {
        this.id = id;
        if (object != null && object.get("id").getAsInt() != id)
            throw new IllegalArgumentException("Packet id does not match!");
    }

    public MQPacket(JsonObject obj) {
    }

    public abstract JsonObject getJSON();

    protected JsonObject getBaseJSON() {
        JsonObject object = new JsonObject();
        object.addProperty("id", id);
        return object;
    }

    public byte[] toBytes() {
        JsonObject obj = getJSON();
        if (obj != null) return obj.toString().getBytes(StandardCharsets.UTF_8);
        else return new byte[0];
    }
}
