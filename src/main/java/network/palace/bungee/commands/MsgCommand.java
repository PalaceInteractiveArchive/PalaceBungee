package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.messages.packets.DMPacket;

public class MsgCommand extends PalaceCommand {

    public MsgCommand() {
        super("msg");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.GREEN + "Private Messaging:");
            player.sendMessage(ChatColor.AQUA + "/msg [Player] [Message]");
            player.sendMessage(ChatColor.GREEN + "Example: " + ChatColor.YELLOW + "/msg MickeyMouse Hello there!");
            return;
        }
        String message = "";
        for (int i = 1; i < args.length; i++) {
            message = args[i] + " ";
        }
        message.substring(0, message.length() - 1);
//        Player targetPlayer = PalaceBungee.getPlayer(args[0]);
//        if (targetPlayer != null) {
//            player.sendMessage(ChatColor.GREEN + "You " + ChatColor.LIGHT_PURPLE + "-> " + ChatColor.GREEN + targetPlayer.getName() + ": " + ChatColor.WHITE + message);
//            targetPlayer.sendMessage(ChatColor.GREEN + targetPlayer.getName() + ChatColor.LIGHT_PURPLE + "-> " + ChatColor.GREEN + "You: " + ChatColor.WHITE + message);
//        } else {
        try {
            String target = args[0];
            DMPacket packet = new DMPacket(player.getUsername(), target, message);
            PalaceBungee.getMessageHandler().sendMessage(packet, "dm_global", "fanout");

            player.sendMessage(ChatColor.GREEN + "You " + ChatColor.LIGHT_PURPLE + "-> " + ChatColor.GREEN + target + ": " + ChatColor.WHITE + message);
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "There was an error sending your private message. Try again soon!");
        }
//        }
    }
}
