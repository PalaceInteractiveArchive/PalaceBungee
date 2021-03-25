package network.palace.bungee.commands;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.protocol.packet.Chat;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.mongo.MongoHandler;

import java.io.IOException;

public class DiscordCommand extends PalaceCommand {
    private static final BaseComponent[] message = new ComponentBuilder("\nClick for more information about Discord!\n").color(ChatColor.YELLOW).bold(true)
            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("Click to open ").color(ChatColor.AQUA).append("https://palnet.us/Discord").color(ChatColor.GREEN).create()))
            .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://palnet.us/Discord")).create();

    public DiscordCommand() {
        super("discord");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            boolean isLinked = PalaceBungee.getMongoHandler().verifyDiscordLink(player.getUniqueId());
            if (isLinked) {
                player.sendMessage(ChatColor.RED + "You have discovered a new feature. This does not yet work, eventually you will a message about your linked username.");
            } else {
                player.sendMessage(ChatColor.GREEN + "You have not yet linked your Discord account. Please run " + ChatColor.YELLOW + ChatColor.BOLD + "/discord link");
            }
        } else if (args[0].equals("link")) {

            player.sendMessage(ChatColor.AQUA + "Automatic account linking will return in a future update! In the meantime, post your username and rank in " +
                    ChatColor.YELLOW + "#link-account-requests " + ChatColor.AQUA + "on our discord server and a staff member will assist you.");
            StringBuilder fullName = new StringBuilder();
//            for (int i = 1; i < args.length; i++) {
//                fullName.append(args[i]);
//            }
//            if (fullName.toString().contains("#") && fullName.toString().matches("(.*)#(\\d+)")) {
//                DiscordUserInfo userInfo = new DiscordUserInfo(fullName.toString(), player.getUsername(), player.getUniqueId().toString(), player.getRank().toString());
//                if (SocketConnection.sendLink(userInfo)) {
//                    player.sendMessage(ChatColor.GREEN + "");
//                }
//            } else {
//                player.sendMessage(ChatColor.DARK_RED + "Please specify a valid Discord ID!");
//            }
        } else {
            player.sendMessage(message);
        }
    }
}