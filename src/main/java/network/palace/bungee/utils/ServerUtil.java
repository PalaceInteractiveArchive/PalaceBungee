package network.palace.bungee.utils;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.Server;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class ServerUtil {
    private final HashMap<String, Server> servers = new HashMap<>();

    public ServerUtil() {
        loadServers();
    }

    private void loadServers() {
        servers.clear();
//        ProxyServer.getInstance().getServers().clear();
        try {
            List<Server> servers = PalaceBungee.getMongoHandler().getServers(PalaceBungee.isTestNetwork());
            for (Server s : servers) {
                this.servers.put(s.getName(), s);
                try {
                    String[] addressList = s.getAddress().split(":");
                    ServerInfo info = ProxyServer.getInstance().constructServerInfo(s.getName(),
                            new InetSocketAddress(addressList[0], Integer.parseInt(addressList[1])), "", false);
                    ProxyServer.getInstance().getServers().put(info.getName(), info);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            PalaceBungee.getProxyServer().getLogger().info("Successfully loaded " + servers.size() + " servers!");
        } catch (Exception e) {
            PalaceBungee.getProxyServer().getLogger().log(Level.SEVERE, "Error loading servers, stopping bungee server!", e);
            System.exit(1);
        }
    }

    public ServerInfo getServerInfo(String name, boolean exact) {
        for (ServerInfo info : ProxyServer.getInstance().getServers().values()) {
            if ((exact && info.getName().equals(name)) || (!exact && info.getName().equalsIgnoreCase(name)))
                return info;
        }
        return null;
    }

    public Server getServer(String name, boolean exact) {
        if (exact) return servers.get(name);
        for (Server s : servers.values()) {
            if (s.getName().equalsIgnoreCase(name))
                return s;
        }
        return null;
    }

    public int getServerCount(net.md_5.bungee.api.connection.Server server) {
        return getServerCount(server.getInfo().getName());
    }

    public int getServerCount(String name) {
        return PalaceBungee.getMongoHandler().getServerCount(name);
    }

    public void createServer(Server server) {
        servers.put(server.getName(), server);
    }

    public void deleteServer(String name) {
        servers.remove(name);
    }
}
