package network.palace.bungee.messages.packets;

import com.google.gson.JsonObject;
import lombok.Getter;
import network.palace.bungee.handlers.Rank;

public class MessageByRankPacket extends MQPacket {
    @Getter private final String message;
    @Getter private final Rank rank;
    @Getter private final boolean exact;

    public MessageByRankPacket(JsonObject object) {
        super(PacketID.Global.STAFFCHAT.getId(), object);
        this.message = object.get("message").getAsString();
        this.rank = Rank.fromString(object.get("rank").getAsString());
        this.exact = object.get("exact").getAsBoolean();
    }

    public MessageByRankPacket(String message, Rank rank, boolean exact) {
        super(PacketID.Global.STAFFCHAT.getId(), null);
        this.message = message;
        this.rank = rank;
        this.exact = exact;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("message", message);
        object.addProperty("rank", rank.getDBName());
        object.addProperty("exact", exact);
        return object;
    }
}
