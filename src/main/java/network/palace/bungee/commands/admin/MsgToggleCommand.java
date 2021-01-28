package network.palace.bungee.commands.admin;

import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;

public class MsgToggleCommand extends PalaceCommand {

    public MsgToggleCommand() {
        super("msgtoggle", Rank.DEVELOPER);
    }

    @Override
    public void execute(Player player, String[] args) {
    }
}