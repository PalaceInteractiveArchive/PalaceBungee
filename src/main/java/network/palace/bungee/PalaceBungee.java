package network.palace.bungee;

import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import network.palace.bungee.commands.*;
import network.palace.bungee.commands.admin.GuideLogCommand;
import network.palace.bungee.commands.admin.ProxyReloadCommand;
import network.palace.bungee.commands.chat.*;
import network.palace.bungee.commands.guide.GuideListCommand;
import network.palace.bungee.commands.moderation.AltAccountsCommand;
import network.palace.bungee.commands.moderation.LookupCommand;
import network.palace.bungee.commands.moderation.NamecheckCommand;
import network.palace.bungee.commands.staff.BroadcastCommand;
import network.palace.bungee.commands.staff.SGListCommand;
import network.palace.bungee.commands.staff.ServerCommand;
import network.palace.bungee.commands.staff.StaffListCommand;
import network.palace.bungee.dashboard.DashboardConnection;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.ProtocolConstants;
import network.palace.bungee.listeners.PlayerChat;
import network.palace.bungee.listeners.PlayerJoinAndLeave;
import network.palace.bungee.listeners.ProxyPing;
import network.palace.bungee.messages.MessageHandler;
import network.palace.bungee.mongo.MongoHandler;
import network.palace.bungee.party.PartyUtil;
import network.palace.bungee.utils.ChatUtil;
import network.palace.bungee.utils.ConfigUtil;
import network.palace.bungee.utils.ServerUtil;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class PalaceBungee extends Plugin {
    @Getter private static final UUID proxyID = UUID.randomUUID();
    @Getter private static PalaceBungee instance;
    @Getter private static ConfigUtil configUtil;
    @Getter private static ServerUtil serverUtil;

    @Getter private static ChatUtil chatUtil;
    @Getter private static PartyUtil partyUtil;

    @Getter private static MongoHandler mongoHandler;
    @Getter private static MessageHandler messageHandler;

    @Getter private static final long startTime = System.currentTimeMillis();
    private final static HashMap<UUID, Player> players = new HashMap<>();

    @Getter private final static HashMap<UUID, String> usernameCache = new HashMap<>();

    @Getter private final static boolean testNetwork = true;
    @Getter private static DashboardConnection dashboardConnection;

    @Override
    public void onEnable() {
        instance = this;

        ProtocolConstants.setHighVersion(753, "1.16.3");
        ProtocolConstants.setLowVersion(573, "1.15");

        configUtil = new ConfigUtil();

        try {
            mongoHandler = new MongoHandler();
            PalaceBungee.getConfigUtil().reload();
        } catch (IOException e) {
            e.printStackTrace();
        }

        serverUtil = new ServerUtil();

        try {
            messageHandler = new MessageHandler();
            messageHandler.initialize();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

        chatUtil = new ChatUtil();
        partyUtil = new PartyUtil();

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
        pm.registerListener(this, new ProxyPing());
        pm.registerListener(this, new PlayerJoinAndLeave());
    }

    private void registerCommands() {
        PluginManager pm = getProxy().getPluginManager();
        /* Admin Commands */
        pm.registerCommand(this, new GuideLogCommand());
        pm.registerCommand(this, new ProxyReloadCommand());
        /* Chat Commands */
        pm.registerCommand(this, new AdminChatCommand());
        pm.registerCommand(this, new ChatDelayCommand());
        pm.registerCommand(this, new ClearChatCommand());
        pm.registerCommand(this, new GuideChatCommand());
        pm.registerCommand(this, new PartyChatCommand());
        pm.registerCommand(this, new StaffChatCommand());
        /* Guide Commands */
        pm.registerCommand(this, new GuideListCommand());
        /* Moderation Commands */
        pm.registerCommand(this, new AltAccountsCommand());
        pm.registerCommand(this, new LookupCommand());
        pm.registerCommand(this, new NamecheckCommand());
        /* Staff Commands */
        pm.registerCommand(this, new BroadcastCommand());
        pm.registerCommand(this, new ServerCommand());
        pm.registerCommand(this, new SGListCommand());
        pm.registerCommand(this, new StaffListCommand());
        /* General Commands */
        pm.registerCommand(this, new ApplyCommand());
        pm.registerCommand(this, new BugCommand());
        pm.registerCommand(this, new MentionsCommand());
        pm.registerCommand(this, new MsgCommand());
        pm.registerCommand(this, new OnlineCountCommand());
        pm.registerCommand(this, new PartyCommand());
        pm.registerCommand(this, new PunishmentsCommand());
        pm.registerCommand(this, new ReplyCommand());
        pm.registerCommand(this, new RulesCommand());
        pm.registerCommand(this, new SocialCommand());
        pm.registerCommand(this, new StoreCommand());
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
        mongoHandler.login(player.getUniqueId());
    }

    public static void logout(UUID uuid) {
        players.remove(uuid);
        mongoHandler.logout(uuid);
    }

    public static Collection<Player> getOnlinePlayers() {
        return players.values();
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
