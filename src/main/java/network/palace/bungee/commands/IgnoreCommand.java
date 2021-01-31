package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;

import java.text.DateFormatSymbols;
import java.util.*;

public class IgnoreCommand extends PalaceCommand {

    public IgnoreCommand() {
        super("ignore");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            helpMenu(player);
            return;
        }
        switch (args[0].toLowerCase()) {
            case "list": {
                List<UUID> list = player.getIgnored();
                List<String> names = new ArrayList<>();
                for (UUID uuid : list) {
                    names.add(PalaceBungee.getUsername(uuid));
                }
                if (names.isEmpty()) {
                    player.sendMessage(ChatColor.GREEN + "No ignored players!");
                    return;
                }
                int page = 1;
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ignored) {
                    }
                }
                names.sort(Comparator.comparing(String::toLowerCase));
                int listSize = names.size();
                int maxPage = (int) Math.ceil((double) listSize / 8);
                if (page > maxPage) page = maxPage;
                int startAmount = 8 * (page - 1);
                int endAmount;
                if (maxPage > 1) {
                    if (page < maxPage) {
                        endAmount = (8 * page);
                    } else {
                        endAmount = listSize;
                    }
                } else {
                    endAmount = listSize;
                }
                names = names.subList(startAmount, endAmount);
                StringBuilder msg = new StringBuilder(ChatColor.YELLOW + "Ignored Players (Page " + page + " of " + maxPage + "):\n");
                for (String name : names) {
                    msg.append(ChatColor.AQUA).append("- ").append(ChatColor.YELLOW).append(name).append("\n");
                }
                player.sendMessage(msg.toString());
                break;
            }
            case "add": {
                if (args.length < 2) {
                    helpMenu(player);
                    return;
                }
                if (args[1].equalsIgnoreCase(player.getUsername())) {
                    player.sendMessage(ChatColor.RED + "You can't ignore yourself!");
                    return;
                }
                String name;
                UUID uuid = PalaceBungee.getMongoHandler().usernameToUUID(args[1]);
                if (uuid == null) {
                    player.sendMessage(ChatColor.RED + "That player can't be found!");
                    return;
                }
                Rank rank = PalaceBungee.getMongoHandler().getRank(uuid);
                if (rank.getRankId() >= Rank.CHARACTER.getRankId()) {
                    player.sendMessage(ChatColor.RED + "You can't ignore that player!");
                    return;
                }
                name = PalaceBungee.getUsername(uuid);
                player.setIgnored(uuid, true);
                PalaceBungee.getMongoHandler().ignorePlayer(player, uuid);
                player.sendMessage(ChatColor.GREEN + "You have ignored " + name);
                // TODO Handle creative chat
//                if (PalaceBungee.getServer(player.getServer()).getServerType().equals("Creative"))
//                    player.sendServerIgnoreList();
                break;
            }
            case "remove": {
                if (args.length < 2) {
                    helpMenu(player);
                    return;
                }
                String name;
                UUID uuid = PalaceBungee.getMongoHandler().usernameToUUID(args[1]);
                if (uuid == null) {
                    player.sendMessage(ChatColor.RED + "That player can't be found!");
                    return;
                }
                name = PalaceBungee.getUsername(uuid);
                player.setIgnored(uuid, false);
                PalaceBungee.getMongoHandler().unignorePlayer(player, uuid);
                player.sendMessage(ChatColor.GREEN + "You have unignored " + name);
//                PalaceBungee.getSchedulerManager().runAsync(() -> {
//                    if (PalaceBungee.getServer(player.getServer()).getServerType().equals("Creative"))
//                        player.sendServerIgnoreList();
//                });
                break;
            }
            default: {
                helpMenu(player);
                break;
            }
        }
    }

    private String format(long started) {
        Calendar c = new GregorianCalendar();
        c.setTime(new Date(started));
        c.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        int hour = c.get(Calendar.HOUR_OF_DAY);
        String am = "am";
        if (hour > 12) {
            am = "pm";
            hour -= 12;
        } else if (hour == 0) {
            hour += 12;
        }
        String month = new DateFormatSymbols().getMonths()[c.get(Calendar.MONTH)].substring(0, 3);
        String min = String.valueOf(c.get(Calendar.MINUTE));
        if (min.length() < 2) {
            min = "0" + min;
        }
        return month + " " + c.get(Calendar.DAY_OF_MONTH) + " " +
                c.get(Calendar.YEAR) + " at " + hour + ":" + min + am;
    }

    public void helpMenu(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Use /ignore to hide messages from players\n" +
                ChatColor.GREEN + "Ignore Commands:\n" + ChatColor.YELLOW + "/ignore list [page] " +
                ChatColor.AQUA + "- List ignored players\n" + ChatColor.YELLOW + "/ignore add [player] " +
                ChatColor.AQUA + "- Ignore a player\n" + ChatColor.YELLOW + "/ignore remove [player] " +
                ChatColor.AQUA + "- Unignore a player\n" + ChatColor.YELLOW + "/ignore help " +
                ChatColor.AQUA + "- Show this help menu");
    }
}