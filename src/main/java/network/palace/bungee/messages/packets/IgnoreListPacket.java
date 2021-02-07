package network.palace.bungee.messages.packets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IgnoreListPacket extends MQPacket {
    @Getter private final UUID uuid;
    @Getter private final List<UUID> players;

    public IgnoreListPacket(JsonObject object) {
        super(PacketID.Global.IGNORE_LIST.getId(), object);
        this.uuid = UUID.fromString(object.get("uuid").getAsString());
        this.players = new ArrayList<>();
        JsonArray array = object.get("players").getAsJsonArray();
        for (JsonElement e : array) {
            players.add(UUID.fromString(e.getAsString()));
        }
    }

    public IgnoreListPacket(UUID uuid, List<UUID> players) {
        super(PacketID.Global.IGNORE_LIST.getId(), null);
        this.uuid = uuid;
        this.players = players;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("uuid", uuid.toString());

        JsonArray players = new JsonArray();
        this.players.forEach(p -> players.add(p.toString()));
        object.add("players", players);

        return object;
    }
}
