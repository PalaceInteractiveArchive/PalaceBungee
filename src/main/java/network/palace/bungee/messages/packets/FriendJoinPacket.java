package network.palace.bungee.messages.packets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FriendJoinPacket extends MQPacket {
    @Getter private final UUID uuid;
    @Getter private final String username;
    @Getter private final List<UUID> players;
    @Getter private final boolean join, staff;

    public FriendJoinPacket(JsonObject object) {
        super(PacketID.Global.FRIEND_JOIN.getId(), object);
        this.uuid = UUID.fromString(object.get("uuid").getAsString());
        this.username = object.get("message").getAsString();
        this.players = new ArrayList<>();
        JsonArray array = object.get("players").getAsJsonArray();
        for (JsonElement e : array) {
            players.add(UUID.fromString(e.getAsString()));
        }
        this.join = object.get("join").getAsBoolean();
        this.staff = object.get("staff").getAsBoolean();
    }

    public FriendJoinPacket(UUID uuid, String username, List<UUID> players, boolean join, boolean staff) {
        super(PacketID.Global.FRIEND_JOIN.getId(), null);
        this.uuid = uuid;
        this.username = username;
        this.players = players;
        this.join = join;
        this.staff = staff;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("uuid", uuid.toString());
        object.addProperty("message", username);

        JsonArray players = new JsonArray();
        this.players.forEach(p -> players.add(p.toString()));
        object.add("players", players);
        object.addProperty("join", join);
        object.addProperty("staff", staff);

        return object;
    }
}
