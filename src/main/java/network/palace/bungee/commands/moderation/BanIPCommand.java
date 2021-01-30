package network.palace.bungee.commands.moderation;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.chat.ComponentSerializer;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.moderation.AddressBan;
import network.palace.bungee.messages.packets.KickIPPacket;

import java.util.logging.Level;

public class BanIPCommand extends PalaceCommand {

    public BanIPCommand() {
        super("banip", Rank.LEAD);
    }

    @Override
    public void execute(Player banner, String[] args) {
        if (args.length < 2) {
            banner.sendMessage(ChatColor.RED + "/banip [IP Address] [Reason]");
            return;
        }
        String ip = args[0];
        StringBuilder r = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            r.append(args[i]).append(" ");
        }
        String reason = r.substring(0, 1).toUpperCase() + r.substring(1);
        String finalReason = reason.trim();
        PalaceBungee.getProxyServer().getScheduler().runAsync(PalaceBungee.getInstance(), () -> {
            try {
                AddressBan existing = PalaceBungee.getMongoHandler().getAddressBan(ip);
                if (existing != null) {
                    banner.sendMessage(ChatColor.RED + "This IP " + (!ip.contains("*") ? "Address " : "Range ") +
                            "is already banned! Unban it to change the reason.");
                    return;
                }
                AddressBan ban = new AddressBan(ip, finalReason, banner.getUniqueId().toString());
                PalaceBungee.getMongoHandler().banAddress(ban);
                PalaceBungee.getMessageHandler().sendMessage(new KickIPPacket(ip,
                        ComponentSerializer.toString(PalaceBungee.getModerationUtil().getBanMessage(ban)),
                        true), PalaceBungee.getMessageHandler().ALL_PROXIES);
                PalaceBungee.getModerationUtil().announceBan(ban);
            } catch (Exception e) {
                e.printStackTrace();
                banner.sendMessage(ChatColor.RED + "An error occurred while banning that player. Check console for errors.");
                PalaceBungee.getProxyServer().getLogger().log(Level.SEVERE, "Error processing ban", e);
            }
        });
    }
}