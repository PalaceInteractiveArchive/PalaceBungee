package network.palace.bungee.commands.chat;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.messages.packets.ClearChatPacket;

import java.util.UUID;

public class ClearChatCommand extends PalaceCommand {

    public ClearChatCommand() {
        super("cc", Rank.TRAINEE);
    }

    @Override
    public void execute(Player player, String[] args) {
        try {
            if (args.length > 0) {
                UUID uuid = PalaceBungee.getUUID(args[0]);
                if (uuid == null || !PalaceBungee.getMongoHandler().isPlayerOnline(uuid)) {
                    player.sendMessage(ChatColor.RED + "Player not found!");
                    return;
                }
                String channel = PalaceBungee.getServerUtil().isOnPark(player) ? "ParkChat" : player.getServerName();
                ClearChatPacket packet = new ClearChatPacket(channel, player.getUsername(), uuid);
                PalaceBungee.getMessageHandler().sendMessage(packet, PalaceBungee.getMessageHandler().ALL_PROXIES);
                return;
            }
            String channel = PalaceBungee.getServerUtil().isOnPark(player) ? "ParkChat" : player.getServerName();
            PalaceBungee.getMessageHandler().sendMessage(new ClearChatPacket(channel, player.getUsername()), PalaceBungee.getMessageHandler().ALL_PROXIES);
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "There was an error running the chat clear command! If this continues to happen, report it immediately on Discord.");
        }
    }
}
