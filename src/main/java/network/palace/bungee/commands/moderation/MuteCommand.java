package network.palace.bungee.commands.moderation;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.moderation.Mute;
import network.palace.bungee.messages.packets.MutePlayerPacket;
import network.palace.bungee.utils.DateUtil;

import java.util.UUID;
import java.util.logging.Level;

public class MuteCommand extends PalaceCommand {

    public MuteCommand() {
        super("mute", Rank.TRAINEE);
        tabComplete = true;
        tabCompletePlayers = true;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "/mute [Player] [Time] [Reason]");
            player.sendMessage(ChatColor.RED + "Time Examples:");
            player.sendMessage(ChatColor.RED + "5m = Five Minutes");
            player.sendMessage(ChatColor.RED + "1h = One Hour");
            return;
        }
        String username = args[0];
        PalaceBungee.getProxyServer().getScheduler().runAsync(PalaceBungee.getInstance(), () -> {
            try {
                long muteTimestamp = DateUtil.parseDateDiff(args[1], true);
                long length = muteTimestamp - System.currentTimeMillis();
                if ((player.getRank().equals(Rank.MEDIA) || player.getRank().equals(Rank.TECHNICIAN) || player.getRank().equals(Rank.TRAINEETECH))) {
                    if (length > 1800000) {
                        player.sendMessage(ChatColor.RED + "You can't mute for longer than 30 minutes!");
                        return;
                    }
                } else if (length > 3600000) {
                    player.sendMessage(ChatColor.RED + "You can't mute for longer than 1 hour!");
                    return;
                }
                String reason;
                StringBuilder r = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    r.append(args[i]).append(" ");
                }
                reason = (r.substring(0, 1).toUpperCase() + r.substring(1)).trim();
                String source = player.getUniqueId().toString();
                UUID uuid = PalaceBungee.getMongoHandler().usernameToUUID(username);
                if (uuid == null) {
                    player.sendMessage(ChatColor.RED + "Player not found!");
                    return;
                }
                if (PalaceBungee.getMongoHandler().isPlayerMuted(uuid)) {
                    player.sendMessage(ChatColor.RED + "This player is already muted! Unmute them to change the reason/duration.");
                    return;
                }
                Mute mute = new Mute(uuid, true, System.currentTimeMillis(), muteTimestamp, reason, source);
                PalaceBungee.getMongoHandler().mutePlayer(uuid, mute);
                PalaceBungee.getMessageHandler().sendMessage(new MutePlayerPacket(uuid), PalaceBungee.getMessageHandler().ALL_PROXIES);
                PalaceBungee.getModerationUtil().announceMute(mute, username);
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "An error occurred while muting that player. Check console for errors.");
                PalaceBungee.getProxyServer().getLogger().log(Level.SEVERE, "Error processing mute", e);
            }
        });
    }
}