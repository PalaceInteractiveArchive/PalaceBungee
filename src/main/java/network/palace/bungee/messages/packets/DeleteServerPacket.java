package network.palace.bungee.messages.packets;

import com.google.gson.JsonObject;
import lombok.Getter;

public class DeleteServerPacket extends MQPacket {
    @Getter private final String name;

    public DeleteServerPacket(JsonObject object) {
        super(PacketID.Global.DELETESERVER.getId(), object);
        this.name = object.get("name").getAsString();
    }

    public DeleteServerPacket(String name) {
        super(PacketID.Global.DELETESERVER.getId(), null);
        this.name = name;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("name", name);
        return object;
    }
}
