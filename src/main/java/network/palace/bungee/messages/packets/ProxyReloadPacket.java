package network.palace.bungee.messages.packets;

import com.google.gson.JsonObject;

public class ProxyReloadPacket extends MQPacket {

    public ProxyReloadPacket(JsonObject object) {
        super(PacketID.Global.PROXYRELOAD.getId(), object);
    }

    public ProxyReloadPacket() {
        super(PacketID.Global.PROXYRELOAD.getId(), null);
    }

    @Override
    public JsonObject getJSON() {
        return getBaseJSON();
    }
}
