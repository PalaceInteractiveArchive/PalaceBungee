package network.palace.bungee.messages.packets;

import com.google.gson.JsonObject;
import lombok.Getter;
import network.palace.bungee.handlers.Server;

public class CreateServerPacket extends MQPacket {
    @Getter private final String name, address, type;
    @Getter private final boolean park;

    public CreateServerPacket(JsonObject object) {
        super(PacketID.Global.CREATESERVER.getId(), object);
        this.name = object.get("name").getAsString();
        this.address = object.get("address").getAsString();
        this.type = object.get("type").getAsString();
        this.park = object.get("park").getAsBoolean();
    }

    public CreateServerPacket(String name, String address, String type, boolean park) {
        super(PacketID.Global.CREATESERVER.getId(), null);
        this.name = name;
        this.address = address;
        this.type = type;
        this.park = park;
    }

    public CreateServerPacket(Server s) {
        super(PacketID.Global.CREATESERVER.getId(), null);
        this.name = s.getName();
        this.address = s.getAddress();
        this.type = s.getServerType();
        this.park = s.isPark();
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("name", name);
        object.addProperty("address", address);
        object.addProperty("type", type);
        object.addProperty("park", park);
        return object;
    }
}
