package network.palace.bungee.messages.packets;

import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.UUID;

public class PlayerQueuePacket extends MQPacket {
    @Getter private final String queueId;
    @Getter private final UUID playerUUID;
    @Getter private final boolean joining;

    public PlayerQueuePacket(JsonObject object) {
        super(PacketID.Global.PLAYER_QUEUE.getId(), object);
        this.queueId = object.get("queueId").getAsString();
        this.playerUUID = UUID.fromString(object.get("playerUUID").getAsString());
        this.joining = object.get("joining").getAsBoolean();
    }

    public PlayerQueuePacket(String queueId, UUID playerUUID, boolean joining) {
        super(PacketID.Global.PLAYER_QUEUE.getId(), null);
        this.queueId = queueId;
        this.playerUUID = playerUUID;
        this.joining = joining;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("queueId", queueId);
        object.addProperty("playerUUID", playerUUID.toString());
        object.addProperty("joining", joining);
        return object;
    }
}
