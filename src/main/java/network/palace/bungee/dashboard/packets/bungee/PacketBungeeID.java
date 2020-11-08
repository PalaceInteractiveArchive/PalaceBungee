package network.palace.bungee.dashboard.packets.bungee;

import com.google.gson.JsonObject;
import network.palace.bungee.dashboard.packets.BasePacket;
import network.palace.bungee.dashboard.packets.PacketID;

import java.util.UUID;

/**
 * Created by Marc on 11/25/16
 */
public class PacketBungeeID extends BasePacket {
    private UUID bid;

    public PacketBungeeID() {
        this(null);
    }

    public PacketBungeeID(UUID id) {
        this.id = PacketID.Bungee.BUNGEEID.getID();
        this.bid = id;
    }

    public UUID getBungeeID() {
        return bid;
    }

    public PacketBungeeID fromJSON(JsonObject obj) {
        try {
            this.bid = UUID.fromString(obj.get("bid").getAsString());
        } catch (Exception e) {
            this.bid = null;
        }
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            obj.addProperty("bid", this.bid.toString());
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}