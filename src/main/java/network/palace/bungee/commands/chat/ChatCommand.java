package network.palace.bungee.commands.chat;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.RankTag;

import java.util.ArrayList;
import java.util.List;

public class ChatCommand extends PalaceCommand {

    public ChatCommand() {
        super("chat");
    }

    @Override
    public void execute(Player player, String[] args) {
        try {
            List<String> list = new ArrayList<>();
            list.add("all");
            list.add("party");
            if (player.getRank().getRankId() >= Rank.TRAINEE.getRankId()) {
                list.add("guide");
                list.add("staff");
                if (player.getRank().getRankId() >= Rank.DEVELOPER.getRankId()) {
                    list.add("admin");
                }
            } else if (player.hasTag(RankTag.GUIDE)) {
                list.add("guide");
            }
            if (args.length <= 0) {
                StringBuilder m = new StringBuilder(ChatColor.AQUA + "You are currently in the " + ChatColor.GREEN + player.getChannel() +
                        ChatColor.AQUA + " channel. You can speak in the following channels:");
                for (String s : list) {
                    m.append(ChatColor.GREEN).append("\n- ").append(ChatColor.AQUA).append(s);
                }
                m.append("\n\nExample: ").append(ChatColor.GREEN).append("/chat all ").append(ChatColor.AQUA).append("switches you to main chat");
                player.sendMessage(m.toString());
                return;
            }
            String channel = args[0].toLowerCase();
            if (!list.contains(channel)) {
                player.sendMessage(ChatColor.RED + "You can't join that channel, or it doesn't exist!");
                return;
            }
            if (channel.equals("party") && PalaceBungee.getMongoHandler().getPartyByMember(player.getUniqueId()) == null) {
                player.sendMessage(ChatColor.RED + "You aren't in a party! Create a party with " + ChatColor.GREEN + "/party create");
                return;
            }
            player.setChannel(channel);
            player.sendMessage(ChatColor.GREEN + "You have selected the " + ChatColor.AQUA + channel +
                    ChatColor.GREEN + " channel");
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "There was an error running that command, try again in a few minutes! If the issue continues, reach out to a staff member for help.");
        }
    }
}
