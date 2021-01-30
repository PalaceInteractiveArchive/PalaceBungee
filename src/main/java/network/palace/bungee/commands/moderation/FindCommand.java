package network.palace.bungee.commands.moderation;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;

public class FindCommand extends PalaceCommand {

    public FindCommand() {
        super("find", Rank.TRAINEE);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "/find [Player]");
            return;
        }
        String server = PalaceBungee.getMongoHandler().getPlayerServer(args[0]);
        if (server == null) {
            player.sendMessage(ChatColor.RED + args[0] + " is not online!");
            return;
        }
        player.sendMessage(ChatColor.BLUE + args[0] + " is on the server " + ChatColor.GOLD + server);
    }
}