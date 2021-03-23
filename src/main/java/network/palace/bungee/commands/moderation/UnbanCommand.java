package network.palace.bungee.commands.moderation;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;

import java.util.UUID;

public class UnbanCommand extends PalaceCommand {

    public UnbanCommand() {
        super("unban", RANK.CM, "pardon");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/unban [Player] [Username]");
            return;
        }
        String username = args[0];
        UUID uuid = PalaceBungee.getMongoHandler().usernameToUUID(username);
        if (uuid == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }
        try {
            PalaceBungee.getMongoHandler().unbanPlayer(uuid);
            PalaceBungee.getModerationUtil().announceUnban(username, player.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred while unbanning that player. Check console for errors.");
        }
    }
}