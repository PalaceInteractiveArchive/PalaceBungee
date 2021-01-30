package network.palace.bungee.commands.moderation;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;

import java.util.List;

public class BannedProvidersCommand extends PalaceCommand {

    public BannedProvidersCommand() {
        super("bannedproviders", Rank.LEAD);
    }

    @Override
    public void execute(Player player, String[] args) {
        List<String> bannedProviders = PalaceBungee.getMongoHandler().getBannedProviders();
        if (bannedProviders.isEmpty()) {
            player.sendMessage(ChatColor.GREEN + "No Banned Providers!");
            return;
        }
        StringBuilder msg = new StringBuilder(ChatColor.GREEN + "Banned Providers:");
        for (String s : bannedProviders) {
            msg.append("\n- ").append(s);
        }
        player.sendMessage(msg.toString());
    }
}