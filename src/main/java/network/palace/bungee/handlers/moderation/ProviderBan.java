package network.palace.bungee.handlers.moderation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ProviderBan {
    @Getter private final String provider, source;
}