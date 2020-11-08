package network.palace.bungee.messages.packets;

import com.google.gson.JsonObject;

public class DMPacket extends MQPacket {
    private final String from, to, message;

    public DMPacket(JsonObject object) {
        super(1, object);
        this.from = object.get("from").getAsString();
        this.to = object.get("to").getAsString();
        this.message = object.get("message").getAsString();
    }

    public DMPacket(String from, String to, String message) {
        super(1, null);
        this.from = from;
        this.to = to;
        this.message = message;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("from", from);
        object.addProperty("to", to);
        object.addProperty("message", message);
        return object;
    }
}
