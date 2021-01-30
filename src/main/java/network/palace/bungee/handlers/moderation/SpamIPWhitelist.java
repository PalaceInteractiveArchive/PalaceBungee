package network.palace.bungee.handlers.moderation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SpamIPWhitelist {
    private final String address;
    private final int limit;
}