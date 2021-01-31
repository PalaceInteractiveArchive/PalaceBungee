package network.palace.bungee.commands.staff;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.moderation.Ban;
import network.palace.bungee.messages.packets.DisablePlayerPacket;
import network.palace.bungee.slack.SlackAttachment;
import network.palace.bungee.slack.SlackMessage;

import java.util.Collections;
import java.util.UUID;
import java.util.logging.Level;

public class StaffCommand extends PalaceCommand {

    public StaffCommand() {
        super("staff", Rank.TRAINEE);
    }

    @Override
    public void execute(Player player, String[] args) {
        PalaceBungee.getProxyServer().getScheduler().runAsync(PalaceBungee.getInstance(), () -> {
            try {
                boolean disabled = player.isDisabled();
                if ((args.length == 0 && disabled) || (disabled && !args[0].equalsIgnoreCase("login"))) {
                    player.sendMessage(ChatColor.GREEN + "/staff login [password]");
                    return;
                }
                if (args.length == 2 && args[0].equalsIgnoreCase("login")) {
                    if (!player.isDisabled()) {
                        player.sendMessage(ChatColor.GREEN + "You're already logged in!");
                        return;
                    }
                    if (PalaceBungee.getMongoHandler().verifyPassword(player.getUniqueId(), args[1])) {
                        player.sendMessage(ChatColor.GREEN + "You logged in!");
                        player.setDisabled(false);
                        PalaceBungee.getMongoHandler().updateAddress(player.getUniqueId(), player.getAddress());
                        PalaceBungee.getMongoHandler().setStaffPasswordAttempts(player.getUniqueId(), 0);
                        PalaceBungee.getMessageHandler().sendStaffMessage(player.getRank().getFormattedName() + ChatColor.YELLOW +
                                " " + player.getUsername() + " has logged in!");
                        player.sendPacket(new DisablePlayerPacket(player.getUniqueId(), false), true);
                        SlackMessage m = new SlackMessage("");
                        SlackAttachment a = new SlackAttachment("[Successful] *" + player.getRank().getName() + "* `" +
                                player.getUsername() + "` `" + player.getAddress() + "`");
                        a.color("good");
                        PalaceBungee.getSlackUtil().sendDashboardMessage(m, Collections.singletonList(a), false);
                    } else {
                        int attempts = PalaceBungee.getMongoHandler().getStaffPasswordAttempts(player.getUniqueId()) + 1;
                        if (attempts >= 5) {
                            Ban ban = new Ban(player.getUniqueId(), player.getUsername(), true, System.currentTimeMillis(),
                                    "Locked out of staff account", "Network");
                            PalaceBungee.getMongoHandler().banPlayer(player.getUniqueId(), ban);
                            PalaceBungee.getModerationUtil().announceBan(ban);
                            PalaceBungee.getMessageHandler().sendStaffMessage(ChatColor.RED + player.getUsername() + " has been locked out of their account!");
                            player.kickPlayer(ChatColor.RED + "Locked out of staff account. Please contact management to unlock your account.");
                            PalaceBungee.getMongoHandler().setStaffPasswordAttempts(player.getUniqueId(), 0);
                            SlackMessage m = new SlackMessage("<!channel> *" + player.getUsername() + " Locked Out*");
                            SlackAttachment a = new SlackAttachment("*[Locked] " + player.getRank().getName() + "* `" +
                                    player.getUsername() + "` `" + player.getAddress() + "`");
                            a.color("danger");
                            PalaceBungee.getSlackUtil().sendDashboardMessage(m, Collections.singletonList(a), false);
                            return;
                        }
                        PalaceBungee.getMongoHandler().setStaffPasswordAttempts(player.getUniqueId(), attempts);
                        PalaceBungee.getMessageHandler().sendStaffMessage(ChatColor.GOLD + player.getUsername() + " attempted to login but failed! (" + attempts + "/5)");
                        player.sendMessage(ChatColor.RED + "Incorrect password!");
                        SlackMessage m = new SlackMessage("");
                        SlackAttachment a = new SlackAttachment("[" + attempts + "/5] *" + player.getRank().getName() + "* `" +
                                player.getUsername() + "` `" + player.getAddress() + "`");
                        a.color("warning");
                        PalaceBungee.getSlackUtil().sendDashboardMessage(m, Collections.singletonList(a), false);
                    }
                    return;
                }
                if (args.length == 3) {
                    if (args[0].equalsIgnoreCase("change")) {
                        String oldp = args[1];
                        String newp = args[2];
                        if (newp.length() > 128) {
                            player.sendMessage(ChatColor.RED + "Passwords cannot be larger than 128 characters!");
                            return;
                        }
                        if (!PalaceBungee.getPasswordUtil().isStrongEnough(newp)) {
                            player.sendMessage(ChatColor.RED + "This password is not secure enough! Make sure it has:\n- at least 8 characters\n- a lowercase letter\n- an uppercase letter\n- a number");
                            return;
                        }
                        if (!PalaceBungee.getMongoHandler().verifyPassword(player.getUniqueId(), oldp)) {
                            player.sendMessage(ChatColor.RED + "Your existing password is incorrect!");
                            SlackMessage m = new SlackMessage("");
                            SlackAttachment a = new SlackAttachment("[Failed PW Change] *" + player.getRank().getName() + "* `" +
                                    player.getUsername() + "` `" + player.getAddress() + "`");
                            a.color("warning");
                            PalaceBungee.getSlackUtil().sendDashboardMessage(m, Collections.singletonList(a), false);
                            return;
                        }
                        PalaceBungee.getMongoHandler().setPassword(player.getUniqueId(), newp);
                        player.sendMessage(ChatColor.GREEN + "Your password was successfully changed!");
                        SlackMessage m = new SlackMessage("");
                        SlackAttachment a = new SlackAttachment("[PW Changed] *" + player.getRank().getName() + "* `" +
                                player.getUsername() + "` `" + player.getAddress() + "`");
                        a.color("good");
                        PalaceBungee.getSlackUtil().sendDashboardMessage(m, Collections.singletonList(a), false);
                        return;
                    } else if (args[0].equalsIgnoreCase("force") && player.getRank().getRankId() >= Rank.DEVELOPER.getRankId()) {
                        String username;
                        String pass = args[2];
                        UUID uuid = PalaceBungee.getMongoHandler().usernameToUUID(args[1]);
                        if (uuid == null) {
                            player.sendMessage(ChatColor.RED + "No player was found with the username '" +
                                    ChatColor.GREEN + args[1] + ChatColor.RED + "'!");
                            return;
                        }
                        username = PalaceBungee.getMongoHandler().uuidToUsername(uuid);
                        if (username.equalsIgnoreCase("unknown")) {
                            player.sendMessage(ChatColor.RED + "No player was found with the username '" +
                                    ChatColor.GREEN + args[1] + ChatColor.RED + "'!");
                            return;
                        }
                        if (pass.length() > 128) {
                            player.sendMessage(ChatColor.RED + "Passwords cannot be larger than 128 characters!");
                            return;
                        }
                        if (!PalaceBungee.getPasswordUtil().isStrongEnough(pass)) {
                            player.sendMessage(ChatColor.RED + "This password is not secure enough! Make sure it has:\n- at least 8 characters\n- a lowercase letter\n- an uppercase letter\n- a number");
                            return;
                        }
                        PalaceBungee.getMongoHandler().setPassword(uuid, pass);
                        player.sendMessage(ChatColor.GREEN + username + "'s password was successfully changed!");
                        SlackMessage m = new SlackMessage("");
                        SlackAttachment a = new SlackAttachment("[PW Force-Changed] `" + username +
                                "` *changed by* `" + player.getUsername() + "` `" + player.getAddress() + "`");
                        a.color("good");
                        PalaceBungee.getSlackUtil().sendDashboardMessage(m, Collections.singletonList(a), false);
                        return;
                    }
                }
                player.sendMessage(ChatColor.GREEN + "Staff commands:");
                player.sendMessage(ChatColor.GREEN + "/staff login [password] " + ChatColor.YELLOW + "- Login to a staff account");
                player.sendMessage(ChatColor.GREEN + "/staff change [old password] [new password] " +
                        ChatColor.YELLOW + "- Change your staff password");
                if (player.getRank().getRankId() >= Rank.DEVELOPER.getRankId()) {
                    player.sendMessage(ChatColor.GOLD + "/staff force [Username] [Password] - Force-change a staff member's password");
                }
            } catch (Exception e) {
                PalaceBungee.getProxyServer().getLogger().log(Level.SEVERE, "Error processing /staff command", e);
            }
        });
    }
}