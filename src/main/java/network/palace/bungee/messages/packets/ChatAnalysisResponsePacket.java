package network.palace.bungee.messages.packets;

import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.UUID;

public class ChatAnalysisResponsePacket extends MQPacket {
    @Getter private final UUID requestId;
    @Getter private final boolean okay;

    // Okay fields
    @Getter private String message;

    // Not okay fields
    @Getter private String filterCaught, offendingText;

    public ChatAnalysisResponsePacket(JsonObject object) {
        super(PacketID.Global.CHAT_ANALYSIS_RESPONSE.getId(), object);
        this.requestId = UUID.fromString(object.get("requestId").getAsString());
        this.okay = object.get("okay").getAsBoolean();

        if (object.has("message")) this.message = object.get("message").getAsString();

        if (object.has("filter_caught")) this.filterCaught = object.get("filter_caught").getAsString();
        if (object.has("offending_text")) this.offendingText = object.get("offending_text").getAsString();
    }

    public ChatAnalysisResponsePacket(UUID requestId, boolean okay, String message) {
        super(PacketID.Global.CHAT_ANALYSIS_RESPONSE.getId(), null);
        this.requestId = requestId;
        this.okay = okay;
        this.message = message;
    }

    public ChatAnalysisResponsePacket(UUID requestId, boolean okay, String filterCaught, String offendingText) {
        super(PacketID.Global.CHAT_ANALYSIS_RESPONSE.getId(), null);
        this.requestId = requestId;
        this.okay = okay;
        this.filterCaught = filterCaught;
        this.offendingText = offendingText;
    }

    @Override
    public JsonObject getJSON() {
        // Should never need to send responses from here
        return null;
    }
}
