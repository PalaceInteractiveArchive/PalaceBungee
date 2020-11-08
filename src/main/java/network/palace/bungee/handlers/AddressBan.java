package network.palace.bungee.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class AddressBan {
    @Getter private String address;
    @Getter private String reason;
    @Getter private String source;
}
