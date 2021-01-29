package network.palace.bungee.messages.packets;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class PacketID {

    @AllArgsConstructor
    enum Global {
        BROADCAST(1), MESSAGEBYRANK(2), PROXYRELOAD(3), DM(4), MESSAGE(5), COMPONENTMESSAGE(6),
        CLEARCHAT(7), CREATESERVER(8), DELETESERVER(9), MENTION(10), CHAT(12), CHAT_ANALYSIS(13),
        CHAT_ANALYSIS_RESPONSE(14), SEND_PLAYER(15), CHANGE_CHANNEL(16), CHAT_MUTED(17);

        @Getter private final int id;
    }
}
