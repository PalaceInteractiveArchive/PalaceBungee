package network.palace.bungee.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.Player;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class BroadcastUtil {

    public BroadcastUtil() {
        PalaceBungee.getProxyServer().getScheduler().schedule(PalaceBungee.getInstance(), new Runnable() {
            int i = 0;

            @Override
            public void run() {
                List<String> announcements = PalaceBungee.getConfigUtil().getAnnouncements();
                if (announcements.isEmpty()) return;
                if (i >= announcements.size()) i = 0;

                BaseComponent[] components = new ComponentBuilder("[").color(ChatColor.WHITE)
                        .append("✦").color(ChatColor.BLUE).append("] ").color(ChatColor.WHITE)
                        .appendLegacy(announcements.get(i++))
                        .create();

                for (Player tp : PalaceBungee.getOnlinePlayers()) {
                    tp.sendMessage(components);
                }
            }
        }, 1, 5, TimeUnit.MINUTES);
    }
}
