package network.palace.bungee;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import network.palace.bungee.commands.*;
import network.palace.bungee.commands.admin.*;
import network.palace.bungee.commands.chat.*;
import network.palace.bungee.commands.guide.GuideAnnounceCommand;
import network.palace.bungee.commands.guide.GuideHelpCommand;
import network.palace.bungee.commands.guide.GuideListCommand;
import network.palace.bungee.commands.guide.HelpMeCommand;
import network.palace.bungee.commands.moderation.*;
import network.palace.bungee.commands.staff.*;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.listeners.PlayerChat;
import network.palace.bungee.listeners.PlayerJoinAndLeave;
import network.palace.bungee.listeners.ProxyPing;
import network.palace.bungee.listeners.ServerSwitch;
import network.palace.bungee.messages.MessageHandler;
import network.palace.bungee.mongo.MongoHandler;
import network.palace.bungee.utils.*;
import network.palace.bungee.utils.chat.JaroWinkler;
import network.palace.bungee.handlers.ShowReminder;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

public class PalaceBungee extends Plugin {
    @Getter private static final UUID proxyID = UUID.randomUUID();
    @Getter private static PalaceBungee instance;
    @Getter private static ConfigUtil configUtil;
    @Getter private static ServerUtil serverUtil;

    @Getter private static AFKUtil afkUtil;
    @Getter private static BroadcastUtil broadcastUtil;
    @Getter private static final JaroWinkler chatAlgorithm = new JaroWinkler();
    @Getter private static ChatUtil chatUtil;
    @Getter private static ForumUtil forumUtil;
    @Getter private static GuideUtil guideUtil;
    @Getter private static ModerationUtil moderationUtil;
    @Getter private static PartyUtil partyUtil;
    @Getter private static PasswordUtil passwordUtil;
    @Getter private static SlackUtil slackUtil;

    @Getter private static MongoHandler mongoHandler;
    @Getter private static MessageHandler messageHandler;

    @Getter private static final long startTime = System.currentTimeMillis();
    private final static HashMap<UUID, Player> players = new HashMap<>();

    @Getter private final static HashMap<UUID, String> usernameCache = new HashMap<>();

    @Getter private static boolean testNetwork;

