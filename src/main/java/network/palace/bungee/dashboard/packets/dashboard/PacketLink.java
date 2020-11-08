package network.palace.bungee.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.dashboard.packets.BasePacket;
import network.palace.bungee.dashboard.packets.PacketID;

import java.util.UUID;

/**
 * Created by Marc on 9/22/16
 */
public class PacketLink extends BasePacket {
    private UUID uuid;
    private String url;
    private String name;
    private ChatColor color;
    private boolean bold;
    private boolean spacing;

    public PacketLink() {
        this(null, "", "", ChatColor.YELLOW, true);
    }

    public PacketLink(UUID uuid, String url, String name, ChatColor color, boolean bold) {
        this(uuid, url, name, color, bold, true);
    }

    public PacketLink(UUID uuid, String url, String name, ChatColor color, boolean bold, boolean spacing) {
        this.id = PacketID.Dashboard.LINK.getID();
        this.uuid = uuid;
        this.url = url;
        this.name = name;
        this.color = color;
        this.bold = bold;
        this.spacing = spacing;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public ChatColor getColor() {
        return color;
    }

    public boolean isBold() {
        return bold;
    }

    public boolean isSpacing() {
        return spacing;
    }

    public PacketLink fromJSON(JsonObject obj) {
        try {
            this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }
        this.url = obj.get("url").getAsString();
        this.name = obj.get("name").getAsString();
        this.color = ChatColor.valueOf(obj.get("color").getAsString());
        this.bold = obj.get("bold").getAsBoolean();
        this.spacing = obj.get("spacing").getAsBoolean();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            obj.addProperty("uuid", this.uuid.toString());
            obj.addProperty("url", this.url);
            obj.addProperty("name", this.name);
            obj.addProperty("color", this.color.name());
            obj.addProperty("bold", this.bold);
            obj.addProperty("spacing", this.spacing);
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}