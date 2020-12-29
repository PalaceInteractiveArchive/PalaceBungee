package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;

public class WhereAmICommand extends PalaceCommand {

    public WhereAmICommand() {
        super("whereami");
    }

    @Override
    public void execute(Player player, String[] args) {
        player.sendMessage(ChatColor.BLUE + "You are on the server " + ChatColor.GOLD + player.getServerName());
    }
}
