package network.palace.bungee.commands.moderation;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.utils.DateUtil;
import network.palace.bungee.utils.ModerationUtil;
import org.bson.Document;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

public class ModlogCommand extends PalaceCommand {

    public ModlogCommand() {
        super("modlog", Rank.TRAINEE);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/modlog [Username] [Bans/Mutes/Kicks/Warns]");
            return;
        }
        String username = args[0];
        Player tp = PalaceBungee.getPlayer(username);
        UUID uuid;
        if (tp == null) {
            uuid = PalaceBungee.getMongoHandler().usernameToUUID(username);
            if (uuid == null) {
                player.sendMessage(ChatColor.RED + "Player not found!");
                return;
            }
        } else {
            uuid = tp.getUniqueId();
            username = tp.getUsername();
        }
        if (args.length == 1) {
            int bans = PalaceBungee.getMongoHandler().getBans(uuid).size();
            int mutes = PalaceBungee.getMongoHandler().getMutes(uuid).size();
            int kicks = PalaceBungee.getMongoHandler().getKicks(uuid).size();
            int warns = PalaceBungee.getMongoHandler().getWarnings(uuid).size();
            player.sendMessage(ChatColor.GREEN + "Moderation Log for " + username + ": " + ChatColor.YELLOW +
                    bans + " Bans, " + mutes + " Mutes, " + kicks + " Kicks, " + warns + " Warnings");
        } else {
            String type = args[1].toLowerCase();
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            switch (type) {
                case "bans": {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Ban Log for " + username + ":");
                    for (Object o : PalaceBungee.getMongoHandler().getBans(uuid)) {
                        Document doc = (Document) o;
                        String reason = doc.getString("reason");
                        long created = doc.getLong("created");
                        long expires = doc.getLong("expires");
                        boolean permanent = doc.getBoolean("permanent");
                        boolean active = doc.getBoolean("active");
                        String source = ModerationUtil.verifySource(doc.getString("source"));
                        Calendar createdCal = Calendar.getInstance();
                        createdCal.setTimeInMillis(created);
                        Calendar expiresCal = Calendar.getInstance();
                        expiresCal.setTimeInMillis(expires);
                        if (permanent) {
                            player.sendMessage(ChatColor.RED + "Reason: " + ChatColor.GREEN + reason.trim() +
                                    ChatColor.RED + " | Source: " + ChatColor.GREEN + source + ChatColor.RED + " | Started: " +
                                    ChatColor.GREEN + df.format(created) + ChatColor.RED + " | Length: " +
                                    ChatColor.GREEN + "Permanent" + ChatColor.RED + " | Active: " +
                                    ChatColor.GREEN + (active ? "True" : "False"));
                        } else {
                            player.sendMessage(ChatColor.RED + "Reason: " + ChatColor.GREEN + reason.trim() +
                                    ChatColor.RED + " | Source: " + ChatColor.GREEN + source + ChatColor.RED + " | Started: " +
                                    ChatColor.GREEN + df.format(created) + ChatColor.RED + (active ? " | Expires: " : " | Expired: ") +
                                    ChatColor.GREEN + df.format(expires) + ChatColor.RED + " | Length: " +
                                    ChatColor.GREEN + DateUtil.formatDateDiff(createdCal, expiresCal) + ChatColor.RED + " | Permanent: " +
                                    ChatColor.GREEN + "False" + ChatColor.RED + " | Active: " +
                                    ChatColor.GREEN + (active ? "True" : "False"));
                        }
                    }
                    break;
                }
                case "mutes": {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Mute Log for " + username + ":");
                    for (Object o : PalaceBungee.getMongoHandler().getMutes(uuid)) {
                        Document doc = (Document) o;
                        String reason = doc.getString("reason");
                        long created = doc.getLong("created");
                        long expires = doc.getLong("expires");
                        boolean active = doc.getBoolean("active");
                        String source = ModerationUtil.verifySource(doc.getString("source"));
                        Calendar createdCal = Calendar.getInstance();
                        createdCal.setTimeInMillis(created);
                        Calendar expiresCal = Calendar.getInstance();
                        expiresCal.setTimeInMillis(expires);
                        player.sendMessage(ChatColor.RED + "Reason: " + ChatColor.GREEN + reason.trim() +
                                ChatColor.RED + " | Source: " + ChatColor.GREEN + source + ChatColor.RED + " | Started: " +
                                ChatColor.GREEN + df.format(created) + ChatColor.RED + (active ? " | Expires: " : " | Expired: ") +
                                ChatColor.GREEN + df.format(expires) + ChatColor.RED + " | Length: " +
                                ChatColor.GREEN + DateUtil.formatDateDiff(createdCal, expiresCal) + ChatColor.RED + " | Active: " +
                                ChatColor.GREEN + (active ? "True" : "False"));
                    }
                    break;
                }
                case "kicks": {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Kick Log for " + username + ":");
                    for (Object o : PalaceBungee.getMongoHandler().getKicks(uuid)) {
                        Document doc = (Document) o;
                        String reason = doc.getString("reason");
                        long time = doc.getLong("time");
                        String source = ModerationUtil.verifySource(doc.getString("source"));
                        player.sendMessage(ChatColor.RED + "Reason: " + ChatColor.GREEN + reason.trim() +
                                ChatColor.RED + " | Source: " + ChatColor.GREEN + source + ChatColor.RED + " | Time: " +
                                ChatColor.GREEN + df.format(time));
                    }
                    break;
                }
                case "warns":
                case "warnings": {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Warning Log for " + username + ":");
                    for (Object o : PalaceBungee.getMongoHandler().getWarnings(uuid)) {
                        Document doc = (Document) o;
                        String reason = doc.getString("reason");
                        long time = doc.getLong("time");
                        String source = ModerationUtil.verifySource(doc.getString("source"));
                        player.sendMessage(ChatColor.RED + "Reason: " + ChatColor.GREEN + reason.trim() +
                                ChatColor.RED + " | Source: " + ChatColor.GREEN + source + ChatColor.RED + " | Time: " +
                                ChatColor.GREEN + df.format(time));
                    }
                    break;
                }
                default: {
                    player.sendMessage(ChatColor.RED + "/modlog [Username] [Bans/Mutes/Kicks/Warns]");
                    break;
                }
            }
        }
    }
}