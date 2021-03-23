package network.palace.bungee.commands.moderation;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;

public class UnbanProviderCommand extends PalaceCommand {

    public UnbanProviderCommand() {
        super("unbanprovider", RANK.CM);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/unbanprovider [Provider]");
            return;
        }
        String provider = String.join(" ", args);
        try {
            PalaceBungee.getMongoHandler().unbanProvider(provider);
            PalaceBungee.getModerationUtil().announceUnban("Provider " + provider, player.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred while unbanning that ISP. Check console for errors.");
        }
    }
}