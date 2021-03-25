package network.palace.bungee.handlers;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.config.ServerInfo;
import network.palace.bungee.PalaceBungee;

import java.util.UUID;

public class Server {
    private final UUID uuid = UUID.randomUUID();
    @Getter private final String name;
    @Getter private final String address;
    @Getter private final boolean park;
    @Getter @Setter private int gameMaxPlayers;
    @Getter private final String serverType;
    @Setter private boolean online;
    private int count = 0;
    private long lastDBUpdate = 0;

    public Server(String name, String address, boolean park, String serverType, boolean online) {
        this.name = name;
        this.address = address;
        this.park = park;
        this.serverType = serverType;
        this.online = online;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public boolean isOnline() {
        if (System.currentTimeMillis() - lastDBUpdate < 3000) return online;
        dbUpdate();
        return online;
    }

    public int getCount() {
        if (System.currentTimeMillis() - lastDBUpdate < 3000) return count;
        dbUpdate();
        return count;
    }

    private void dbUpdate() {
        count = PalaceBungee.getMongoHandler().getServerCount(name);
        online = PalaceBungee.getMongoHandler().isServerOnline(name);
        lastDBUpdate = System.currentTimeMillis();
    }

    public void join(Player player) {
        ServerInfo info = PalaceBungee.getServerUtil().getServerInfo(name, true);
        if (info != null) player.getProxiedPlayer().ifPresent(p -> p.connect(info));
    }
}