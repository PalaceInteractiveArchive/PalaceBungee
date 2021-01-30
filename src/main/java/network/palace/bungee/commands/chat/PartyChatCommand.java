package network.palace.bungee.commands.chat;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;

public class PartyChatCommand extends PalaceCommand {

    public PartyChatCommand() {
        super("pchat");
    }

    @Override
    public void execute(Player player, String[] args) {
        try {
            PalaceBungee.getPartyUtil().chat(player, String.join(" ", args));
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred while sending your party chat message! Please try again in a few minutes.");
        }
    }
}
