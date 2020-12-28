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
        String message = "";
        StringBuilder msg = new StringBuilder();
        for (String s : args) {
            msg.append(s).append(" ");
        }
        try {
            PalaceBungee.getPartyUtil().chat(player, msg.toString().trim());
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred while sending your party chat message! Please try again in a few minutes.");
        }
    }
}
