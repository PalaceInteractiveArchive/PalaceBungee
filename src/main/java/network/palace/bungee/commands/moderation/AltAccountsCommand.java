package network.palace.bungee.commands.moderation;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.*;
import org.bson.Document;

import java.util.List;

public class AltAccountsCommand extends PalaceCommand {
    private static final String IP_PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

    public AltAccountsCommand() {
        super("altaccounts", Rank.TRAINEE, "alts");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/altaccounts [Username" + (player.getRank().getRankId() >= Rank.LEAD.getRankId() ? "/IP Address" : "") + "]");
            return;
        }
        boolean usernameLookup;
        String ip;
        if (player.getRank().getRankId() >= Rank.LEAD.getRankId() && args[0].matches(IP_PATTERN)) {
            player.sendMessage(ChatColor.GREEN + "Searching for alt accounts on the IP Address " + args[0] + "...");
            ip = args[0];
            usernameLookup = false;
        } else {
            if (args[0].matches(IP_PATTERN)) {
                player.sendMessage(ChatColor.AQUA + "You aren't permitted to search for alt accounts by IP Address!");
                return;
            }
            player.sendMessage(ChatColor.GREEN + "Searching for alt accounts on " + args[0] + "'s IP Address...");
            try {
                ip = PalaceBungee.getMongoHandler().getPlayer(args[0], new Document("ip", 1)).getString("ip");
            } catch (Exception ignored) {
                player.sendMessage(ChatColor.RED + "Player not found!");
                return;
            }
            usernameLookup = true;
        }
        AddressBan ban = PalaceBungee.getMongoHandler().getAddressBan(ip);
        if (ban != null) {
            player.sendMessage(ChatColor.RED + "This IP Address is banned for " + ChatColor.AQUA + ban.getReason());
        }
        SpamIPWhitelist spamIPWhitelist = PalaceBungee.getMongoHandler().getSpamIPWhitelist(ip);
        if (spamIPWhitelist != null) {
            player.sendMessage(ChatColor.GREEN + "This IP Address is whitelisted from Spam IP protection with a player limit of " +
                    spamIPWhitelist.getLimit() + " players.");
        }
        List<String> users = PalaceBungee.getMongoHandler().getPlayersFromIP(ip);
        if (users == null || users.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No users found on that IP Address.");
            return;
        }
        if (users.size() > 30) {
            player.sendMessage(ChatColor.RED + "There are more than 30 players on that IP Address! If you need the list message Legobuilder0813 on Slack.");
            return;
        }
        ComponentBuilder ulist = new ComponentBuilder("");
        for (int i = 0; i < users.size(); i++) {
            String s = users.get(i);
            if (i == (users.size() - 1)) {
                ulist.append(s).color(ChatColor.GREEN).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bseen "
                        + s)).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Click to search this Player!").color(ChatColor.GREEN).create()));
                continue;
            }
            ulist.append(s).color(ChatColor.GREEN).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bseen "
                    + s)).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("Click to search this Player!").color(ChatColor.GREEN).create())).append(", ");
        }
        BaseComponent[] msg;
        if (usernameLookup) {
            msg = new ComponentBuilder("Users on the same IP Address as " + args[0] + ":").color(ChatColor.AQUA).create();
        } else {
            msg = new ComponentBuilder("Users on IP Address " + ip + ":").color(ChatColor.AQUA).create();
        }
        player.sendMessage(msg);
        player.sendMessage(ulist.create());
    }
}