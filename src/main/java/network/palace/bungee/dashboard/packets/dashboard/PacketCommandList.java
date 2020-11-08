package network.palace.bungee.dashboard.packets.dashboard;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import network.palace.bungee.dashboard.packets.BasePacket;
import network.palace.bungee.dashboard.packets.PacketID;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marc on 9/3/16
 */
public class PacketCommandList extends BasePacket {
    @Getter private List<String> tabPlayerCommands;
    @Getter private List<String> generalTabCommands;

    public PacketCommandList() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    public PacketCommandList(List<String> tabPlayerCommands, List<String> generalTabCommands) {
        this.id = PacketID.Dashboard.COMMANDLIST.getID();
        this.tabPlayerCommands = tabPlayerCommands;
        this.generalTabCommands = generalTabCommands;
    }

    public PacketCommandList fromJSON(JsonObject obj) {
        JsonArray tabPlayerCommands = obj.get("tabPlayerCommands").getAsJsonArray();
        for (JsonElement e : tabPlayerCommands) {
            this.tabPlayerCommands.add(e.getAsString());
        }
        JsonArray generalTabCommands = obj.get("generalTabCommands").getAsJsonArray();
        for (JsonElement e : generalTabCommands) {
            this.generalTabCommands.add(e.getAsString());
        }
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            Gson gson = new Gson();
            obj.add("tabPlayerCommands", gson.toJsonTree(this.tabPlayerCommands).getAsJsonArray());
            obj.add("generalTabCommands", gson.toJsonTree(this.generalTabCommands).getAsJsonArray());
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}