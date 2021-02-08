package network.palace.bungee.messages.packets;

import com.google.gson.JsonObject;
import lombok.Getter;
import network.palace.bungee.handlers.Rank;

import java.util.UUID;

public class ChatAnalysisPacket extends MQPacket {
    @Getter private final UUID requestId;
    @Getter private final UUID sendingProxy;
    @Getter private final UUID sender;
    @Getter private final Rank rank;
    @Getter private final String message;
    @Getter private final String channel;
    @Getter private final long created = System.currentTimeMillis();
    @Getter private Runnable callback = null;

    public ChatAnalysisPacket(JsonObject object) {
        super(PacketID.Global.CHAT_ANALYSIS.getId(), object);
        this.requestId = UUID.fromString(object.get("requestId").getAsString());
        this.sendingProxy = UUID.fromString(object.get("sendingProxy").getAsString());
        this.sender = UUID.fromString(object.get("sender").getAsString());
        this.rank = Rank.fromString(object.get("rank").getAsString());
        this.message = object.get("message").getAsString();
        this.channel = object.get("channel").getAsString();
    }

    public ChatAnalysisPacket(UUID sender, UUID sendingProxy, Rank rank, String message, String channel, Runnable callback) {
        super(PacketID.Global.CHAT_ANALYSIS.getId(), null);
        this.requestId = UUID.randomUUID();
        this.sendingProxy = sendingProxy;
        this.sender = sender;
        this.rank = rank;
        this.message = message;
        this.channel = channel;
        this.callback = callback;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("requestId", requestId.toString());
        object.addProperty("sendingProxy", sendingProxy.toString());
        object.addProperty("sender", sender.toString());
        object.addProperty("rank", rank.getDBName());
        object.addProperty("message", message);
        object.addProperty("channel", channel);
        return object;
    }
}
