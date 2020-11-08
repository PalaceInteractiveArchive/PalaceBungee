package network.palace.bungee.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import network.palace.bungee.dashboard.packets.BasePacket;
import network.palace.bungee.dashboard.packets.PacketID;

import java.util.UUID;

/**
 * Created by Marc on 9/28/16
 */
public class PacketTitle extends BasePacket {
    private UUID uuid;
    private String title;
    private String subtitle;
    private int fadeIn;
    private int stay;
    private int fadeOut;

    public PacketTitle() {
        this(null, "", "", 0, 0, 0);
    }

    public PacketTitle(UUID uuid, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        this.id = PacketID.Dashboard.TITLE.getID();
        this.uuid = uuid;
        this.title = title;
        this.subtitle = subtitle;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public int getFadeIn() {
        return fadeIn;
    }

    public int getStay() {
        return stay;
    }

    public int getFadeOut() {
        return fadeOut;
    }

    @Override
    public PacketTitle fromJSON(JsonObject obj) {
        this.id = obj.get("id").getAsInt();
        try {
            this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }
        this.title = obj.get("title").getAsString();
        this.subtitle = obj.get("subtitle").getAsString();
        this.fadeIn = obj.get("fadeIn").getAsInt();
        this.stay = obj.get("stay").getAsInt();
        this.fadeOut = obj.get("fadeOut").getAsInt();
        return this;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", this.id);
        obj.addProperty("uuid", this.uuid.toString());
        obj.addProperty("title", this.title);
        obj.addProperty("subtitle", this.subtitle);
        obj.addProperty("fadeIn", this.fadeIn);
        obj.addProperty("stay", this.stay);
        obj.addProperty("fadeOut", this.fadeOut);
        return obj;
    }
}