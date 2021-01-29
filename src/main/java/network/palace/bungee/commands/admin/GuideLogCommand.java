package network.palace.bungee.commands.admin;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;

import java.util.UUID;

public class GuideLogCommand extends PalaceCommand {

    public GuideLogCommand() {
        super("glog", Rank.LEAD);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/guidelog [Username]");
            return;
        }
        String username = args[0];
        Player tp = PalaceBungee.getPlayer(username);
        UUID uuid;
        if (tp == null) {
            uuid = PalaceBungee.getMongoHandler().usernameToUUID(username);
            if (uuid == null) {
                player.sendMessage(ChatColor.RED + "Player not found!");
                return;
            }
        } else {
            uuid = tp.getUniqueId();
            username = tp.getUsername();
        }
        String[] stats = PalaceBungee.getMongoHandler().getHelpActivity(uuid).split(",");
        player.sendMessage(ChatColor.GREEN + "Guide Log for " + username + ": \n" + ChatColor.YELLOW +
                "Last Day: " + stats[0] + " requests\n" +
                "Last Week: " + stats[1] + " requests\n" +
                "Last Month: " + stats[2] + " requests\n" +
                "All Time: " + stats[3] + " requests");
    }
}
