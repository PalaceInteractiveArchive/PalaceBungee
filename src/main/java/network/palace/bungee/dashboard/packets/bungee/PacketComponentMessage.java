package network.palace.bungee.dashboard.packets.bungee;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import network.palace.bungee.dashboard.packets.BasePacket;
import network.palace.bungee.dashboard.packets.PacketID;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class PacketComponentMessage extends BasePacket {
    private List<UUID> targets;
    private String serializedMessage;

    public PacketComponentMessage() {
        this(new ArrayList<>(), "");
    }

    public PacketComponentMessage(List<UUID> targets, String serializedMessage) {
        this.id = PacketID.Bungee.COMPONENT_MESSAGE.getID();
        this.targets = targets;
        this.serializedMessage = serializedMessage;
    }

    public PacketComponentMessage fromJSON(JsonObject obj) {
        JsonArray targets = obj.get("targets").getAsJsonArray();
        for (JsonElement e : targets) {
            this.targets.add(UUID.fromString(e.getAsString()));
        }
        this.serializedMessage = obj.get("serializedMessage").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            Gson gson = new Gson();
            obj.add("targets", gson.toJsonTree(this.targets).getAsJsonArray());
            obj.addProperty("serializedMessage", this.serializedMessage);
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}