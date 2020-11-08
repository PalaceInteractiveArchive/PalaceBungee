package network.palace.bungee.commands.chat;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.messages.packets.MessageByRankPacket;

public class AdminChatCommand extends PalaceCommand {

    public AdminChatCommand() {
        super("ho", Rank.DEVELOPER);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/ho [Message]");
            return;
        }
        try {
            StringBuilder message = new StringBuilder();
            for (String arg : args) {
                message.append(arg).append(" ");
            }
            MessageByRankPacket packet = new MessageByRankPacket(ChatColor.RED + "[ADMIN CHAT] " + ChatColor.GRAY + player.getUsername() + ": " + ChatColor.WHITE +
                    ChatColor.translateAlternateColorCodes('&', message.toString().trim()), Rank.DEVELOPER, false);
            PalaceBungee.getMessageHandler().sendMessage(packet, "all_proxies", "fanout");
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "There was an error executing this command!");
        }
    }
}
