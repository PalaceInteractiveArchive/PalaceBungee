package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;

public class OnlineCountCommand extends PalaceCommand {

    public OnlineCountCommand() {
        super("oc");
    }

    @Override
    public void execute(Player player, String[] args) {
        player.sendMessage(ChatColor.GREEN + "\nTotal Players Online: " + PalaceBungee.getMongoHandler().getOnlineCount() + "\n");
    }
}