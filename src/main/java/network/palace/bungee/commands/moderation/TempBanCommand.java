package network.palace.bungee.commands.moderation;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.chat.ComponentSerializer;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.moderation.Ban;
import network.palace.bungee.messages.packets.KickPlayerPacket;
import network.palace.bungee.utils.DateUtil;

import java.util.UUID;
import java.util.logging.Level;

public class TempBanCommand extends PalaceCommand {

    public TempBanCommand() {
        super("tempban", Rank.MOD);
        tabComplete = true;
        tabCompletePlayers = true;
    }

    @Override
    public void execute(Player banner, String[] args) {
        if (args.length < 3) {
            banner.sendMessage(ChatColor.RED + "/tempban [Player] [Time] [Reason]");
            banner.sendMessage(ChatColor.RED + "Time Examples:");
            banner.sendMessage(ChatColor.RED + "6h = Six Hours");
            banner.sendMessage(ChatColor.RED + "6d = Six Days");
            banner.sendMessage(ChatColor.RED + "6w = Six Weeks");
            banner.sendMessage(ChatColor.RED + "6mon = Six Months");
            return;
        }
        String playername = args[0];
        UUID uuid = PalaceBungee.getMongoHandler().usernameToUUID(playername);
        if (uuid == null) {
            banner.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }
        StringBuilder r = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            r.append(args[i]).append(" ");
        }
        String reason = (r.substring(0, 1).toUpperCase() + r.substring(1)).trim();
        PalaceBungee.getProxyServer().getScheduler().runAsync(PalaceBungee.getInstance(), () -> {
            try {
                long timestamp = DateUtil.parseDateDiff(args[1], true);
                if (PalaceBungee.getMongoHandler().isPlayerBanned(uuid)) {
                    banner.sendMessage(ChatColor.RED + "This player is already banned! Unban them to change the reason.");
                    return;
                }
                Ban ban = new Ban(uuid, playername, false, timestamp, reason, banner.getUniqueId().toString());
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