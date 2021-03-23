package network.palace.bungee.commands.chat;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;

public class ChatDelayCommand extends PalaceCommand {

    public ChatDelayCommand() {
        super("chatdelay", Rank.CM);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length <= 0) {
            player.sendMessage(ChatColor.GREEN + "The chat delay is currently " +
                    PalaceBungee.getConfigUtil().getChatDelay() + " seconds.");
            player.sendMessage(ChatColor.GREEN + "Change delay: /chatdelay [Seconds]");
            player.sendMessage(ChatColor.GREEN + "(Default is 2 seconds)");
            return;
        }
        try {
            int time = Integer.parseInt(args[0]);
            PalaceBungee.getConfigUtil().setChatDelay(time);
            PalaceBungee.getMessageHandler().sendStaffMessage(ChatColor.GREEN + "The chat delay was set to " + time + " seconds by " + player.getUsername());
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Please use a whole number :)");
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "There was an error changing the chat delay! Please try again in a few minutes.");
        }
    }
}