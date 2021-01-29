package network.palace.bungee.handlers;

import java.util.UUID;

public class PalaceCallback {

    public interface PlayerCallback {
        void run(Player player);
    }

    public interface UUIDCallback {
        void run(UUID uuid);
    }
}
