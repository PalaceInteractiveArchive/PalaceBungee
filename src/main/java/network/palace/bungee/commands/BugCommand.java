package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;

public class BugCommand extends PalaceCommand {

    public BugCommand() {
        super("bug");
    }

    @Override
    public void execute(Player player, String[] args) {
        player.sendMessage(new ComponentBuilder("\nClick to report a bug!\n")
                .color(ChatColor.YELLOW).underlined(false).bold(true)
                .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://palnet.us/bugreport"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to visit https://palnet.us/apply").color(ChatColor.GREEN).create())).create());
    }
}
