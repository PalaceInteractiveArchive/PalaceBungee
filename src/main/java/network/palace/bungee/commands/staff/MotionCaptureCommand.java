package network.palace.bungee.commands.staff;

import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;

public class MotionCaptureCommand extends PalaceCommand {

    public MotionCaptureCommand() {
        super("mocap", RANK.CM);
    }

    @Override
    public void execute(Player player, String[] args) {
        player.chat("/motioncapture:mc " + String.join(" ", args));
    }
}