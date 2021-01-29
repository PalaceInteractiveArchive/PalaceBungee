package network.palace.bungee.messages.packets;

import com.google.gson.JsonObject;
import lombok.Getter;

public class ChatMutePacket extends MQPacket {
    // ParkChat, or server name
    @Getter private final String channel;
    @Getter private final boolean muted;

    public ChatMutePacket(JsonObject object) {
        super(PacketID.Global.CHAT_MUTED.getId(), object);
        this.channel = object.get("channel").getAsString();
        this.muted = object.get("muted").getAsBoolean();
    }

    public ChatMutePacket(String channel, boolean muted) {
        super(PacketID.Global.CHAT_MUTED.getId(), null);
        this.channel = channel;
        this.muted = muted;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("channel", channel);
        object.addProperty("muted", muted);
        return object;
    }
}
