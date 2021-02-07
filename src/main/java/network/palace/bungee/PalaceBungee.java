package network.palace.bungee;

import lombok.Getter;
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
import network.palace.bungee.dashboard.DashboardConnection;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.listeners.PlayerChat;
import network.palace.bungee.listeners.PlayerJoinAndLeave;
import network.palace.bungee.listeners.ProxyPing;
import network.palace.bungee.listeners.ServerSwitch;
import network.palace.bungee.messages.MessageHandler;
import network.palace.bungee.mongo.MongoHandler;
import network.palace.bungee.utils.*;
import network.palace.bungee.utils.chat.JaroWinkler;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

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

    //TODO move to environment variable
    @Getter private final static boolean testNetwork = true;
    @Getter private static DashboardConnection dashboardConnection;

    @Override
    public void onEnable() {
        instance = this;

        configUtil = new ConfigUtil();

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

        registerListeners();
        registerCommands();

        try {
            dashboardConnection = new DashboardConnection();
        } catch (URISyntaxException | NoSuchAlgorithmException | KeyManagementException | InterruptedException e) {
            e.printStackTrace();
            getLogger().severe("Error connecting to Dashboard!");
        }
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
        pm.registerCommand(this, new MotionCaptureCommand());
        pm.registerCommand(this, new MultiShowCommand());
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
    }

    public static void logout(UUID uuid, Player player) {
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
}
