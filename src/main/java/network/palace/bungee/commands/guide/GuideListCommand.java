package network.palace.bungee.commands.guide;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.RankTag;

import java.util.Set;
import java.util.TreeMap;

public class GuideListCommand extends PalaceCommand {

    public GuideListCommand() {
        super("guidelist", Rank.TRAINEE, RankTag.GUIDE);
    }

    @Override
    public void execute(Player player, String[] args) {
        TreeMap<RankTag, Set<String>> players = PalaceBungee.getMongoHandler().getRankTagList(tag -> tag.equals(RankTag.GUIDE));
        Set<String> members = players.get(RankTag.GUIDE);
        if (members == null || members.isEmpty()) {
            player.sendMessage(ChatColor.RED + "There are no Guides online!");
            return;
        }
        ComponentBuilder comp = new ComponentBuilder("Online Guides (" + members.size() + "): ").color(RankTag.GUIDE.getColor());
        int i = 0;
        for (String s : members) {
            String[] list = s.split(":");
            comp.append(list[0], ComponentBuilder.FormatRetention.NONE).color(ChatColor.GREEN)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Currently on: ")
                            .color(ChatColor.GREEN).append(list[1]).color(ChatColor.AQUA).create()));
            if (i < (members.size() - 1)) comp.append(", ");
            i++;
        }
        player.sendMessage(comp.create());
    }
}
