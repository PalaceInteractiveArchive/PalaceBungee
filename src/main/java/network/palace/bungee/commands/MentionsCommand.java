package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;

public class MentionsCommand extends PalaceCommand {

    public MentionsCommand() {
        super("mentions");
    }

    @Override
    public void execute(final Player player, String[] args) {
        player.setMentions(!player.hasMentions());
        player.sendMessage((player.hasMentions() ? ChatColor.GREEN : ChatColor.RED) + "You have " +
                (player.hasMentions() ? "enabled" : "disabled") + " mention notifications!");
        if (player.hasMentions()) {
            player.mention();
        }
        PalaceBungee.getMongoHandler().setSetting(player.getUniqueId(), "mentions", player.hasMentions());
    }
}