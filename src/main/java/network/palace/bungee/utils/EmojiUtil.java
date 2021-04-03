package network.palace.bungee.utils;

import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;

import java.util.*;

public class EmojiUtil {
    private static final HashMap<String, String> translations = new HashMap<>();
    public static final Rank emojiRank = Rank.DVC;

    static {
        translations.put(":heart:", "❤");
        translations.put(":checkmark:", "✔");
        translations.put(":music:", "♫");
        translations.put(":snowman:", "☃");
        translations.put(":shrug:", "¯\\_(ツ)_/¯");
        translations.put(":tableflip:", "(╯°□°）╯︵ ┻━┻");
        translations.put(":sqrt:", "√");
        translations.put("<3", "❤");
    }

    public static String convertMessage(Player player, String msg) throws IllegalArgumentException {
        if (player.getRank().getRankId() < emojiRank.getRankId()) return msg;
        int count = 0;
        for (Map.Entry<String, String> entry : translations.entrySet()) {
            if (msg.equalsIgnoreCase(entry.getKey())) {
                msg = entry.getValue();
                count++;
                continue;
            }
            if (msg.startsWith(entry.getKey() + " ")) {
                msg = msg.replaceFirst(entry.getKey() + " ", entry.getValue() + " ");
                count++;
            }
            if (msg.endsWith(" " + entry.getKey())) {
                int start = msg.lastIndexOf(entry.getKey());
                msg = msg.substring(0, start) + entry.getValue();
                count++;
            }
            String from = " " + entry.getKey() + " ";
            String to = " " + entry.getValue() + " ";
            while (!msg.replaceFirst(from, to).equals(msg)) {
                msg = msg.replaceFirst(from, to);
                count++;
            }
        }
        if (count > 5) {
            throw new IllegalArgumentException("Your message can't contain more than five emoji!");
        }
        return msg;
    }

    public static void tabComplete(Player player, int transactionId, String cmd, List<String> args, List<String> results) {
        if (player.getRank().getRankId() < emojiRank.getRankId()) return;
        Iterable<String> l;
        String arg2 = args.isEmpty() ? cmd : args.get(args.size() - 1);
        List<String> l2 = new ArrayList<>();
        for (String s : translations.keySet()) {
            if (!s.startsWith(":")) continue;
            if (s.toLowerCase().startsWith(arg2.toLowerCase())) {
                l2.add(s);
            }
        }
        Collections.sort(l2);
        l = l2;

        List<String> list = new ArrayList<>();
        for (String s : l) {
            list.add(s);
        }
        if (!list.isEmpty()) {
            results.clear();
            results.addAll(list);
        }
//        PacketTabComplete packet = new PacketTabComplete(player.getUniqueId(), transactionId, cmd, args, results);
//        player.send(packet);
    }
}