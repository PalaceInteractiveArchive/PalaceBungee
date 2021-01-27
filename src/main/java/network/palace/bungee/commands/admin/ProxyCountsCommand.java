package network.palace.bungee.commands.admin;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProxyCountsCommand extends PalaceCommand {

    public ProxyCountsCommand() {
        super("proxycounts", Rank.DEVELOPER);
    }

    @Override
    public void execute(Player player, String[] args) {
        HashMap<UUID, Integer> proxyCounts = PalaceBungee.getMongoHandler().getProxyCounts();
        player.sendMessage(ChatColor.GREEN + "Proxy Player Counts:");
        int i = 1;
        for (Map.Entry<UUID, Integer> entry : proxyCounts.entrySet()) {
            player.sendMessage(ChatColor.GREEN + "Proxy" + (i++) + " (" + entry.getKey().toString() + "): " + entry.getValue());
        }
    }
}
