package network.palace.bungee.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class Mute {
    private UUID uuid;
    @Setter private boolean muted;
    @Setter private long created;
    @Setter private long expires;
    @Setter private String reason;
    @Setter private String source;
}