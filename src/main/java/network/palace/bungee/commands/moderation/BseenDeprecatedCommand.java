package network.palace.bungee.commands.moderation;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.RankTag;
import network.palace.bungee.handlers.moderation.Ban;
import network.palace.bungee.handlers.moderation.Mute;
import network.palace.bungee.utils.DateUtil;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BseenDeprecatedCommand extends PalaceCommand {

    public BseenDeprecatedCommand() {
        super("bseen", Rank.TRAINEE);
        tabComplete = true;
        tabCompletePlayers = true;
    }


    @Override
    public void execute(Player player, String[] args) {
        player.sendMessage(ChatColor.RED + "Hey, you! Following an announcement in Discord, " + ChatColor.BOLD + "/bseen" + ChatColor.RESET + ChatColor.RED + " is being deprecated on July 1st 2021. Please use /lookup instead.");

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/lookup [Username]");
            return;
        }
        Player tp = PalaceBungee.getPlayer(args[0]);
        boolean onProxy = tp != null && tp.getProxiedPlayer().isPresent();
        String name = onProxy ? tp.getUsername() : args[0];
        UUID uuid = onProxy ? tp.getUniqueId() : PalaceBungee.getMongoHandler().usernameToUUID(name);
        if (uuid == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }
        Rank rank;
        List<RankTag> tags;
        boolean online;
        long lastOnline;
        String ip;
        Mute mute;
        String server;
        if (onProxy) {
            rank = tp.getRank();
            tags = tp.getTags();
            lastOnline = tp.getLoginTime();
            ip = tp.getAddress();
            mute = tp.getMute();
            server = tp.getServerName();
            online = true;
        } else {
            Document doc = PalaceBungee.getMongoHandler().getPlayer(uuid, new Document("username", 1).append("rank", 1)
                    .append("tags", 1).append("lastOnline", 1).append("ip", 1).append("server", 1).append("onlineData", 1));
            name = doc.getString("username");
            rank = Rank.fromString(doc.getString("rank"));
            tags = new ArrayList<>();
            if (doc.containsKey("tags")) {
                var tagList = doc.get("tags", ArrayList.class);
                for (Object s : tagList) {
                    RankTag tag = RankTag.fromString((String) s);
                    if (tag != null) tags.add(tag);
                }
            }
            lastOnline = doc.getLong("lastOnline");
            ip = doc.getString("ip");
            mute = PalaceBungee.getMongoHandler().getCurrentMute(uuid);
            online = doc.containsKey("onlineData");
            if (online) {
                Document onlineData = doc.get("onlineData", Document.class);
                server = onlineData.getString("server");
            } else {
                server = doc.getString("server");
            }

            Ban ban = PalaceBungee.getMongoHandler().getCurrentBan(uuid, name);
            if (ban != null) {
                String type = ban.isPermanent() ? "Permanently" : ("Temporarily (Expires: " +
                        DateUtil.formatDateDiff(ban.getExpires()) + ")");
                player.sendMessage(ChatColor.RED + name + " is Banned " + type + " for " + ban.getReason() +
                        " by " + PalaceBungee.getMongoHandler().verifyModerationSource(ban.getSource()));
            }
        }

        if (server == null) server = "Unknown";

        if (mute != null && mute.isMuted()) {
            player.sendMessage(ChatColor.RED + name + " is Muted for " +
                    DateUtil.formatDateDiff(mute.getExpires()) + " by " + PalaceBungee.getMongoHandler().verifyModerationSource(mute.getSource()) +
                    ". Reason: " + mute.getReason());
        }
        player.sendMessage(ChatColor.GREEN + name + " has been " + (online ? "online" : "away") + " for " +
                DateUtil.formatDateDiff(lastOnline));
        player.sendMessage(ChatColor.RED + "Rank: " + rank.getFormattedName());
        for (RankTag tag : tags) {
            player.sendMessage(tag.getColor() + tag.getName());
        }

        String divider = " - ";
        player.sendMessage(new ComponentBuilder("Alt Accounts").color(ChatColor.AQUA)
                .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/altaccounts " + (player.getRank().getRankId() >= Rank.LEAD.getRankId() ? ip : name)))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Click to search for alt accounts").color(ChatColor.AQUA)
                                .append(player.getRank().getRankId() >= Rank.LEAD.getRankId() ? ("\nUser IP: " + ip) : "").color(ChatColor.GOLD)
                                .create())).append(divider).color(ChatColor.DARK_GREEN)
                .append("Name Check").color(ChatColor.LIGHT_PURPLE)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/namecheck " + name))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Click to run a name check").color(ChatColor.AQUA)
                                .create())).append(divider).color(ChatColor.DARK_GREEN)
                .append("Mod Log").color(ChatColor.GREEN)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Review moderation history").color(ChatColor.GREEN)
                                .create())).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/modlog " + name)).append("\n" + (online ? "Current" : "Last") +
                        " Server: ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.YELLOW)
                .append(server).color(ChatColor.AQUA).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Click to join this server!").color(ChatColor.GREEN)
                                .create())).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/server " + server)).create());

    }
}
