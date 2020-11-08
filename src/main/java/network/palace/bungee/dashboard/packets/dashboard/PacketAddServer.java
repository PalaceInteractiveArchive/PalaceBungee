package network.palace.bungee.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import network.palace.bungee.dashboard.packets.BasePacket;
import network.palace.bungee.dashboard.packets.PacketID;

/**
 * Created by Marc on 8/25/16
 */
public class PacketAddServer extends BasePacket {
    private String name;
    private String address;

    public PacketAddServer() {
        this("", "");
    }

    public PacketAddServer(String name, String address) {
        this.id = PacketID.Dashboard.ADDSERVER.getID();
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public PacketAddServer fromJSON(JsonObject obj) {
        this.name = obj.get("name").getAsString();
        this.address = obj.get("address").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            obj.addProperty("name", this.name);
            obj.addProperty("address", this.address);
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}