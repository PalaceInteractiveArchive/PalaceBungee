package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.messages.packets.PlayerQueuePacket;
import org.bson.Document;

import java.util.logging.Level;

public class VirtualQueueJoinCommand extends PalaceCommand {

    public VirtualQueueJoinCommand() {
        super("vqjoin");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 1) return;
        try {
            Document queueDoc = PalaceBungee.getMongoHandler().getVirtualQueue(args[0]);
            if (queueDoc == null) return;
            PalaceBungee.getMessageHandler().sendDirectServerMessage(new PlayerQueuePacket(queueDoc.getString("queueId"), player.getUniqueId(), true), queueDoc.getString("server"));
        } catch (Exception e) {
            PalaceBungee.getInstance().getLogger().log(Level.SEVERE, "Error requesting player to join virtual queue", e);
            player.sendMessage(ChatColor.RED + "An error occurred while joining that virtual queue, try again in a few minutes!");
        }
    }
}