    @Override
    public void onEnable() {
        instance = this;

        configUtil = new ConfigUtil();
        try {
            testNetwork = configUtil.isTestNetwork();
        } catch (IOException e) {
            testNetwork = true;
            getLogger().log(Level.WARNING, "Error loading testNetwork setting from config file, defaulting to testNetwork ENABLED", e);
        }
        if (testNetwork) getLogger().log(Level.WARNING, "Test network enabled!");

        try {
            mongoHandler = new MongoHandler();
            PalaceBungee.getConfigUtil().reload();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            forumUtil = new ForumUtil();
        } catch (Exception e) {
            e.printStackTrace();
        }

        serverUtil = new ServerUtil();

        try {
            messageHandler = new MessageHandler();
            messageHandler.initialize();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

        afkUtil = new AFKUtil();
        broadcastUtil = new BroadcastUtil();
        chatUtil = new ChatUtil();
        guideUtil = new GuideUtil();
        moderationUtil = new ModerationUtil();
        partyUtil = new PartyUtil();
        passwordUtil = new PasswordUtil();
        slackUtil = new SlackUtil();

        setupShowReminder();

        registerListeners();
        registerCommands();
    }

    @Override
    public void onDisable() {
        if (messageHandler != null) messageHandler.shutdown();
    }

    private void registerListeners() {
        PluginManager pm = getProxy().getPluginManager();
        pm.registerListener(this, new PlayerChat());
        pm.registerListener(this, new PlayerJoinAndLeave());
        pm.registerListener(this, new ProxyPing());
        pm.registerListener(this, new ServerSwitch());
    }

    private void registerCommands() {
        PluginManager pm = getProxy().getPluginManager();
        /* Admin Commands */
        pm.registerCommand(this, new GuideLogCommand());
        pm.registerCommand(this, new MaintenanceCommand());
        pm.registerCommand(this, new MsgToggleCommand());
        pm.registerCommand(this, new ProxyCountsCommand());
        pm.registerCommand(this, new ProxyReloadCommand());
        pm.registerCommand(this, new ProxyVersionCommand());
        pm.registerCommand(this, new SendCommand());
        pm.registerCommand(this, new UpdateHashesCommand());
        /* Chat Commands */
        pm.registerCommand(this, new AdminChatCommand());
        pm.registerCommand(this, new ChatCommand());
        pm.registerCommand(this, new ChatDelayCommand());
        pm.registerCommand(this, new ChatStatusCommand());
        pm.registerCommand(this, new ClearChatCommand());
        pm.registerCommand(this, new GuideChatCommand());
        pm.registerCommand(this, new PartyChatCommand());
        pm.registerCommand(this, new StaffChatCommand());
        /* Guide Commands */
        pm.registerCommand(this, new GuideAnnounceCommand());
        pm.registerCommand(this, new GuideHelpCommand());
        pm.registerCommand(this, new GuideListCommand());
        pm.registerCommand(this, new HelpMeCommand());
        /* Moderation Commands */
        pm.registerCommand(this, new AltAccountsCommand());
        pm.registerCommand(this, new BanCommand());
        pm.registerCommand(this, new BanIPCommand());
        pm.registerCommand(this, new BannedProvidersCommand());
        pm.registerCommand(this, new BanProviderCommand());
        pm.registerCommand(this, new DMToggleCommand());
        pm.registerCommand(this, new FindCommand());
        pm.registerCommand(this, new IPCommand());
        pm.registerCommand(this, new KickCommand());
        pm.registerCommand(this, new LookupCommand());
        pm.registerCommand(this, new ModlogCommand());
        pm.registerCommand(this, new MuteChatCommand());
        pm.registerCommand(this, new MuteCommand());
        pm.registerCommand(this, new NamecheckCommand());
        pm.registerCommand(this, new PartiesCommand());
        pm.registerCommand(this, new StrictCommand());
        pm.registerCommand(this, new TempBanCommand());
        pm.registerCommand(this, new UnbanCommand());
        pm.registerCommand(this, new UnbanIPCommand());
        pm.registerCommand(this, new UnbanProviderCommand());
        pm.registerCommand(this, new UnmuteCommand());
        pm.registerCommand(this, new WarnCommand());
        /* Staff Commands */
        pm.registerCommand(this, new BroadcastClockCommand());
        pm.registerCommand(this, new BroadcastCommand());
        pm.registerCommand(this, new CharListCommand());
        pm.registerCommand(this, new DirectChatCommand());
        pm.registerCommand(this, new MotionCaptureCommand());
        pm.registerCommand(this, new ServerCommand());
        pm.registerCommand(this, new SGListCommand());
        pm.registerCommand(this, new StaffCommand());
        pm.registerCommand(this, new StaffListCommand());
        /* General Commands */
        pm.registerCommand(this, new ApplyCommand());
        pm.registerCommand(this, new AudioCommand());
        pm.registerCommand(this, new BugCommand());
        pm.registerCommand(this, new DiscordCommand());
        pm.registerCommand(this, new IgnoreCommand());
        pm.registerCommand(this, new FriendCommand());
        pm.registerCommand(this, new JoinCommand());
        pm.registerCommand(this, new LinkCommand());
        pm.registerCommand(this, new MentionsCommand());
        pm.registerCommand(this, new MsgCommand());
        pm.registerCommand(this, new OnlineCountCommand());
        pm.registerCommand(this, new PartyCommand());
        pm.registerCommand(this, new PunishmentsCommand());
        pm.registerCommand(this, new ReplyCommand());
        pm.registerCommand(this, new RulesCommand());
        pm.registerCommand(this, new SocialCommand());
        pm.registerCommand(this, new StoreCommand());
        pm.registerCommand(this, new UptimeCommand());
        pm.registerCommand(this, new VirtualQueueJoinCommand());
        pm.registerCommand(this, new WhereAmICommand());
    }

    public static ProxyServer getProxyServer() {
        return instance.getProxy();
    }

    public static Player getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public static Player getPlayer(String name) {
        Player p = null;
        for (Player tp : players.values()) {
            if (tp.getUsername().equalsIgnoreCase(name)) {
                p = tp;
                break;
            }
        }
        return p;
    }

    public static void login(Player player) {
        players.put(player.getUniqueId(), player);
        mongoHandler.login(player);
        if (player.isNewGuest()) {
            player.runTutorial();
        }
    }

    public static void logout(UUID uuid, Player player) {
        if (player != null && player.isNewGuest()) player.cancelTutorial();
        players.remove(uuid);
        mongoHandler.logout(uuid, player);
    }

    public static Collection<Player> getOnlinePlayers() {
        return new ArrayList<>(players.values());
    }

    public static String getUsername(UUID uuid) {
        String name = usernameCache.get(uuid);
        if (name == null) {
            name = mongoHandler.uuidToUsername(uuid);
            if (name == null) {
                name = "Unknown";
            } else {
                PalaceBungee.getUsernameCache().put(uuid, name);
            }
        }
        return name;
    }

    public static UUID getUUID(String username) {
        ProxiedPlayer p = getProxyServer().getPlayer(username);
        if (p != null) return p.getUniqueId();
        return mongoHandler.usernameToUUID(username);
    }


    public void setupShowReminder() {
        LocalDateTime localNow = LocalDateTime.now();
        ZoneId currentZone = ZoneId.of("America/New_York");
        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
        ZonedDateTime zonedNext10 = zonedNow.withHour(9).withMinute(20).withSecond(0);
        ZonedDateTime zonedNext10_2 = zonedNow.withHour(9).withMinute(30).withSecond(0);
        ZonedDateTime zonedNext10_3 = zonedNow.withHour(9).withMinute(40).withSecond(0);
        ZonedDateTime zonedNext14 = zonedNow.withHour(13).withMinute(20).withSecond(0);
        ZonedDateTime zonedNext14_2 = zonedNow.withHour(13).withMinute(30).withSecond(0);
        ZonedDateTime zonedNext14_3 = zonedNow.withHour(13).withMinute(40).withSecond(0);
        ZonedDateTime zonedNext18 = zonedNow.withHour(17).withMinute(20).withSecond(0);
        ZonedDateTime zonedNext18_2 = zonedNow.withHour(17).withMinute(30).withSecond(0);
        ZonedDateTime zonedNext18_3 = zonedNow.withHour(17).withMinute(40).withSecond(0);
        ZonedDateTime zonedNext22 = zonedNow.withHour(21).withMinute(20).withSecond(0);
        ZonedDateTime zonedNext22_2 = zonedNow.withHour(21).withMinute(30).withSecond(0);
        ZonedDateTime zonedNext22_3 = zonedNow.withHour(21).withMinute(40).withSecond(0);

        if (zonedNow.compareTo(zonedNext10) > 0) zonedNext10 = zonedNext10.plusDays(1);
        if (zonedNow.compareTo(zonedNext10_2) > 0) zonedNext10_2 = zonedNext10_2.plusDays(1);
        if (zonedNow.compareTo(zonedNext10_3) > 0) zonedNext10_3 = zonedNext10_3.plusDays(1);
        if (zonedNow.compareTo(zonedNext14) > 0) zonedNext14 = zonedNext14.plusDays(1);
        if (zonedNow.compareTo(zonedNext14_2) > 0) zonedNext14_2 = zonedNext14_2.plusDays(1);
        if (zonedNow.compareTo(zonedNext14_3) > 0) zonedNext14_3 = zonedNext14_3.plusDays(1);
        if (zonedNow.compareTo(zonedNext18) > 0) zonedNext18 = zonedNext18.plusDays(1);
        if (zonedNow.compareTo(zonedNext18_2) > 0) zonedNext18_2 = zonedNext18_2.plusDays(1);
        if (zonedNow.compareTo(zonedNext18_3) > 0) zonedNext18_3 = zonedNext18_3.plusDays(1);
        if (zonedNow.compareTo(zonedNext22) > 0) zonedNext22 = zonedNext22.plusDays(1);
        if (zonedNow.compareTo(zonedNext22_2) > 0) zonedNext22_2 = zonedNext22_2.plusDays(1);
        if (zonedNow.compareTo(zonedNext22_3) > 0) zonedNext22_3 = zonedNext22_3.plusDays(1);

        long d1 = Duration.between(zonedNow, zonedNext10).getSeconds();
        long d2 = Duration.between(zonedNow, zonedNext10_2).getSeconds();
        long d3 = Duration.between(zonedNow, zonedNext10_3).getSeconds();
        long d4 = Duration.between(zonedNow, zonedNext14).getSeconds();
        long d5 = Duration.between(zonedNow, zonedNext14_2).getSeconds();
        long d6 = Duration.between(zonedNow, zonedNext14_3).getSeconds();
        long d7 = Duration.between(zonedNow, zonedNext18).getSeconds();
        long d8 = Duration.between(zonedNow, zonedNext18_2).getSeconds();
        long d9 = Duration.between(zonedNow, zonedNext18_3).getSeconds();
        long d10 = Duration.between(zonedNow, zonedNext22).getSeconds();
        long d11 = Duration.between(zonedNow, zonedNext22_2).getSeconds();
        long d12 = Duration.between(zonedNow, zonedNext22_3).getSeconds();

        ScheduledExecutorService sch = Executors.newScheduledThreadPool(1);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 10am Show in 40 minutes!"), d1,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 10am Show in 30 minutes!"), d2,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 10am Show in 20 minutes!"), d3,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 2pm Show in 40 minutes!"), d4,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 2pm Show in 30 minutes!"), d5,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 2pm Show in 20 minutes!"), d6,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 6pm Show in 40 minutes!"), d7,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 6pm Show in 30 minutes!"), d8,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 6pm Show in 20 minutes!"), d9,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 10pm Show in 40 minutes!"), d10,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 10pm Show in 30 minutes!"), d11,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 10pm Show in 20 minutes!"), d12,
                24 * 60 * 60, TimeUnit.SECONDS);
    }
}
