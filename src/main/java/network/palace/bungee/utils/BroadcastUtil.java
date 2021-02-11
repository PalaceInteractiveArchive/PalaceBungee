package network.palace.bungee.utils;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.Player;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class BroadcastUtil {

    public BroadcastUtil() {
        //TODO need to find way to distribute this system in the future
        PalaceBungee.getProxyServer().getScheduler().schedule(PalaceBungee.getInstance(), new Runnable() {
            int i = 0;

            @Override
            public void run() {
                List<String> announcements = PalaceBungee.getConfigUtil().getAnnouncements();
                if (announcements.isEmpty()) return;
                if (i >= announcements.size()) i = 0;

                String message = ChatColor.WHITE + "[" + ChatColor.BLUE + "✦" + ChatColor.WHITE + "] " + announcements.get(i++);

                for (Player tp : PalaceBungee.getOnlinePlayers()) {
                    tp.sendMessage(message);
                }
            }
        }, 1, 5, TimeUnit.MINUTES);
    }
}
