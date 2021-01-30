package network.palace.bungee.commands.moderation;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.chat.ComponentSerializer;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.moderation.Kick;
import network.palace.bungee.messages.packets.KickPlayerPacket;

import java.util.UUID;

public class KickCommand extends PalaceCommand {

    public KickCommand() {
        super("kick", Rank.TRAINEE);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "/kick [Player] [Reason]");
            return;
        }
        String playername = args[0];
        UUID uuid = PalaceBungee.getMongoHandler().usernameToUUID(playername);
        if (uuid == null || !PalaceBungee.getMongoHandler().isPlayerOnline(uuid)) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }
        StringBuilder r = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            r.append(args[i]).append(" ");
        }
        String reason = (r.substring(0, 1).toUpperCase() + r.substring(1)).trim();
        Kick kick = new Kick(uuid, reason, player.getUniqueId().toString());
        try {
            PalaceBungee.getMessageHandler().sendMessage(new KickPlayerPacket(uuid,
                    ComponentSerializer.toString(PalaceBungee.getModerationUtil().getKickMessage(kick)),
                    true), PalaceBungee.getMessageHandler().ALL_PROXIES);
            PalaceBungee.getModerationUtil().announceKick(playername, reason, player.getUsername());
            PalaceBungee.getMongoHandler().kickPlayer(uuid, kick);
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "That player isn't online!");
        }
    }
}