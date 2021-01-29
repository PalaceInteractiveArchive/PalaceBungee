package network.palace.bungee.commands.admin;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;

public class MsgToggleCommand extends PalaceCommand {

    public MsgToggleCommand() {
        super("msgtoggle", Rank.DEVELOPER);
    }

    @Override
    public void execute(Player player, String[] args) {
        player.setDmEnabled(!player.isDmEnabled());
        String modifier = player.isDmEnabled() ? ChatColor.GREEN + "enabled " : ChatColor.RED + "disabled ";
        player.sendMessage(ChatColor.YELLOW + "You have " + modifier + ChatColor.YELLOW +
                "receiving direct messages!");
    }
}