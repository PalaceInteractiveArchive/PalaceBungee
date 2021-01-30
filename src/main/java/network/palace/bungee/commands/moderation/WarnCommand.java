package network.palace.bungee.commands.moderation;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.chat.ComponentSerializer;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.moderation.Warning;
import network.palace.bungee.messages.packets.ComponentMessagePacket;

import java.util.UUID;
import java.util.logging.Level;

public class WarnCommand extends PalaceCommand {

    public WarnCommand() {
        super("warn", Rank.TRAINEE);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "/warn [Player] [Reason]");
            return;
        }
        String playername = args[0];
        UUID uuid = PalaceBungee.getMongoHandler().usernameToUUID(playername);
        if (uuid == null || !PalaceBungee.getMongoHandler().isPlayerOnline(uuid)) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }
        if (System.currentTimeMillis() - PalaceBungee.getMongoHandler().getWarningCooldown(uuid) < 5000) {
            //players can't be warned until at least 4 seconds after their previous warn
            player.sendMessage(ChatColor.RED + "That player was warned recently, wait at least 5 seconds before warning again.");
            return;
        }
        StringBuilder r = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            r.append(args[i]).append(" ");
        }
        String reason = (r.substring(0, 1).toUpperCase() + r.substring(1)).trim();
        Warning warn = new Warning(uuid, reason, player.getUniqueId().toString());
        try {
            PalaceBungee.getMessageHandler().sendMessage(
                    new ComponentMessagePacket(ComponentSerializer.toString(PalaceBungee.getModerationUtil().getWarnMessage(warn)), uuid),
                    PalaceBungee.getMessageHandler().ALL_PROXIES
            );
            PalaceBungee.getModerationUtil().announceWarning(playername, reason, player.getUsername());
            PalaceBungee.getMongoHandler().warnPlayer(warn);
            PalaceBungee.getMongoHandler().setWarningCooldown(uuid);
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred while warning that player. Check console for errors.");
            PalaceBungee.getProxyServer().getLogger().log(Level.SEVERE, "Error processing warn", e);
        }
    }
}