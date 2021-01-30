package network.palace.bungee.messages.packets;

import com.google.gson.JsonObject;
import lombok.Getter;

public class BanProviderPacket extends MQPacket {
    @Getter private final String provider;

    public BanProviderPacket(JsonObject object) {
        super(PacketID.Global.BAN_PROVIDER.getId(), object);
        this.provider = object.get("provider").getAsString();
    }

    public BanProviderPacket(String provider) {
        super(PacketID.Global.BAN_PROVIDER.getId(), null);
        this.provider = provider;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("provider", provider);
        return object;
    }
}
