package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;

public class SocialCommand extends PalaceCommand {
    static BaseComponent[] message = new ComponentBuilder("Forums: https://forums.palace.network\n").color(ChatColor.GREEN).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open ").color(ChatColor.AQUA).append("https://forums.palace.network").color(ChatColor.GREEN).create())).event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://forums.palace.network"))
            .append("Discord: https://palnet.us/Discord\n").color(ChatColor.LIGHT_PURPLE).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open ").color(ChatColor.AQUA).append("https://palnet.us/Discord").color(ChatColor.GREEN).create())).event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://palnet.us/Discord"))
            .append("Twitter: @PalaceNetwork\n").color(ChatColor.AQUA).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open ").color(ChatColor.AQUA).append("https://twitter.com/PalaceNetwork").color(ChatColor.GREEN).create())).event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://twitter.com/PalaceNetwork"))
            .append("Instagram: https://instagram.com/palacenetwork\n").color(ChatColor.GOLD).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open ").color(ChatColor.AQUA).append("https://instagram.com/palacenetwork").color(ChatColor.GREEN).create())).event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://instagram.com/palacenetwork"))
            .append("YouTube: https://youtube.com/MCMagicParks\n").color(ChatColor.RED).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open ").color(ChatColor.AQUA).append("https://youtube.com/MCMagicParks").color(ChatColor.GREEN).create())).event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://youtube.com/MCMagicParks"))
            .append("Facebook: https://facebook.com/PalaceNetworkMC\n").color(ChatColor.BLUE).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open ").color(ChatColor.AQUA).append("https://facebook.com/PalaceNetworkMC").color(ChatColor.GREEN).create())).event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://facebook.com/PalaceNetworkMC"))
            .create();

    public SocialCommand() {
        super("social");
    }

    @Override
    public void execute(Player player, String[] args) {
        player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "\nPalace Network Social Links:");
        player.sendMessage(message);
    }
}