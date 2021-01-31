package network.palace.bungee.handlers;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import network.palace.bungee.PalaceBungee;

public abstract class PalaceCommand extends Command {
    @Getter private final Rank rank;
    @Getter private final RankTag tag;

    public PalaceCommand(String name) {
        this(name, Rank.SETTLER);
    }

    public PalaceCommand(String name, String... aliases) {
        this(name, Rank.SETTLER, aliases);
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
        if (player.getRank().getRankId() >= rank.getRankId() || (tag != null && player.getTags().contains(tag))) {
            // either player meets the rank requirement, or the tag requirement
            execute(player, strings);
        } else {
            player.sendMessage(ChatColor.RED + "You do not have permission to perform this command!");
        }
    }

    public abstract void execute(Player player, String[] args);
}
