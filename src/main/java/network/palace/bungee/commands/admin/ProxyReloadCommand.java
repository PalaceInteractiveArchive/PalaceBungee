package network.palace.bungee.commands.admin;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.messages.packets.ProxyReloadPacket;

public class ProxyReloadCommand extends PalaceCommand {

    public ProxyReloadCommand() {
        super("proxyreload", Rank.DEVELOPER);
    }

    @Override
    public void execute(Player player, String[] args) {
        try {
            PalaceBungee.getMessageHandler().sendMessage(new ProxyReloadPacket(), PalaceBungee.getMessageHandler().ALL_PROXIES);
            player.sendMessage(ChatColor.GREEN + "Proxy settings reloaded!");
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "There was an error reloading proxy settings!");
        }
    }
}
