package network.palace.bungee.messages.packets;

import com.google.gson.JsonObject;
import lombok.Getter;
import network.palace.bungee.handlers.Rank;

import java.util.UUID;

@Getter
public class DMPacket extends MQPacket {
    private final String from, to, message, channel, command;
    private final UUID fromUUID, toUUID;
    private final boolean initialSend;
    private final Rank rank;

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
        this.channel = object.has("channel") ? object.get("channel").getAsString() : "ParkChat";
        this.command = object.has("command") ? object.get("command").getAsString() : "msg";
        this.sendingProxy = UUID.fromString(object.get("sendingProxy").getAsString());
        this.initialSend = object.get("initialSend").getAsBoolean();
        this.rank = Rank.fromString(object.get("rank").getAsString());
    }

    public DMPacket(String from, String to, String message, String channel, String command, UUID fromUUID, UUID toUUID, UUID sendingProxy, boolean initialSend, Rank rank) {
        super(PacketID.Global.DM.getId(), null);
        this.from = from;
        this.to = to;
        this.message = message;
        this.channel = channel;
        this.command = command;
        this.fromUUID = fromUUID;
        this.toUUID = toUUID;
        this.sendingProxy = sendingProxy;
        this.initialSend = initialSend;
        this.rank = rank;
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
        object.addProperty("rank", rank.getDBName());
        return object;
    }
}
