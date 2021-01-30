package network.palace.bungee.handlers.moderation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class AddressBan {
    @Getter private final String address;
    @Getter private final String reason;
    @Getter private final String source;
}
