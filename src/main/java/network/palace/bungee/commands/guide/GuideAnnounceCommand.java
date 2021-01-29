package network.palace.bungee.commands.guide;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.RankTag;

public class GuideAnnounceCommand extends PalaceCommand {

    public GuideAnnounceCommand() {
        super("gannounce", Rank.TRAINEE, RankTag.GUIDE);
    }

    @Override
    public void execute(Player player, String[] args) {
        Rank rank = player.getRank();
        if ((rank.getRankId() < Rank.MOD.getRankId() && rank.getRankId() >= Rank.TRAINEE.getRankId()) ||
                (rank.getRankId() < Rank.TRAINEE.getRankId() && player.hasTag(RankTag.GUIDE))) {
            // if trainee or non-staff guide
            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "/gannounce [Message]");
                return;
            }
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                s.append(args[i]);
                if (i <= (args.length - 1)) {
                    s.append(" ");
                }
            }
            PalaceBungee.getGuideUtil().sendAnnouncementRequest(player, ChatColor.translateAlternateColorCodes('&', s.toString()));
        } else {
            if (args.length < 2) {
                player.sendMessage(ChatColor.GREEN + "Guide Announce Commands:");
                player.sendMessage(ChatColor.AQUA + "/gannounce accept [Username] - Accept a player's announcement request");
                player.sendMessage(ChatColor.AQUA + "/gannounce decline [Username] - Decline a player's announcement request");
                return;
            }
            switch (args[0].toLowerCase()) {
                case "accept": {
                    PalaceBungee.getGuideUtil().acceptAnnouncementRequest(player, args[1]);
                    break;
                }
                case "decline": {
                    PalaceBungee.getGuideUtil().declineAnnouncementRequest(player, args[1]);
                    break;
                }
                default: {
                    player.sendMessage(ChatColor.GREEN + "Guide Announce Commands:");
                    player.sendMessage(ChatColor.AQUA + "/gannounce accept [Username] - Accept a player's announcement request");
                    player.sendMessage(ChatColor.AQUA + "/gannounce decline [Username] - Decline a player's announcement request");
                }
            }
        }
    }
}