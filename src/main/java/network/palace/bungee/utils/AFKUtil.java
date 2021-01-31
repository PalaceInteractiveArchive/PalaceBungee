package network.palace.bungee.utils;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class AFKUtil {

    public AFKUtil() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                for (Player tp : PalaceBungee.getOnlinePlayers()) {
                    if (tp.getRank().getRankId() < Rank.TRAINEE.getRankId() || tp.getRank().getRankId() >= Rank.DEVELOPER.getRankId()) {
                        continue;
                    }
                    if (System.currentTimeMillis() - tp.getAfkTime() >= 30 * 60 * 1000) {
                        if (tp.isAFK()) continue;
                        try {
                            warn(tp);
                        } catch (IOException e) {
                            PalaceBungee.getProxyServer().getLogger().log(Level.SEVERE, "Error processing AFK warn", e);
                        }
                    }
                }
            }
        }, 5000, 5000);
    }

    public void warn(final Player player) throws IOException {
        UUID uuid = player.getUniqueId();
        String afk = ChatColor.RED + "" + ChatColor.BOLD + "                      AFK Timer:";
        String blank = "";
        String msg = ChatColor.YELLOW + "" + ChatColor.BOLD + "Type anything in chat (it won't be seen by others)";
        List<String> msgs = Arrays.asList(blank, blank, afk, blank, msg, blank, blank, blank, blank, blank);
        Timer t1 = new Timer();
        Timer t2 = new Timer();
        player.setAFK(true);
        player.getAfkTimers().addAll(Arrays.asList(t1, t2));
        t2.schedule(new TimerTask() {
            @Override
            public void run() {
                t1.cancel();
                if (player.isAFK()) {
                    player.getAfkTimers().forEach(Timer::cancel);
                    player.kickPlayer(ChatColor.RED + "You have been AFK for 30 minutes. Please try not to be AFK while on our servers.");
                    PalaceBungee.getMongoHandler().logAFK(player.getUniqueId());
                } else {
                    cancel();
                }
            }
        }, 300000);
        t1.schedule(new TimerTask() {
            int i = 0;

            @Override
            public void run() {
                try {
                    if (player.isAFK()) {
                        player.getProxiedPlayer().sendTitle(
                                BungeeCord.getInstance().createTitle()
                                        .title(new ComponentBuilder("Are you AFK?").color(ChatColor.RED).bold(true).create())
                                        .subTitle(new ComponentBuilder("AFK kick in ").color(ChatColor.RED)
                                                .append((5 - i) + " ").color(ChatColor.DARK_RED)
                                                .append("minutes!").color(ChatColor.RED).create())
                                        .fadeIn(10)
                                        .stay(1200).fadeOut(20)
                        );
                        i++;
                        for (String m : msgs) {
                            player.sendMessage(m);
                        }
                    } else {
                        cancel();
                    }
                } catch (Exception ignored) {
                }
            }
        }, 0, 60000);
    }
}