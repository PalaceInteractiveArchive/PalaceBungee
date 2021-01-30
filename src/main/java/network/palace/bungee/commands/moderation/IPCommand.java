package network.palace.bungee.commands.moderation;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import org.bson.Document;

public class IPCommand extends PalaceCommand {

    public IPCommand() {
        super("ip", Rank.LEAD);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "/ip [Player]");
            return;
        }
        Document doc = PalaceBungee.getMongoHandler().getPlayer(args[0], new Document("ip", true).append("online", true));
        if (doc == null || !doc.containsKey("ip") || !doc.containsKey("online")) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }
        player.sendMessage(ChatColor.GREEN + "IP of " + args[0] + " is " + doc.getString("ip"));
    }
}