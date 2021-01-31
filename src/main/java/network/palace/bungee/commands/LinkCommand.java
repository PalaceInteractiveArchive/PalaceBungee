package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;

public class LinkCommand extends PalaceCommand {

    public LinkCommand() {
        super("link");
    }

    @Override
    public void execute(Player player, String[] args) {
//        if (player.getRank().getRankId() < Rank.TRAINEE.getRankId()) {
//            player.sendMessage(ChatColor.YELLOW + "You will be able to link your forum account with your Minecraft account soon!");
//            return;
//        }
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "/link [email address]");
            return;
        }
        switch (args[0].toLowerCase()) {
            case "cancel": {
                PalaceBungee.getForumUtil().unlinkAccount(player);
                return;
            }
            case "confirm": {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "/link confirm [six-digit code]");
                    return;
                }
                PalaceBungee.getForumUtil().confirm(player, args[1]);
                return;
            }
        }
        String email = args[0];
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            player.sendMessage(ChatColor.RED + "That isn't a valid email!");
            return;
        }
        PalaceBungee.getForumUtil().linkAccount(player, email);
    }
}