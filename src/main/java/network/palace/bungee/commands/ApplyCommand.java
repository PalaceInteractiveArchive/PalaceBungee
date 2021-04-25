package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;

import java.util.Random;

public class ApplyCommand extends PalaceCommand {
    private final Random random = new Random();

    public ApplyCommand() {
        super("apply");
    }

    @Override
    public void execute(Player player, String[] args) {
        String token = getRandomToken();
        PalaceBungee.getMongoHandler().setTitanLogin(player.getUniqueId(), token);
        player.sendMessage(new ComponentBuilder("\nWe now take applications through our new portal\nClick to see what positions we have available!\n").color(ChatColor.YELLOW).bold(true)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Click to open ").color(ChatColor.AQUA).append("https://titan.palace.network/apply/login/" + token + "/" + player.getUniqueId().toString()).color(ChatColor.GREEN).create()))
                .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://titan.palace.network/apply/login/" + token + "/" + player.getUniqueId().toString())).create());
    }

    public String getRandomToken() {
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }
}
