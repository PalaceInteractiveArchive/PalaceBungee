package network.palace.bungee.messages.packets;

import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.UUID;

@Getter
public class DMPacket extends MQPacket {
    private final String from, to, message;
    private final UUID fromUUID, toUUID;
    private final boolean initialSend;

    public DMPacket(JsonObject object) {
        super(PacketID.Global.DM.getId(), object);
        this.from = object.get("from").getAsString();
        this.to = object.get("to").getAsString();
        if (object.has("fromUUID")) {
            this.fromUUID = UUID.fromString(object.get("fromUUID").getAsString());
        } else {
            this.fromUUID = null;
        }
        if (object.has("toUUID")) {
            this.toUUID = UUID.fromString(object.get("toUUID").getAsString());
        } else {
            this.toUUID = null;
        }
        this.message = object.get("message").getAsString();
        this.sendingProxy = UUID.fromString(object.get("sendingProxy").getAsString());
        this.initialSend = object.get("initialSend").getAsBoolean();
    }

    public DMPacket(String from, String to, String message, UUID fromUUID, UUID toUUID, UUID sendingProxy, boolean initialSend) {
        super(PacketID.Global.DM.getId(), null);
        this.from = from;
        this.to = to;
        this.message = message;
        this.fromUUID = fromUUID;
        this.toUUID = toUUID;
        this.sendingProxy = sendingProxy;
        this.initialSend = initialSend;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("from", from);
        object.addProperty("to", to);
        if (fromUUID != null) object.addProperty("fromUUID", fromUUID.toString());
        if (toUUID != null) object.addProperty("toUUID", toUUID.toString());
        object.addProperty("message", message);
        object.addProperty("sendingProxy", sendingProxy.toString());
        object.addProperty("initialSend", initialSend);
        return object;
    }
}
