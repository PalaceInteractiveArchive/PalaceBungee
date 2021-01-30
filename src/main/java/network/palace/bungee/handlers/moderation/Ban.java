package network.palace.bungee.handlers.moderation;

import lombok.Getter;

import java.util.UUID;

public class Ban {
    private final UUID uuid;
    @Getter private final String name;
    @Getter private final boolean permanent;
    @Getter private final long created;
    @Getter private final long expires;
    @Getter private final String reason;
    @Getter private final String source;

    public Ban(UUID uuid, String name, boolean permanent, long expires, String reason, String source) {
        this(uuid, name, permanent, System.currentTimeMillis(), expires, reason, source);
    }

    public Ban(UUID uuid, String name, boolean permanent, long created, long expires, String reason, String source) {
        this.uuid = uuid;
        this.name = name;
        this.permanent = permanent;
        this.created = created;
        this.expires = expires;
        this.reason = reason;
        this.source = source;
    }

    public UUID getUniqueId() {
        return uuid;
    }
}
