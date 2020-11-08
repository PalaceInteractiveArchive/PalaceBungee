package network.palace.bungee.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import network.palace.bungee.dashboard.packets.BasePacket;
import network.palace.bungee.dashboard.packets.PacketID;

import java.util.UUID;

/**
 * Created by Marc on 9/22/16
 */
public class PacketWarning extends BasePacket {
    private UUID warningid;
    private String username;
    private String message;
    private String action;

    public PacketWarning() {
        this(null, "", "", "");
    }

    public PacketWarning(UUID warningid, String username, String message, String action) {
        this.id = PacketID.Dashboard.WARNING.getID();
        this.warningid = warningid;
        this.username = username;
        this.message = message;
        this.action = action;
    }

    public UUID getWarningID() {
        return warningid;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public String getAction() {
        return action;
    }

    public PacketWarning fromJSON(JsonObject obj) {
        try {
            this.warningid = UUID.fromString(obj.get("warningid").getAsString());
        } catch (Exception e) {
            this.warningid = null;
        }
        this.username = obj.get("username").getAsString();
        this.message = obj.get("message").getAsString();
        this.action = obj.get("action").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            obj.addProperty("warningid", this.warningid.toString());
            obj.addProperty("username", this.username);
            obj.addProperty("message", this.message);
            obj.addProperty("action", this.action);
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}