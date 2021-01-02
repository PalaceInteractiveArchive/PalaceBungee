package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.utils.DateUtil;
import org.bson.Document;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

public class PunishmentsCommand extends PalaceCommand {

    public PunishmentsCommand() {
        super("punishments");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/punishments [Summary/Bans/Mutes/Kicks/Warns]");
            return;
        }
        String section = args[0].toLowerCase();
        UUID uuid = player.getUniqueId();
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        switch (section) {
            case "summary": {
                int bans = PalaceBungee.getMongoHandler().getBans(uuid).size();
                int mutes = PalaceBungee.getMongoHandler().getMutes(uuid).size();
                int kicks = PalaceBungee.getMongoHandler().getKicks(uuid).size();
                int warns = PalaceBungee.getMongoHandler().getWarnings(uuid).size();
                player.sendMessage(ChatColor.GREEN + "Your Punishment History: " + ChatColor.YELLOW +
                        bans + " Bans, " + mutes + " Mutes, " + kicks + " Kicks, " + warns + " Warnings");
                if (bans == 0 && mutes == 0 && kicks == 0 && warns == 0) {
                    player.sendMessage(ChatColor.GREEN + "A clean record, nice work!");
                }
                break;
            }
            case "bans": {
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Your Ban History:");
                boolean empty = true;
                for (Object o : PalaceBungee.getMongoHandler().getBans(uuid)) {
                    empty = false;
                    Document doc = (Document) o;
                    String reason = doc.getString("reason");
                    long created = doc.getLong("created");
                    long expires = doc.getLong("expires");
                    boolean permanent = doc.getBoolean("permanent");
                    boolean active = doc.getBoolean("active");
                    Calendar createdCal = Calendar.getInstance();
                    createdCal.setTimeInMillis(created);
                    Calendar expiresCal = Calendar.getInstance();
                    expiresCal.setTimeInMillis(expires);
                    if (permanent) {
                        player.sendMessage(ChatColor.RED + "Reason: " + ChatColor.GREEN + reason.trim() +
                                ChatColor.RED + " | Started: " + ChatColor.GREEN + df.format(created) +
                                ChatColor.RED + " | Length: " + ChatColor.GREEN + "Permanent");
                    } else {
                        player.sendMessage(ChatColor.RED + "Reason: " + ChatColor.GREEN + reason.trim() +
                                ChatColor.RED + " | Started: " + ChatColor.GREEN + df.format(created) +
                                ChatColor.RED + (active ? " | Expired: " : " | Expires: ") +
                                ChatColor.GREEN + df.format(expires) + ChatColor.RED + " | Length: " +
                                ChatColor.GREEN + DateUtil.formatDateDiff(createdCal, expiresCal) +
                                ChatColor.RED + " | Permanent: " + ChatColor.GREEN + "False");
                    }
                }
                if (empty) {
                    player.sendMessage(ChatColor.GREEN + "No bans, good job! :)");
                }
                break;
            }
            case "mutes": {
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Your Mute History:");
                boolean empty = true;
                for (Object o : PalaceBungee.getMongoHandler().getMutes(uuid)) {
                    empty = false;
                    Document doc = (Document) o;
                    String reason = doc.getString("reason");
                    long created = doc.getLong("created");
                    long expires = doc.getLong("expires");
                    boolean active = doc.getBoolean("active");
                    Calendar createdCal = Calendar.getInstance();
                    createdCal.setTimeInMillis(created);
                    Calendar expiresCal = Calendar.getInstance();
                    expiresCal.setTimeInMillis(expires);
                    player.sendMessage(ChatColor.RED + "Reason: " + ChatColor.GREEN + reason.trim() +
                            ChatColor.RED + " | Started: " + ChatColor.GREEN + df.format(created) +
                            ChatColor.RED + (active ? " | Expired: " : " | Expires: ") +
                            ChatColor.GREEN + df.format(expires) + ChatColor.RED + " | Length: " +
                            ChatColor.GREEN + DateUtil.formatDateDiff(createdCal, expiresCal));
                }
                if (empty) {
                    player.sendMessage(ChatColor.GREEN + "No mutes, great job! :)");
                }
                break;
            }
            case "kicks": {
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Your Kick History:");
                boolean empty = true;
                for (Object o : PalaceBungee.getMongoHandler().getKicks(uuid)) {
                    empty = false;
                    Document doc = (Document) o;
                    String reason = doc.getString("reason");
                    long time = doc.getLong("time");
                    player.sendMessage(ChatColor.RED + "Reason: " + ChatColor.GREEN + reason.trim() +
                            ChatColor.RED + " | Time: " + ChatColor.GREEN + df.format(time));
                }
                if (empty) {
                    player.sendMessage(ChatColor.GREEN + "No kicks, nice job! :)");
                }
                break;
            }
            case "warns":
            case "warnings": {
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Your Warning History:");
                boolean empty = true;
                for (Object o : PalaceBungee.getMongoHandler().getWarnings(uuid)) {
                    empty = false;
                    Document doc = (Document) o;
                    String reason = doc.getString("reason");
                    long time = doc.getLong("time");
                    player.sendMessage(ChatColor.RED + "Reason: " + ChatColor.GREEN + reason.trim() +
                            ChatColor.RED + " | Time: " + ChatColor.GREEN + df.format(time));
                }
                if (empty) {
                    player.sendMessage(ChatColor.GREEN + "No warnings, great work! :)");
                }
                break;
            }
            default: {
                player.sendMessage(ChatColor.RED + "/punishments [Summary/Bans/Mutes/Kicks/Warns]");
                break;
            }
        }
    }
}