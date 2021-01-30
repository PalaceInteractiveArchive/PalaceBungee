package network.palace.bungee.commands.moderation;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.moderation.AddressBan;

public class UnbanIPCommand extends PalaceCommand {

    public UnbanIPCommand() {
        super("unbanip", Rank.LEAD, "pardonip", "pardon-ip");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/unbanip [IP Address]");
            return;
        }
        String address = args[0];
        PalaceBungee.getMongoHandler().unbanAddress(address);
        PalaceBungee.getModerationUtil().announceUnban(new AddressBan(address, "", player.getUsername()));
    }
}