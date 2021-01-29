package network.palace.bungee.listeners;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Server;

public class PlayerChat implements Listener {

    @EventHandler
    public void onChat(ChatEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer)) return;
        Player player = PalaceBungee.getPlayer(((ProxiedPlayer) event.getSender()).getUniqueId());
        if (player == null) {
            event.setCancelled(true);
            return;
        }
        if (event.isProxyCommand()) return;
        if (event.isCommand()) return;
        String msg = event.getMessage();

        switch (player.getChannel()) {
            case "party": {
                PalaceBungee.getProxyServer().getPluginManager().dispatchCommand(player.getProxiedPlayer(), "pchat " + msg);
                event.setCancelled(true);
                return;
            }
            case "staff": {
                PalaceBungee.getProxyServer().getPluginManager().dispatchCommand(player.getProxiedPlayer(), "sc " + msg);
                event.setCancelled(true);
                return;
            }
            case "admin": {
                PalaceBungee.getProxyServer().getPluginManager().dispatchCommand(player.getProxiedPlayer(), "ho " + msg);
                event.setCancelled(true);
                return;
            }
            default: {
                Server server = PalaceBungee.getServerUtil().getServer(player.getServerName(), true);
                if (server == null) return;
                if (server.isPark()) {
                    event.setCancelled(true);
                    try {
                        PalaceBungee.getChatUtil().sendOutgoingParkChatMessage(player, msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "There was an error sending your chat message! Please try again in a few minutes. If the issue continues, try logging out and back in.");
                    }
                }
            }
        }
    }
}
