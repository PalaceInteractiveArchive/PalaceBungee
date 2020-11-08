package network.palace.bungee.dashboard.packets.dashboard;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import network.palace.bungee.dashboard.packets.BasePacket;
import network.palace.bungee.dashboard.packets.PacketID;
import network.palace.bungee.handlers.Rank;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PacketPlayerRank extends BasePacket {
    private UUID uuid;
    @Getter private Rank rank;
    @Getter private List<String> tags;

    public PacketPlayerRank() {
        this(null, Rank.SETTLER, null);
    }

    public PacketPlayerRank(UUID uuid, Rank rank, List<String> tags) {
        this.id = PacketID.Dashboard.PLAYERRANK.getID();
        this.uuid = uuid;
        this.rank = rank;
        this.tags = tags;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public PacketPlayerRank fromJSON(JsonObject obj) {
        try {
            this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }
        this.rank = Rank.fromString(obj.get("rank").getAsString());
        this.tags = new ArrayList<>();
        for (JsonElement e : obj.get("tags").getAsJsonArray()) {
            tags.add(e.getAsString());
        }
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            obj.addProperty("uuid", this.uuid.toString());
            obj.addProperty("rank", this.rank.getDBName());
            Gson gson = new Gson();
            obj.add("tags", gson.toJsonTree(this.tags).getAsJsonArray());
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}