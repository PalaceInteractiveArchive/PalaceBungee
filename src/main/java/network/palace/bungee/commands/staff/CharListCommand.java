package network.palace.bungee.commands.staff;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;

import java.util.Set;
import java.util.TreeMap;

public class CharListCommand extends PalaceCommand {

    public CharListCommand() {
        super("charlist", Rank.CHARACTER);
    }

    @Override
    public void execute(Player player, String[] args) {
        TreeMap<Rank, Set<String>> players = PalaceBungee.getMongoHandler().getRankList(rank -> rank.equals(Rank.CHARACTER));
        Set<String> members = players.get(Rank.CHARACTER);
        if (members == null || members.isEmpty()) {
            player.sendMessage(ChatColor.RED + "There are no Characters online!");
            return;
        }
        ComponentBuilder comp = new ComponentBuilder("Online Characters (" + members.size() + "): ").color(Rank.CHARACTER.getTagColor());
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
