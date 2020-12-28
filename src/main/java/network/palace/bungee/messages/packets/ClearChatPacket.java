package network.palace.bungee.messages.packets;

import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.UUID;

public class ClearChatPacket extends MQPacket {
    @Getter private final String chat;
    @Getter private final String source;
    @Getter private final UUID target;

    public ClearChatPacket(JsonObject object) {
        super(PacketID.Global.CLEARCHAT.getId(), object);
        this.chat = object.get("chat").getAsString();
        this.source = object.get("source").getAsString();
        this.target = object.has("target") ? UUID.fromString(object.get("target").getAsString()) : null;
    }

    public ClearChatPacket(String chat, String source) {
        this(chat, source, null);
    }

    public ClearChatPacket(String chat, String source, UUID target) {
        super(PacketID.Global.CLEARCHAT.getId(), null);
        this.chat = chat;
        this.source = source;
        this.target = target;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("chat", chat);
        object.addProperty("source", source);
        if (target != null) object.addProperty("target", target.toString());
        return object;
    }
}
