package network.palace.bungee.listeners;

import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.ProtocolConstants;

public class ProxyPing implements Listener {

    @EventHandler
    public void onProxyPing(ProxyPingEvent event) {
        try {
//            List<String> info = new ArrayList<>(PalaceBungee.getInfo());
//            ServerPing.PlayerInfo[] infolist = new ServerPing.PlayerInfo[info.size()];
//            for (int i = 0; i < info.size(); i++) {
//                infolist[i] = new ServerPing.PlayerInfo(ChatColor.translateAlternateColorCodes('&', info.get(i)), "");
//            }
            ServerPing response = event.getResponse();
            ServerPing.Protocol p = response.getVersion();
            int remoteProtocol = event.getConnection().getVersion();

            if (remoteProtocol > ProtocolConstants.HIGHEST_VERSION.getProtocolId()) {
                p.setProtocol(ProtocolConstants.HIGHEST_VERSION.getProtocolId());
            } else p.setProtocol(Math.max(remoteProtocol, ProtocolConstants.LOWEST_VERSION.getProtocolId()));

            p.setName(ProtocolConstants.getVersionString());

            event.setResponse(new ServerPing(p, new ServerPing.Players(2000, PalaceBungee.getInstance().getProxy().getOnlineCount(), null),
                    PalaceBungee.getConfigUtil().getMotdComponent(), PalaceBungee.getConfigUtil().getFavicon()));

//            if (PalaceBungee.isMaintenance()) {
//                event.setResponse(new ServerPing(p, new ServerPing.Players(0, 0, infolist),
//                        ChatColor.translateAlternateColorCodes('&', PalaceBungee.getMOTDMmaintenance()),
//                        PalaceBungee.getServerIcon()));
//            } else {
//                event.setResponse(new ServerPing(p, new ServerPing.Players(2000,
//                        tracker ? PalaceBungee.getProxyServer().getOnlineCount() : PalaceBungee.getOnlineCount(), infolist),
//                        ChatColor.translateAlternateColorCodes('&', PalaceBungee.getMOTD()), PalaceBungee.getServerIcon()));
//            }
        } catch (Exception ignored) {
        }
    }
}
