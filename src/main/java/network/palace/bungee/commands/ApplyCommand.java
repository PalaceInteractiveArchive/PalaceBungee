package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;

public class ApplyCommand extends PalaceCommand {
    static BaseComponent[] message = new ComponentBuilder("\nClick to see what positions we have available!\n").color(ChatColor.YELLOW).bold(true)
            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("Click to open ").color(ChatColor.AQUA).append("https://palnet.us/apply").color(ChatColor.GREEN).create()))
            .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://palnet.us/apply")).create();

    public ApplyCommand() {
        super("apply");
    }

    @Override
    public void execute(Player player, String[] args) {
        player.sendMessage(message);
    }
}
