package network.palace.bungee.commands.admin;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;

public class ProxyVersionCommand extends PalaceCommand {

    public ProxyVersionCommand() {
        super("proxyversion", Rank.DEVELOPER, "proxyver");
    }

    @Override
    public void execute(Player player, String[] args) {
        player.sendMessage(ChatColor.GREEN + "Bungee " + PalaceBungee.getProxyID().toString() +
                " currently running PalaceBungee v" + PalaceBungee.getInstance().getDescription().getVersion());
    }
}
