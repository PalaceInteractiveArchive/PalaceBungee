package network.palace.bungee.handlers;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import network.palace.bungee.PalaceBungee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class PalaceCommand extends Command implements TabExecutor {
    @Getter private final Rank rank;
    @Getter private final RankTag tag;
    protected boolean tabComplete = false, tabCompletePlayers = false;

    public PalaceCommand(String name) {
        this(name, Rank.GUEST);
    }

    public PalaceCommand(String name, String... aliases) {
        this(name, Rank.GUEST, aliases);
    }

    public PalaceCommand(String name, Rank rank) {
        this(name, rank, (RankTag) null);
    }

    public PalaceCommand(String name, Rank rank, String... aliases) {
        this(name, rank, null, aliases);
    }

    public PalaceCommand(String name, Rank rank, RankTag tag, String... aliases) {
        super(name, "", aliases);
        this.rank = rank;
        this.tag = tag;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        if (!(sender instanceof ProxiedPlayer)) return false;
        Player player = PalaceBungee.getPlayer(((ProxiedPlayer) sender).getUniqueId());
        if (player == null) return false;
        return player.getRank().getRankId() >= rank.getRankId() || (tag != null && player.getTags().contains(tag));
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (!(commandSender instanceof ProxiedPlayer)) return;
        Player player = PalaceBungee.getPlayer(((ProxiedPlayer) commandSender).getUniqueId());
        if (player == null) return;
        if (player.isDisabled() && !getName().equals("staff")) return;
        if (player.getRank().getRankId() >= rank.getRankId() || (tag != null && player.getTags().contains(tag))) {
            // either player meets the rank requirement, or the tag requirement
            execute(player, strings);
        } else {
            player.sendMessage(ChatColor.RED + "You do not have permission to perform this command!");
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] args) {
        if (!tabComplete || !(commandSender instanceof ProxiedPlayer)) return new ArrayList<>();
        Player player = PalaceBungee.getPlayer(((ProxiedPlayer) commandSender).getUniqueId());
        if (player == null) return new ArrayList<>();
        if (player.getRank().getRankId() >= rank.getRankId() || (tag != null && player.getTags().contains(tag))) {
            if (tabCompletePlayers) {
                List<String> list = new ArrayList<>(PalaceBungee.getServerUtil().getOnlinePlayerNames());
                if (args.length > 0) {
                    String arg2 = args[args.length - 1];
                    List<String> l2 = new ArrayList<>();
                    for (String s : list) {
                        if (s.toLowerCase().startsWith(arg2.toLowerCase())) {
                            l2.add(s);
                        }
                    }
                    Collections.sort(l2);
                    return l2;
                } else {
                    Collections.sort(list);
                    return list;
                }
            } else {
                return onTabComplete(player, Arrays.asList(args));
            }
        } else {
            return new ArrayList<>();
        }
    }

    public abstract void execute(Player player, String[] args);

    public Iterable<String> onTabComplete(Player player, List<String> args) {
        return new ArrayList<>();
    }
}
