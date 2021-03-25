package network.palace.bungee.messages.packets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MentionPacket extends MQPacket {
    @Getter private final List<UUID> players;

    public MentionPacket(JsonObject object) {
        super(PacketID.Global.MENTION.getId(), object);
        this.players = new ArrayList<>();
        if (object.has("players")) {
            JsonArray array = object.get("players").getAsJsonArray();
            for (JsonElement e : array) {
                players.add(UUID.fromString(e.getAsString()));
            }
        } else {
            players.add(UUID.fromString(object.get("uuid").getAsString()));
        }
    }

    public MentionPacket(List<UUID> players) {
        super(PacketID.Global.MENTION.getId(), null);
        this.players = players;
    }

    public MentionPacket(UUID... players) {
        super(PacketID.Global.MENTION.getId(), null);
        this.players = new ArrayList<>(Arrays.asList(players));
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("uuid", players.get(0).toString());

        JsonArray players = new JsonArray();
        this.players.forEach(p -> players.add(p.toString()));
        object.add("players", players);

        return object;
    }
}
