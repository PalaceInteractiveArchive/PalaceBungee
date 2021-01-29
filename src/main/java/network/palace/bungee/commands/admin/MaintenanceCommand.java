package network.palace.bungee.commands.admin;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;

public class MaintenanceCommand extends PalaceCommand {

    public MaintenanceCommand() {
        super("maintenance", Rank.DEVELOPER);
    }

    @Override
    public void execute(Player player, String[] args) {
        boolean maintenance = !PalaceBungee.getConfigUtil().isMaintenance();
        try {
            PalaceBungee.getConfigUtil().setMaintenanceMode(maintenance);
            player.sendMessage(ChatColor.AQUA + "Maintenance mode has been " +
                    (maintenance ? (ChatColor.RED + "enabled! Disconnecting all players below Developer...") : (ChatColor.GREEN + "disabled! All players can connect to the network.")));
            
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "There was an error toggling maintenance mode! Check console for details.");
        }
    }
}