package network.palace.bungee.commands.staff;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;

public class DirectChatCommand extends PalaceCommand {

    public DirectChatCommand() {
        super("dc", RANK.CM);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Direct Chat: Bypass cross-server chat and send a chat message directly to the server you're on.");
            player.sendMessage(ChatColor.RED + "/dc [Message]");
            return;
        }
        StringBuilder msg = new StringBuilder();
        for (String arg : args) {
            msg.append(arg).append(" ");
        }
        String message = msg.toString().trim();
        try {
            player.sendMessage(ChatColor.YELLOW + "Sending '" + ChatColor.AQUA + message + ChatColor.YELLOW + "' directly to " + player.getServerName() + "...");
            player.chat(message);
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred sending chat directly to " + player.getServerName() + "!");
        }
    }
}
