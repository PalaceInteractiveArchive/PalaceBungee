package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.utils.DateUtil;

public class UptimeCommand extends PalaceCommand {

    public UptimeCommand() {
        super("uptime");
    }

    @Override
    public void execute(Player player, String[] args) {
        player.sendMessage(new ComponentBuilder("\nThis BungeeCord proxy has been online for " +
                DateUtil.formatDateDiff(PalaceBungee.getStartTime())).color(ChatColor.GREEN).create());
    }
}