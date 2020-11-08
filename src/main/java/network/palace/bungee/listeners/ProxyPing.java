package network.palace.bungee.listeners;

import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.ProtocolConstants;

public class ProxyPing implements Listener {

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onProxyPing(ProxyPingEvent event) {
        try {
            ServerPing response = event.getResponse();
            ServerPing.Protocol p = response.getVersion();
            int remoteProtocol = event.getConnection().getVersion();

            if (remoteProtocol > ProtocolConstants.HIGHEST_VERSION.getProtocolId()) {
                p.setProtocol(ProtocolConstants.HIGHEST_VERSION.getProtocolId());
            } else p.setProtocol(Math.max(remoteProtocol, ProtocolConstants.LOWEST_VERSION.getProtocolId()));

            p.setName(ProtocolConstants.getVersionString());

            event.setResponse(new ServerPing(p, new ServerPing.Players(2000, PalaceBungee.getInstance().getProxy().getOnlineCount(), null),
                    PalaceBungee.getConfigUtil().getMotd(), PalaceBungee.getConfigUtil().getFavicon()));

            if (PalaceBungee.getConfigUtil().isMaintenance()) {
                event.setResponse(new ServerPing(p, new ServerPing.Players(0, 0, PalaceBungee.getConfigUtil().getMotdInfo()), PalaceBungee.getConfigUtil().getMaintenanceMotd(), PalaceBungee.getConfigUtil().getFavicon()));
            } else {
                event.setResponse(new ServerPing(p, new ServerPing.Players(2000, PalaceBungee.getProxyServer().getOnlineCount(),
                        PalaceBungee.getConfigUtil().getMotdInfo()), PalaceBungee.getConfigUtil().getMotd(), PalaceBungee.getConfigUtil().getFavicon()));
            }
        } catch (Exception ignored) {
        }
    }
}
