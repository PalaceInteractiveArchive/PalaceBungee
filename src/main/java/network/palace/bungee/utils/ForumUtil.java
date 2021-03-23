package network.palace.bungee.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

public class ForumUtil {
    private HikariDataSource connectionPool = null;
    private Random random;

    public ForumUtil() throws IOException, SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        initialize();
    }

    public void initialize() throws IOException, SQLException {
        ConfigUtil.DatabaseConnection sql = PalaceBungee.getConfigUtil().getSQLInfo();

        // Begin configuration of Hikari DataSource
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + sql.getHost() + ":" + sql.getPort() + "/" + sql.getDatabase());
        config.setUsername(sql.getUsername());
        config.setPassword(sql.getPassword());

        // See: https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        connectionPool = new HikariDataSource(config);
    }

    public boolean isConnected() {
        return connectionPool != null && connectionPool.isRunning();
    }

    public void stop() {
        connectionPool.close();
    }

    public void linkAccount(Player player, String email) {
        if (!isConnected()) {
            player.sendMessage(ChatColor.RED + "Could not connect to database, please try again later!");
            return;
        }
        try (Connection connection = connectionPool.getConnection()) {
            //Check Minecraft account isn't already linked to a forum account.
            PreparedStatement checkMcNotLinkedSql = connection.prepareStatement("SELECT * FROM core_pfields_content WHERE field_3=?");
            checkMcNotLinkedSql.setString(1, player.getUniqueId().toString());
            ResultSet checkMcNotLinkedResult = checkMcNotLinkedSql.executeQuery();
            if (checkMcNotLinkedResult.next()) {
                player.sendMessage(ChatColor.RED + "Your Minecraft account is already linked to a Forum account. To unlink, type /link cancel.");
                checkMcNotLinkedResult.close();
                checkMcNotLinkedSql.close();
                return;
            }

            //Check forum account exists with provided email.
            PreparedStatement memberIdSql = connection.prepareStatement("SELECT member_id FROM core_members WHERE email=?");
            memberIdSql.setString(1, email);
            ResultSet memberIdResult = memberIdSql.executeQuery();
            if (!memberIdResult.next()) {
                player.sendMessage(ChatColor.RED + "There is no forum account with that email address!");
                player.sendMessage(ChatColor.YELLOW + "Register for our forums here: " + ChatColor.GOLD + "https://forums.palace.network/register/");
                memberIdResult.close();
                memberIdSql.close();
                return;
            }
            int member_id = memberIdResult.getInt("member_id");
            memberIdResult.close();
            memberIdSql.close();

            //Check forum account isn't already linked to a Minecraft account.
            PreparedStatement checkNotLinkedSql = connection.prepareStatement("SELECT * FROM core_pfields_content WHERE member_id=?");
            checkNotLinkedSql.setInt(1, member_id);
            ResultSet checkNotLinkedResult = checkNotLinkedSql.executeQuery();
            if (!checkNotLinkedResult.next()) {
                player.sendMessage(ChatColor.RED + "There was an error, please try again later!");
                checkNotLinkedResult.close();
                checkNotLinkedSql.close();
                return;
            }
            String uuidField = checkNotLinkedResult.getString("field_3");
            String usernameField = checkNotLinkedResult.getString("field_4");
            checkNotLinkedResult.close();
            checkNotLinkedSql.close();
            if ((uuidField != null && !uuidField.isEmpty()) || (usernameField != null && !usernameField.isEmpty())) {
                player.sendMessage(ChatColor.RED + "This forum account is already linked to a different Minecraft account!");
                return;
            }

            String linkingCode = getRandomNumberString();
            PalaceBungee.getMongoHandler().setForumLinkingCode(player.getUniqueId(), member_id, linkingCode);
            //Set Linking Code value for forum account.
            PreparedStatement setLinkCodeSql = connection.prepareStatement("UPDATE core_pfields_content SET field_2=? WHERE member_id=?");
            setLinkCodeSql.setString(1, linkingCode);
            setLinkCodeSql.setInt(2, member_id);
            setLinkCodeSql.execute();
            setLinkCodeSql.close();

            player.sendMessage(ChatColor.GREEN + "Okay, you're almost finished linking! Visit your Forum Profile and look for the six-digit Linking Code. When you've found it, run " +
                    ChatColor.YELLOW + "/link confirm [six-digit code].");
            player.sendMessage(new ComponentBuilder("If you need help, check out our guide ").color(ChatColor.GREEN)
                    .append("here!").color(ChatColor.YELLOW)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("Click to visit ").color(ChatColor.GREEN)
                                    .append("https://forums.palace.network/topic/6141-link-your-minecraft-and-forum-accounts/")
                                    .color(ChatColor.YELLOW).create()))
                    .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://forums.palace.network/topic/6141-link-your-minecraft-and-forum-accounts/")).create());
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "There was an error, please try again later!");
            PalaceBungee.getProxyServer().getLogger().log(Level.SEVERE, "Error linking forum account", e);
        }
    }

    public void unlinkAccount(Player player) {
        try {
            int member_id = PalaceBungee.getMongoHandler().getForumMemberId(player.getUniqueId());
            if (PalaceBungee.getMongoHandler().getForumLinkingCode(player.getUniqueId()) != null || member_id < 0) {
                player.sendMessage(ChatColor.RED + "Your Minecraft and Forums accounts are not currently linked!");
                return;
            }
            player.sendMessage(ChatColor.GREEN + "Unlinking your Minecraft and Forums accounts...");
            try (Connection connection = connectionPool.getConnection()) {
                //Unset uuid/username on forum account
                PreparedStatement unsetPlayerDataSql = connection.prepareStatement("UPDATE core_pfields_content SET field_2=?,field_3=?,field_4=? WHERE member_id=?");
                unsetPlayerDataSql.setString(1, null);
                unsetPlayerDataSql.setString(2, null);
                unsetPlayerDataSql.setString(3, null);
                unsetPlayerDataSql.setInt(4, member_id);
                unsetPlayerDataSql.execute();
                unsetPlayerDataSql.close();

                if (player.getRank().getRankId() < Rank.TRAINEE.getRankId()) {
                    //Set forum group to member
                    PreparedStatement setForumGroupSql = connection.prepareStatement("UPDATE core_members SET member_group_id=? WHERE member_id=?");
                    setForumGroupSql.setInt(1, 3);
                    setForumGroupSql.setInt(2, member_id);
                    setForumGroupSql.execute();
                    setForumGroupSql.close();
                }
            } catch (SQLException e) {
                PalaceBungee.getProxyServer().getLogger().log(Level.SEVERE, "Error unlinking forum account", e);
            }
            PalaceBungee.getMongoHandler().unlinkForumAccount(player.getUniqueId());
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "There was an error, please try again later!");
            PalaceBungee.getProxyServer().getLogger().log(Level.SEVERE, "Error unlinking forum account", e);
        }
        player.sendMessage(ChatColor.GREEN + "Your Minecraft and Forums accounts are no longer linked.");
    }

    private static String getRandomNumberString() {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        return String.format("%06d", number);
    }

    public void confirm(Player player, String code) {
        String correctCode = PalaceBungee.getMongoHandler().getForumLinkingCode(player.getUniqueId());
        if (correctCode == null) {
            player.sendMessage(ChatColor.RED + "Before you can confirm, first you need to start the linking process: /link [email address]");
            return;
        }
        if (!code.equals(correctCode)) {
            player.sendMessage(ChatColor.RED + "That isn't the right code!");
            return;
        }
        player.sendMessage(ChatColor.GREEN + "Code accepted! We're finishing the linking process right now...");
        int member_id = PalaceBungee.getMongoHandler().getForumMemberId(player.getUniqueId());
        if (member_id < 0) {
            player.sendMessage(ChatColor.RED + "Uh oh, there was a problem! (Error code 121)");
            return;
        }
        try (Connection connection = connectionPool.getConnection()) {
            //Unset Linking Code value and setting uuid/username on forum account
            PreparedStatement unsetLinkCodeSql = connection.prepareStatement("UPDATE core_pfields_content SET field_2=?,field_3=?,field_4=? WHERE member_id=?");
            unsetLinkCodeSql.setString(1, null);
            unsetLinkCodeSql.setString(2, player.getUniqueId().toString());
            unsetLinkCodeSql.setString(3, player.getUsername());
            unsetLinkCodeSql.setInt(4, member_id);
            unsetLinkCodeSql.execute();
            unsetLinkCodeSql.close();

            //Set forum account group to match player rank (if Developer or above, this isn't automated!)
            if (player.getRank().getRankId() < Rank.DEVELOPER.getRankId()) {
                int forumGroup = getForumGroup(player.getRank());
                PreparedStatement setForumGroupSql = connection.prepareStatement("UPDATE core_members SET member_group_id=? WHERE member_id=?");
                setForumGroupSql.setInt(1, forumGroup);
                setForumGroupSql.setInt(2, member_id);
                setForumGroupSql.execute();
                setForumGroupSql.close();
            } else {
                player.sendMessage(ChatColor.RED + "Note: Since your rank is " + player.getRank().getFormattedName() + ChatColor.RED + ", your forum group will not be automatically set.");
            }

            //Unset Linking Code value on mongo document
            PalaceBungee.getMongoHandler().unsetForumLinkingCode(player.getUniqueId());
        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "There was an error, please try again later!");
            PalaceBungee.getProxyServer().getLogger().log(Level.SEVERE, "Error confirming forum account", e);
            return;
        }
        player.sendMessage(ChatColor.GREEN + "All done! Your Minecraft and Forums accounts are now linked.");
    }

    public void updatePlayerName(UUID uuid, int member_id, String newUsername) {
        try (Connection connection = connectionPool.getConnection()) {
            // field_4 is username, field_3 is uuid
            PreparedStatement sql = connection.prepareStatement("UPDATE core_pfields_content SET field_4=? WHERE member_id=? AND field_3=?");
            sql.setString(1, newUsername);
            sql.setInt(2, member_id);
            sql.setString(3, uuid.toString());
            sql.execute();
            sql.close();
        } catch (SQLException e) {
            PalaceBungee.getProxyServer().getLogger().log(Level.SEVERE, "Error updating name on forum account", e);
        }
    }

    public void updatePlayerRank(UUID uuid, int member_id, Rank rank, Player player) {
        if (rank.getRankId() >= Rank.DEVELOPER.getRankId()) {
            if (player != null)
                player.sendMessage(ChatColor.RED + "Note: Since your rank is " + player.getRank().getFormattedName() +
                        ChatColor.RED + ", your forum group will not be automatically set.");
            return;
        }
        try (Connection connection = connectionPool.getConnection()) {
            int forumGroup = getForumGroup(rank);
            PreparedStatement setForumGroupSql = connection.prepareStatement("UPDATE core_members SET member_group_id=? WHERE member_id=?");
            setForumGroupSql.setInt(1, forumGroup);
            setForumGroupSql.setInt(2, member_id);
            setForumGroupSql.execute();
            setForumGroupSql.close();
        } catch (SQLException e) {
            PalaceBungee.getProxyServer().getLogger().log(Level.SEVERE, "Error updating rank on forum account", e);
        }
    }

    private int getForumGroup(Rank rank) {
        switch (rank) {
            case EXEC:
                return 4;
            case MANAGER:
                return 33;
            case LEAD:
                return 7;
            case DEVELOPER:
                return 8;
            case COORDINATOR:
                return 9;
            case BUILDER:
                return 19;
            case IMAGINEER:
                return 35;
            case MEDIA:
                return 37;
            case CM:
                return 6;
            case TRAINEETECH:
                return 34;
            case TRAINEEBUILD:
                return 46;
            case TRAINEE:
                return 10;
            case VIP:
                return 22;
            case SHAREHOLDER:
                return 38;
            case CLUB:
                return 17;
            case DVC:
                return 16;
            case PASSPORT:
                return 15;
            case PASSHOLDER:
                return 14;
        }
        return 13;
    }
}