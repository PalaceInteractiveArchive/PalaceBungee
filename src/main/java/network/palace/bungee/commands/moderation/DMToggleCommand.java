package network.palace.bungee.commands.moderation;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;

public class DMToggleCommand extends PalaceCommand {

    public DMToggleCommand() {
        super("dmtoggle", Rank.CM);
    }

    @Override
    public void execute(Player player, String[] args) {
        boolean enabled = !PalaceBungee.getConfigUtil().isDmEnabled();
        try {
            PalaceBungee.getConfigUtil().setDmEnabled(enabled);
            PalaceBungee.getMessageHandler().sendStaffMessage(ChatColor.GREEN + "Direct messages have been " + (enabled ? "enabled" : ChatColor.RED +
                    "disabled" + ChatColor.GREEN) + " by " + player.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred while toggling direct messages. Check console for errors.");
        }
    }
}