package network.palace.bungee.commands.admin;

import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;

public class MaintenanceCommand extends PalaceCommand {

    public MaintenanceCommand() {
        super("maintenance", Rank.DEVELOPER);
    }

    @Override
    public void execute(Player player, String[] args) {
    }
}