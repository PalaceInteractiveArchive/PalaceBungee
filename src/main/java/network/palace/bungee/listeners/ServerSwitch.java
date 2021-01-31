package network.palace.bungee.listeners;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import network.palace.bungee.PalaceBungee;

public class ServerSwitch implements Listener {

    @EventHandler
    public void onServerSwitch(ServerSwitchEvent event) {
        ProxiedPlayer proxied = event.getPlayer();
        proxied.sendMessage(new TextComponent(" "));
        PalaceBungee.getServerUtil().handleServerSwitch(proxied.getUniqueId(), event.getFrom(), proxied.getServer().getInfo());
    }
}