package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.messages.packets.DMPacket;

import java.util.UUID;

public class MsgCommand extends PalaceCommand {

    public MsgCommand() {
        super("msg");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.GREEN + "Direct Messaging:");
            player.sendMessage(ChatColor.AQUA + "/msg [Player] [Message]");
            player.sendMessage(ChatColor.GREEN + "Example: " + ChatColor.YELLOW + "/msg " + player.getUsername() + " Hello there!");
            return;
        }
        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            message.append(args[i]).append(" ");
        }
        message.substring(0, message.length() - 1);
        Player targetPlayer = PalaceBungee.getPlayer(args[0]);
        if (targetPlayer != null) {
            player.sendMessage(ChatColor.GREEN + "You" + ChatColor.LIGHT_PURPLE + " -> " + ChatColor.GREEN + targetPlayer.getUsername() + ": " + ChatColor.WHITE + message);
            targetPlayer.sendMessage(ChatColor.GREEN + player.getUsername() + ChatColor.LIGHT_PURPLE + " -> " + ChatColor.GREEN + "You: " + ChatColor.WHITE + message);
            targetPlayer.mention();
            player.setReplyTo(targetPlayer.getUniqueId());
            player.setReplyTime(System.currentTimeMillis());
            targetPlayer.setReplyTo(player.getUniqueId());
            targetPlayer.setReplyTime(System.currentTimeMillis());
        } else {
            try {
                String target = args[0];
                UUID targetProxy = PalaceBungee.getMongoHandler().findPlayer(target);
                if (targetProxy == null) {
                    player.sendMessage(ChatColor.RED + "Player not found!");
                    return;
                }
                DMPacket packet = new DMPacket(player.getUsername(), target, message.toString(), player.getUniqueId(), null, PalaceBungee.getProxyID(), true);
                PalaceBungee.getMessageHandler().sendToProxy(packet, targetProxy);
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "There was an error sending your private message. Try again soon!");
            }
        }
    }
}
