package network.palace.bungee.commands.staff;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class StaffListCommand extends PalaceCommand {

    public StaffListCommand() {
        super("stafflist", Rank.TRAINEE);
    }

    @Override
    public void execute(Player player, String[] args) {
        TreeMap<Rank, Set<String>> players = PalaceBungee.getMongoHandler().getStaffList();
        player.sendMessage(ChatColor.GREEN + "Online Staff Members:");
        for (Map.Entry<Rank, Set<String>> entry : players.entrySet()) {
            sendRankMessage(player, entry.getKey(), entry.getValue());
        }
    }

    private void sendRankMessage(Player player, Rank rank, Set<String> members) {
        ComponentBuilder comp = new ComponentBuilder(rank.getName() +
                (rank.equals(Rank.MEDIA) ? "" : "s") +
                ": (" + members.size() + ") ").color(rank.getTagColor());
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
