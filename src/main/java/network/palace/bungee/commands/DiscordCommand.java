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
import java.util.Base64;
import java.util.Locale;

public class DiscordCommand extends PalaceCommand {
    public DiscordCommand() {
        super("discord");
    }

    private static final BaseComponent[] message = new ComponentBuilder("\nClick for more information about Discord!\n").color(ChatColor.YELLOW).bold(true)
            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("Click to open ").color(ChatColor.AQUA).append("https://palnet.us/Discord").color(ChatColor.GREEN).create()))
            .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://palnet.us/Discord")).create();

    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(message);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "link": {
                boolean isLinked = PalaceBungee.getMongoHandler().verifyDiscordLink(player.getUniqueId());

                BaseComponent[] linkMessage = new ComponentBuilder("\nClick to start linking your Discord account.\n").color(ChatColor.YELLOW).bold(true)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Opens a browser window to start the discord linking process.").color(ChatColor.GREEN).create()))
                .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.com/api/oauth2/authorize?client_id=543141358496383048&redirect_uri=https%3A%2F%2Finternal-api.palace.network%2Fdiscord%2Flink&response_type=code&scope=identify&state="
                + Base64.getEncoder().encodeToString(player.getUniqueId().toString().getBytes()) + "")).create();

                if (isLinked) {
                    player.sendMessage(ChatColor.GREEN + "Hey " + ChatColor.YELLOW + ChatColor.BOLD + player.getUsername() + ChatColor.GREEN + " you currently have a discord account linked. To unlink, run " + ChatColor.YELLOW + ChatColor.BOLD + "/discord unlink");
                } else {
                    player.sendMessage(linkMessage);
                }
                return;
            }
            case "unlink": {
                PalaceBungee.getMongoHandler().removeDiscordLink(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "You have successfully unlinked your discord account. Please run " + ChatColor.YELLOW + ChatColor.BOLD +  "/discord link" + ChatColor.GREEN + " to restart the linking process");
            }
        }
    }
}
