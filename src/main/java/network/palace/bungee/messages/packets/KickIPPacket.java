package network.palace.bungee.messages.packets;

import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class KickIPPacket extends MQPacket {
    private final String address;
    private final String reason;
    private final boolean componentMessage;

    public KickIPPacket(JsonObject object) {
        super(PacketID.Global.KICK_IP.getId(), object);
        this.address = object.get("address").getAsString();
        this.reason = object.get("reason").getAsString();
        this.componentMessage = object.get("componentMessage").getAsBoolean();
    }

    public KickIPPacket(String address, String reason, boolean componentMessage) {
        super(PacketID.Global.KICK_IP.getId(), null);
        this.address = address;
        this.reason = reason;
        this.componentMessage = componentMessage;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("address", address);
        object.addProperty("reason", reason);
        object.addProperty("componentMessage", componentMessage);
        return object;
    }
}
