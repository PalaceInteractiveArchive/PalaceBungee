package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.messages.packets.DMPacket;

import java.util.UUID;

public class ReplyCommand extends PalaceCommand {

    public ReplyCommand() {
        super("reply", "r");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "/reply [Message]");
            return;
        }
        UUID replyTo = player.getReplyTo();
        long replyTime = player.getReplyTime();
        if (replyTo == null || replyTime == 0) {
            player.sendMessage(ChatColor.AQUA + "No one to reply to! Message someone with " + ChatColor.YELLOW + "/msg [Username] [Message]");
            return;
        }
        StringBuilder message = new StringBuilder();
        for (String arg : args) {
            message.append(arg).append(" ");
        }
        message.substring(0, message.length() - 1);
        Player targetPlayer = PalaceBungee.getPlayer(replyTo);
        if (player.getRank().getRankId() < Rank.CHARACTER.getRankId() && !PalaceBungee.getConfigUtil().isDmEnabled()) {
            player.sendMessage(ChatColor.RED + "Direct messages are currently disabled.");
            return;
        }
        if (targetPlayer != null) {
            if (player.getRank().getRankId() < Rank.CHARACTER.getRankId() && (!targetPlayer.isDmEnabled() || (targetPlayer.isIgnored(player.getUniqueId()) && targetPlayer.getRank().getRankId() < Rank.CHARACTER.getRankId()))) {
                player.sendMessage(ChatColor.RED + "This person has messages disabled!");
                return;
            }
            player.sendMessage(ChatColor.GREEN + "You" + ChatColor.LIGHT_PURPLE + " -> " + ChatColor.GREEN + targetPlayer.getUsername() + ": " + ChatColor.WHITE + message);
            targetPlayer.sendMessage(ChatColor.GREEN + player.getUsername() + ChatColor.LIGHT_PURPLE + " -> " + ChatColor.GREEN + "You: " + ChatColor.WHITE + message);
            targetPlayer.mention();
            player.setReplyTo(targetPlayer.getUniqueId());
            player.setReplyTime(System.currentTimeMillis());
            targetPlayer.setReplyTo(player.getUniqueId());
            targetPlayer.setReplyTime(System.currentTimeMillis());
        } else {
            try {
                String username = PalaceBungee.getMongoHandler().uuidToUsername(replyTo);
                UUID targetProxy = PalaceBungee.getMongoHandler().findPlayer(replyTo);
                if (targetProxy == null) {
                    player.sendMessage(ChatColor.RED + "Player not found!");
                    return;
                }
                DMPacket packet = new DMPacket(player.getUsername(), username, message.toString(), player.getUniqueId(), replyTo, PalaceBungee.getProxyID(), true, player.getRank().getRankId() >= Rank.CHARACTER.getRankId());
                PalaceBungee.getMessageHandler().sendToProxy(packet, targetProxy);
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "There was an error sending your private message. Try again soon!");
            }
        }
    }
}
