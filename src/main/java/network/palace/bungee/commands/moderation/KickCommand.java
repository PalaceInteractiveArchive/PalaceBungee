package network.palace.bungee.commands.moderation;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.*;
import network.palace.bungee.messages.packets.DMPacket;
import network.palace.bungee.messages.packets.KickPacket;
import network.palace.bungee.utils.DateUtil;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KickCommand extends PalaceCommand {

    public KickCommand() {
        super("kick", Rank.TRAINEE);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.GREEN + "Kicking:");
            player.sendMessage(ChatColor.AQUA + "/kick [Player] [Reason]");
            player.sendMessage(ChatColor.GREEN + "Example: " + ChatColor.YELLOW + "/kick " + player.getUsername() + " Please disable your hacked client");
            return;
        }
        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            message.append(args[i]).append(" ");
        }
        message.substring(0, message.length() - 1);
        Player targetPlayer = PalaceBungee.getPlayer(args[0]);
        if (targetPlayer != null) {
            targetPlayer.kickPlayer(message.toString());
        } else {
            try {
                String target = args[0];
                UUID targetProxy = PalaceBungee.getMongoHandler().findPlayer(target);
                if (targetProxy == null) {
                    player.sendMessage(ChatColor.RED + "Player not found!");
                    return;
                }
                KickPacket packet = new KickPacket(player.getUniqueId(), message.toString());
                PalaceBungee.getMessageHandler().sendMessage(packet, "all_proxies", "fanout");
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "There was an error kicking that user. Try again soon!");
            }
        }
    }
}
