package network.palace.bungee.commands.moderation;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.messages.packets.MutePlayerPacket;

import java.util.UUID;
import java.util.logging.Level;

public class UnmuteCommand extends PalaceCommand {

    public UnmuteCommand() {
        super("unmute", Rank.MOD);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/unmute [Username]");
            return;
        }
        String username = args[0];
        UUID uuid = PalaceBungee.getMongoHandler().usernameToUUID(username);
        if (uuid == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }
        try {
            PalaceBungee.getMongoHandler().unmutePlayer(uuid);
            PalaceBungee.getMessageHandler().sendMessage(new MutePlayerPacket(uuid), PalaceBungee.getMessageHandler().ALL_PROXIES);
            PalaceBungee.getModerationUtil().announceUnmute(username, player.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred while unmuting that player. Check console for errors.");
            PalaceBungee.getProxyServer().getLogger().log(Level.SEVERE, "Error processing mute", e);
        }
    }
}