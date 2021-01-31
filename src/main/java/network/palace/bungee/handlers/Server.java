package network.palace.bungee.handlers;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.config.ServerInfo;
import network.palace.bungee.PalaceBungee;

import java.util.UUID;

public class Server {
    private UUID uuid = UUID.randomUUID();
    @Getter private final String name;
    @Getter private final String address;
    @Getter private final boolean park;
    @Getter @Setter private int gameMaxPlayers;
    @Getter private final String serverType;
    @Getter @Setter private boolean online;
    private int count = 0;
    private long lastCountRetrieval = 0;

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

    public int getCount() {
        if (System.currentTimeMillis() - lastCountRetrieval < 3000) return count;
        count = PalaceBungee.getMongoHandler().getServerCount(name);
        lastCountRetrieval = System.currentTimeMillis();
        return count;
    }

    public void join(Player player) {
        ServerInfo info = PalaceBungee.getServerUtil().getServerInfo(name, true);
        if (info != null) player.getProxiedPlayer().connect(info);
    }
}