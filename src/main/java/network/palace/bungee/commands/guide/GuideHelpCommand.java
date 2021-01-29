package network.palace.bungee.commands.guide;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.RankTag;

import java.io.IOException;
import java.util.UUID;

public class GuideHelpCommand extends PalaceCommand {

    public GuideHelpCommand() {
        super("h", Rank.TRAINEE, RankTag.GUIDE);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.AQUA + "/h accept [username] - Accept a help request");
            player.sendMessage(ChatColor.AQUA + "/h tp [username] - Teleport cross-server to a player");
            return;
        }
        Player tp = PalaceBungee.getPlayer(args[1]);
        switch (args[0].toLowerCase()) {
            case "accept": {
                UUID uuid;
                String username;
                if (tp != null) {
                    username = tp.getUsername();
                } else {
                    username = args[1];
                }
                uuid = PalaceBungee.getMongoHandler().usernameToUUID(username);
                if (uuid == null) {
                    player.sendMessage(ChatColor.RED + "Player not found!");
                    return;
                }
                try {
                    PalaceBungee.getGuideUtil().acceptHelpRequest(player, uuid, username);
                } catch (IOException e) {
                    e.printStackTrace();
                    player.sendMessage(ChatColor.RED + "An error occurred while accepting that help request. Try again in a few minutes!");
                }
                break;
            }
            case "tp": {
                String serverName, username;
                if (tp != null) {
                    serverName = tp.getServerName();
                    username = tp.getUsername();
                } else {
                    serverName = PalaceBungee.getMongoHandler().getPlayerServer(args[1]);
                    username = args[1];
                }
                if (serverName == null) {
                    player.sendMessage(ChatColor.RED + "Player not found!");
                    return;
                }
                PalaceBungee.getGuideUtil().teleport(player, username, serverName);
                break;
            }
            default: {
                player.sendMessage(ChatColor.AQUA + "/h accept [username] - Accept a help request");
                player.sendMessage(ChatColor.AQUA + "/h tp [username] - Teleport cross-server to a player");
                break;
            }
        }
    }
}