package network.palace.bungee.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Created by Marc on 7/16/16
 */
@AllArgsConstructor
public class Mute {
    @Getter private UUID uuid;
    @Getter private String name;
    @Getter @Setter private boolean muted;
    @Getter @Setter private long release;
    @Getter @Setter private String reason;
    @Getter @Setter private String source;
}