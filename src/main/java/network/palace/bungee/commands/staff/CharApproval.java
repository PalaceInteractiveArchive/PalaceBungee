package network.palace.bungee.commands.staff;


import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.chat.ComponentSerializer;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.messages.packets.BroadcastPacket;
import network.palace.bungee.messages.packets.MessageByRankPacket;
import network.palace.bungee.slack.SlackAttachment;
import network.palace.bungee.slack.SlackMessage;

import java.io.IOException;
import java.util.Collections;

public class CharApproval extends PalaceCommand {

    public CharApproval() {
        super("charlogin", Rank.CM);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "/charlogin [player] [a/d] " + ChatColor.AQUA + " - Approves/Denies login for Character");
            return;
        }
        Player target = PalaceBungee.getPlayer(args[0]);
        if (target.isDisabled() && target.getRank() == Rank.CHARACTER) {
            if (args[1].equals("a")) {
                target.sendMessage(ChatColor.GREEN + "Login approved by " + player.getUsername());
                target.setDisabled(false);
                SlackMessage m = new SlackMessage("");
                SlackAttachment a = new SlackAttachment("*Character* `" + target.getUsername() +
                        "` Login approved by: `" + player.getUsername() + "`");
                a.color("good");
                PalaceBungee.getSlackUtil().sendDashboardMessage(m, Collections.singletonList(a), false);
                BaseComponent[] components = new ComponentBuilder("[").color(ChatColor.WHITE)
                        .append("STAFF").color(ChatColor.RED).append("] ").color(ChatColor.WHITE)
                        .append(target.getUsername()).color(target.getRank().getTagColor())
                        .append(" had login approved by: ").color(ChatColor.AQUA)
                        .append(player.getUsername()).color(player.getRank().getTagColor()).create();
                try {
                    PalaceBungee.getMessageHandler().sendMessage(new MessageByRankPacket(ComponentSerializer.toString(components), Rank.TRAINEE, null, false, true), PalaceBungee.getMessageHandler().ALL_PROXIES);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (args[1].equals("d")) {
                target.kickPlayer("Character account is not authorised for login");
                player.sendMessage(ChatColor.RED + "Kicked player");
                SlackMessage m = new SlackMessage("");
                SlackAttachment a = new SlackAttachment("*Character* `" + target.getUsername() +
                        "` Login denied by: `" + player.getUsername() + "`");
                a.color("warning");
                PalaceBungee.getSlackUtil().sendDashboardMessage(m, Collections.singletonList(a), false);
                BaseComponent[] components = new ComponentBuilder("[").color(ChatColor.WHITE)
                        .append("STAFF").color(ChatColor.RED).append("] ").color(ChatColor.WHITE)
                        .append(target.getUsername()).color(target.getRank().getTagColor())
                        .append(" had login denied by: ").color(ChatColor.AQUA)
                        .append(player.getUsername()).color(player.getRank().getTagColor()).create();
                try {
                    PalaceBungee.getMessageHandler().sendMessage(new MessageByRankPacket(ComponentSerializer.toString(components), Rank.TRAINEE, null, false, true), PalaceBungee.getMessageHandler().ALL_PROXIES);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }  else {
            player.sendMessage(ChatColor.RED + "This player does not need to be approved");
        }
    }
}

