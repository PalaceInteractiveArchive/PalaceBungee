package network.palace.bungee.commands.moderation;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.chat.ComponentSerializer;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.moderation.Ban;
import network.palace.bungee.messages.packets.KickPlayerPacket;

import java.util.UUID;
import java.util.logging.Level;

public class BanCommand extends PalaceCommand {

    public BanCommand() {
        super("ban", Rank.CM);
        tabComplete = true;
        tabCompletePlayers = true;
    }

    @Override
    public void execute(Player banner, String[] args) {
        if (banner.getRank().equals(Rank.MEDIA) || banner.getRank().equals(Rank.IMAGINEER)) {
            banner.sendMessage(ChatColor.RED + "You do not have permission to execute this command!");
            return;
        }
        if (args.length < 2) {
            banner.sendMessage(ChatColor.RED + "/ban [Player] [Reason]");
            return;
        }
        String playername = args[0];
        UUID uuid = PalaceBungee.getMongoHandler().usernameToUUID(playername);
        if (uuid == null) {
            banner.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }
        StringBuilder r = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            r.append(args[i]).append(" ");
        }
        String reason = (r.substring(0, 1).toUpperCase() + r.substring(1)).trim();
        PalaceBungee.getProxyServer().getScheduler().runAsync(PalaceBungee.getInstance(), () -> {
            try {
                if (PalaceBungee.getMongoHandler().isPlayerBanned(uuid)) {
                    banner.sendMessage(ChatColor.RED + "This player is already banned! Unban them to change the reason.");
                    return;
                }
                Ban ban = new Ban(uuid, playername, true, System.currentTimeMillis(), System.currentTimeMillis(), reason, banner.getUniqueId().toString());
                PalaceBungee.getMongoHandler().banPlayer(uuid, ban);
                PalaceBungee.getMessageHandler().sendMessage(new KickPlayerPacket(uuid,
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