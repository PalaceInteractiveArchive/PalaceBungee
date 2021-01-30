package network.palace.bungee.commands.moderation;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.utils.MiscUtil;

public class StrictCommand extends PalaceCommand {

    public StrictCommand() {
        super("strict", Rank.MOD);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "You must supply an argument! /strict [toggle:threshold] [args...]");
            return;
        }
        try {
            if (args[0].equalsIgnoreCase("toggle")) {
                String response = PalaceBungee.getConfigUtil().isStrictChat() ? ChatColor.GREEN + "Leaving strict mode..." :
                        ChatColor.RED + "Entering strict mode... Matching level: " + PalaceBungee.getConfigUtil().getStrictThreshold();
                PalaceBungee.getConfigUtil().setStrictChat(!PalaceBungee.getConfigUtil().isStrictChat());
                player.sendMessage(response);
            } else if (args[0].equalsIgnoreCase("threshold")) {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "You must supply a threshold! /strict threshold [threshold]");
                    return;
                }
                if (!MiscUtil.isDouble(args[1])) {
                    player.sendMessage(ChatColor.RED + "Invalid threshold!");
                    return;
                }
                double threshold = Double.parseDouble(args[1]);
                PalaceBungee.getConfigUtil().setStrictThreshold(threshold);
                player.sendMessage(ChatColor.GREEN + "Strict mode threshold has been set to " + threshold);
            }
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred while modifying strict chat settings. Check console for errors.");
        }
    }
}