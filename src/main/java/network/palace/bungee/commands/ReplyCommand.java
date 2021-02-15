package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.messages.packets.DMPacket;
import network.palace.bungee.utils.EmojiUtil;

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
        if (player.getTotalOnlineTime() < 600) {
            player.sendMessage(ChatColor.RED + "New guests must be on the server for at least 10 minutes before talking in chat." +
                    ChatColor.DARK_AQUA + " Learn more at palnet.us/rules");
            return;
        }
        boolean onlyStaff = player.isMuted();
        UUID replyTo = player.getReplyTo();
        long replyTime = player.getReplyTime();
        if (replyTo == null || replyTime == 0) {
            player.sendMessage(ChatColor.AQUA + "No one to reply to! Message someone with " + ChatColor.YELLOW + "/msg [Username] [Message]");
            return;
        }
        String message = String.join(" ", args);
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
            if (onlyStaff && targetPlayer.getRank().getRankId() < Rank.TRAINEE.getRankId()) {
                player.sendMessage(ChatColor.RED + "You can't direct message this player while muted.");
                return;
            }
            try {
                String processed = PalaceBungee.getChatUtil().processChatMessage(player, message, "DM", true);
                if (processed == null) return;

                PalaceBungee.getChatUtil().analyzeMessage(player.getUniqueId(), player.getRank(), processed, "DM Reply to " + args[0], () -> {
                    try {
                        String msg;
                        try {
                            msg = EmojiUtil.convertMessage(player, processed);
                        } catch (IllegalArgumentException e) {
                            player.sendMessage(ChatColor.RED + e.getMessage());
                            return;
                        }
                        player.sendMessage(ChatColor.GREEN + "You" + ChatColor.LIGHT_PURPLE + " -> " + ChatColor.GREEN + targetPlayer.getUsername() + ": " + ChatColor.WHITE + msg);
                        targetPlayer.sendMessage(ChatColor.GREEN + player.getUsername() + ChatColor.LIGHT_PURPLE + " -> " + ChatColor.GREEN + "You: " + ChatColor.WHITE + msg);
                        targetPlayer.mention();
                        player.setReplyTo(targetPlayer.getUniqueId());
                        player.setReplyTime(System.currentTimeMillis());
                        targetPlayer.setReplyTo(player.getUniqueId());
                        targetPlayer.setReplyTime(System.currentTimeMillis());
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "There was an error sending your direct message. Try again soon!");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "There was an error sending your direct message. Try again soon!");
            }
        } else {
            try {
                if (onlyStaff && PalaceBungee.getMongoHandler().getRank(replyTo).getRankId() < Rank.TRAINEE.getRankId()) {
                    player.sendMessage(ChatColor.RED + "You can't direct message this player while muted.");
                    return;
                }
                String username = PalaceBungee.getMongoHandler().uuidToUsername(replyTo);
                String processed = PalaceBungee.getChatUtil().processChatMessage(player, message, "DM", true);
                if (processed == null) return;

                PalaceBungee.getChatUtil().analyzeMessage(player.getUniqueId(), player.getRank(), processed, "DM Reply to " + username, () -> {
                    try {
                        String msg;
                        try {
                            msg = EmojiUtil.convertMessage(player, processed);
                        } catch (IllegalArgumentException e) {
                            player.sendMessage(ChatColor.RED + e.getMessage());
                            return;
                        }
                        UUID targetProxy = PalaceBungee.getMongoHandler().findPlayer(replyTo);
                        if (targetProxy == null) {
                            player.sendMessage(ChatColor.RED + "Player not found!");
                            return;
                        }
                        DMPacket packet = new DMPacket(player.getUsername(), username, msg, player.getUniqueId(), null, PalaceBungee.getProxyID(), true, player.getRank().getRankId() >= Rank.CHARACTER.getRankId());
                        PalaceBungee.getMessageHandler().sendToProxy(packet, targetProxy);
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "There was an error sending your direct message. Try again soon!");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "There was an error sending your direct message. Try again soon!");
            }
        }
    }
}
