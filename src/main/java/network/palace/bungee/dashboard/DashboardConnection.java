package network.palace.bungee.dashboard;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.protocol.packet.TabCompleteResponse;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.dashboard.packets.BasePacket;
import network.palace.bungee.dashboard.packets.bungee.PacketComponentMessage;
import network.palace.bungee.dashboard.packets.bungee.PacketPlayerListInfo;
import network.palace.bungee.dashboard.packets.bungee.PacketUpdateBungeeConfig;
import network.palace.bungee.dashboard.packets.dashboard.*;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.ProtocolConstants;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.RankTag;
import network.palace.bungee.utils.DateUtil;
import org.java_websocket.client.DefaultSSLWebSocketClientFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * Created by Marc on 5/22/16
 */
@SuppressWarnings("DuplicateBranchesInSwitch")
public class DashboardConnection {
    protected WebSocketClient ws;
    private final String dashboardURL;
    private boolean offline = true;

    public DashboardConnection() throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, InterruptedException {
        dashboardURL = PalaceBungee.getConfigUtil().getDashboardURL();
        start();
    }

    private void start() throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, InterruptedException {
        ws = new WebSocketClient(new URI(dashboardURL), new Draft_10()) {
            @Override
            public void onMessage(String message) {
                JsonObject object = (JsonObject) new JsonParser().parse(message);
                if (!object.has("id")) {
                    return;
                }
                PalaceBungee.getProxyServer().getLogger().info("[DEBUG] Incoming from Dashboard: " + object.toString());
                int id = object.get("id").getAsInt();
//                System.out.println(object.toString());
                switch (id) {
                    case 24: {
                        PacketPlayerDisconnect packet = new PacketPlayerDisconnect().fromJSON(object);
                        UUID uuid = packet.getUniqueId();
                        String reason = packet.getReason();
                        ProxiedPlayer pl = PalaceBungee.getProxyServer().getPlayer(uuid);
                        if (pl != null) {
                            pl.disconnect(TextComponent.fromLegacyText(reason));
                        }
                        break;
                    }
                    case 25: {
                        PacketPlayerChat packet = new PacketPlayerChat().fromJSON(object);
                        UUID uuid = packet.getUniqueId();
                        String msg = packet.getMessage();
                        Player tp = PalaceBungee.getPlayer(uuid);
                        if (tp == null) return;
                        tp.chat(msg);
                        break;
                    }
                    case 26: {
                        PacketMessage packet = new PacketMessage().fromJSON(object);
                        UUID uuid = packet.getUniqueId();
                        Player p = PalaceBungee.getPlayer(uuid);
                        if (p == null) return;
                        String msg = packet.getMessage();
                        p.sendMessage(TextComponent.fromLegacyText(msg));
                        break;
                    }
                    case 28: {
                        PacketPlayerRank packet = new PacketPlayerRank().fromJSON(object);
                        UUID uuid = packet.getUniqueId();
                        Player player = PalaceBungee.getPlayer(uuid);
                        if (player == null) return;
                        player.setRank(packet.getRank());
                        for (String tag : packet.getTags()) {
                            player.addTag(RankTag.fromString(tag));
                        }
                        break;
                    }
                    case 29: {
//                        PacketStartReboot packet = new PacketStartReboot().fromJSON(object);
//                        PlayerJoinAndLeave.setRebooting(true);
//                        PalaceBungee.getProxyServer().getScheduler().schedule(PalaceBungee.getInstance(),
//                                () -> PalaceBungee.getProxyServer().stop(), 10, TimeUnit.SECONDS);
                        break;
                    }
                    case 32: {
                        PacketSendToServer packet = new PacketSendToServer().fromJSON(object);
                        UUID uuid = packet.getUniqueId();
                        String server = packet.getServer();
                        ProxiedPlayer tp = PalaceBungee.getProxyServer().getPlayer(uuid);
                        if (tp == null) {
                            return;
                        }
                        try {
                            tp.connect(PalaceBungee.getProxyServer().getServerInfo(server));
                        } catch (Exception ignored) {
                        }
                        break;
                    }
                    case 33: {
//                        PacketUpdateMOTD packet = new PacketUpdateMOTD().fromJSON(object);
//                        String motd = packet.getMOTD();
//                        String maintenance = packet.getMaintenance();
//                        List<String> info = packet.getInfo();
//                        PalaceBungee.setMOTD(motd);
//                        PalaceBungee.setMOTDMaintenance(maintenance);
//                        PalaceBungee.setInfo(info);
                        break;
                    }
                    case 35: {
//                        PacketServerList packet = new PacketServerList().fromJSON(object);
//                        List<String> servers = packet.getServers();
//                        BungeeCord bungee = ((BungeeCord) ProxyServer.getInstance());
//                        for (String server : servers) {
//                            try {
//                                String[] list = server.split(";");
//                                String[] addressList = list[1].split(":");
//                                ServerInfo info = ProxyServer.getInstance().constructServerInfo(list[0],
//                                        new InetSocketAddress(addressList[0], Integer.parseInt(addressList[1])), "", false);
//                                System.out.println(server + "=" + list[0] + " " + addressList[0] + " " + addressList[1]);
//                                bungee.getServers().put(info.getName(), info);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
                        break;
                    }
                    case 36: {
                        PacketRemoveServer packet = new PacketRemoveServer().fromJSON(object);
                        String name = packet.getName();
                        ProxyServer.getInstance().getServers().remove(name);
                        break;
                    }
                    case 37: {
                        PacketAddServer packet = new PacketAddServer().fromJSON(object);
                        String name = packet.getName();
                        String[] addressList = packet.getAddress().split(":");
                        String host = addressList[0];
                        int port = Integer.parseInt(addressList[1]);
                        ServerInfo server = ProxyServer.getInstance().constructServerInfo(name,
                                new InetSocketAddress(host, port), "", false);
                        ProxyServer.getInstance().getServers().put(name, server);
                        break;
                    }
                    case 38: {
//                        PacketTargetLobby packet = new PacketTargetLobby().fromJSON(object);
//                        String server = packet.getServer();
//                        PalaceBungee.setTargetLobby(PalaceBungee.getProxyServer().getServerInfo(server));
                        break;
                    }
                    case 40: {
                        PacketUptimeCommand packet = new PacketUptimeCommand().fromJSON(object);
                        UUID uuid = packet.getUniqueId();
                        long time = packet.getTime();
                        Player tp = PalaceBungee.getPlayer(uuid);
                        if (tp == null) {
                            return;
                        }
                        tp.sendMessage(new ComponentBuilder("Dashboard has been online for " +
                                DateUtil.formatDateDiff(time) + "\nThis BungeeCord has been online for " +
                                DateUtil.formatDateDiff(PalaceBungee.getStartTime())).color(ChatColor.GREEN).create());
                        break;
                    }
                    case 41: {
//                        PacketOnlineCount packet = new PacketOnlineCount().fromJSON(object);
//                        int count = packet.getCount();
//                        PalaceBungee.setOnlineCount(count);
                        break;
                    }
                    case 43: {
                        PacketTabComplete packet = new PacketTabComplete().fromJSON(object);
                        UUID uuid = packet.getUniqueId();
                        List<String> results = packet.getResults();
                        ProxiedPlayer player = PalaceBungee.getProxyServer().getPlayer(uuid);
                        Player tp = PalaceBungee.getPlayer(uuid);

                        if (player == null || tp == null) {
                            return;
                        }

                        if (results.isEmpty()) {
                            return;
                        }

                        if (tp.getProtocolVersion() >= ProtocolConstants.MINECRAFT_1_13.getProtocolId()) {
                            int start = 0;
                            int length = 0;

                            if (!results.isEmpty()) {
                                StringBuilder input = new StringBuilder("/" + packet.getCommand() + " ");
                                for (int i = 0; i < packet.getArgs().size(); i++) {
                                    input.append(packet.getArgs().get(i));
                                    if (i < packet.getArgs().size() - 1) {
                                        input.append(" ");
                                    }
                                }

                                if (input.toString().endsWith(" ") || input.length() == 0) {
                                    start = input.length();
                                    length = 0;
                                } else {
                                    int lastSpace = input.lastIndexOf(" ") + 1;
                                    start = lastSpace;
                                    length = input.length() - lastSpace;
                                }
                            }
                            StringRange range = StringRange.between(start, start + length);

                            List<Suggestion> suggestionList = new ArrayList<>();
                            results.forEach(s -> suggestionList.add(new Suggestion(range, s)));
                            Suggestions suggestions = Suggestions.create(packet.getCommand(), suggestionList);

                            player.unsafe().sendPacket(new TabCompleteResponse(packet.getTransactionId(), suggestions));
                        } else {
                            player.unsafe().sendPacket(new TabCompleteResponse(results));
                        }
                        break;
                    }
                    case 44: {
//                        PacketCommandList packet = new PacketCommandList().fromJSON(object);
//                        PalaceBungee.getCommandUtil().processPacket(packet);
                        break;
                    }
                    case 46: {
//                        PacketMaintenance packet = new PacketMaintenance().fromJSON(object);
//                        boolean maintenance = packet.isMaintenance();
//                        PalaceBungee.setMaintenance(maintenance);
                        break;
                    }
                    case 47: {
//                        PacketMaintenanceWhitelist packet = new PacketMaintenanceWhitelist().fromJSON(object);
//                        List<UUID> list = packet.getAllowed();
//                        PalaceBungee.setMaintenanceWhitelist(list);
                        break;
                    }
                    case 53: {
                        PacketLink packet = new PacketLink().fromJSON(object);
                        UUID uuid = packet.getUniqueId();
                        String url = packet.getUrl();
                        String name = packet.getName();
                        ChatColor color = packet.getColor();
                        boolean bold = packet.isBold();
                        boolean spacing = packet.isSpacing();
                        Player tp = PalaceBungee.getPlayer(uuid);
                        if (tp == null) {
                            return;
                        }
                        String s = spacing ? "\n" : "";
                        tp.sendMessage(new ComponentBuilder(s + name + s).color(color).bold(bold)
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to visit " +
                                        url).color(ChatColor.GREEN).create())).event(new ClickEvent(ClickEvent.Action.OPEN_URL,
                                        url)).create());
                        break;
                    }
                    case 56: {
                        PacketWarning packet = new PacketWarning().fromJSON(object);
                        UUID warnid = packet.getWarningID();
                        String username = packet.getUsername();
                        String msg = packet.getMessage();
                        String action = packet.getAction();
                        BaseComponent[] comp = new ComponentBuilder(username).color(ChatColor.AQUA)
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ":warn-" + warnid))
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        new ComponentBuilder("Click to warn this player").color(ChatColor.GREEN)
                                                .create())).append(" " + action + ": ", ComponentBuilder.FormatRetention.ALL)
                                .color(ChatColor.RED).append(msg, ComponentBuilder.FormatRetention.ALL)
                                .color(ChatColor.AQUA).create();
                        for (Player tp : PalaceBungee.getOnlinePlayers()) {
                            if (tp.getRank().getRankId() >= Rank.TRAINEE.getRankId()) {
                                tp.sendMessage(comp);
                            }
                        }
                        break;
                    }
                    case 60: {
                        PacketTitle packet = new PacketTitle().fromJSON(object);
                        UUID uuid = packet.getUniqueId();
                        String title = packet.getTitle();
                        String subtitle = packet.getSubtitle();
                        int fadeIn = packet.getFadeIn();
                        int stay = packet.getStay();
                        int fadeOut = packet.getFadeOut();
                        ProxiedPlayer tp = PalaceBungee.getProxyServer().getPlayer(uuid);
                        if (tp == null) {
                            return;
                        }
                        Title t = PalaceBungee.getProxyServer().createTitle();
                        t.title(TextComponent.fromLegacyText(title));
                        t.subTitle(TextComponent.fromLegacyText(subtitle));
                        t.fadeIn(fadeIn);
                        t.stay(stay);
                        t.fadeOut(fadeOut);
                        t.send(tp);
                        break;
                    }
                    case 65: {
//                        PacketBungeeID packet = new PacketBungeeID().fromJSON(object);
//                        UUID bid = packet.getBungeeID();
//                        if (PalaceBungee.getBungeeID() != null) {
//                            PacketBungeeID newpacket = new PacketBungeeID(PalaceBungee.getBungeeID());
//                            send(newpacket.getJSON().toString());
//                        }
//                        PalaceBungee.setBungeeID(bid);
                        break;
                    }
                    case 70: {
//                        PacketServerIcon packet = new PacketServerIcon().fromJSON(object);
//                        try {
//                            String base64 = packet.getBase64();
//                            byte[] array = Base64.getDecoder().decode(base64);
//                            InputStream in = new ByteArrayInputStream(array);
//                            BufferedImage bImageFromConvert = ImageIO.read(in);
//                            ImageIO.write(bImageFromConvert, "png", new File("server-icon.png"));
//                            PalaceBungee.setServerIcon(Favicon.create(bImageFromConvert));
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                        break;
                    }
                    case 74: {
                        PacketUpdateBungeeConfig packet = new PacketUpdateBungeeConfig().fromJSON(object);
                        try {
                            PalaceBungee.getConfigUtil().reload();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    case 76: {
                        PacketComponentMessage packet = new PacketComponentMessage().fromJSON(object);
                        BaseComponent[] comp = ComponentSerializer.parse(packet.getSerializedMessage());
                        packet.getTargets().forEach(uuid -> {
                            Player p = PalaceBungee.getPlayer(uuid);
                            if (p != null) p.sendMessage(comp);
                        });
                        break;
                    }
                }
            }

            @Override
            public void onOpen(ServerHandshake handshake) {
                System.out.println("Successfully connected to Dashboard");
                DashboardConnection.this.send(new PacketConnectionType(PacketConnectionType.ConnectionType.BUNGEECORD).getJSON().toString());
//                DashboardConnection.this.send(new PacketServerName(PalaceBungee.getBungeeName()));
                if (PalaceBungee.getOnlinePlayers().size() > 0) {
                    List<PacketPlayerListInfo.Player> list = new ArrayList<>();
                    for (ProxiedPlayer tp : PalaceBungee.getProxyServer().getPlayers()) {
                        Player p = PalaceBungee.getPlayer(tp.getUniqueId());
                        StringBuilder tags = new StringBuilder();
                        List<RankTag> tagList = p.getTags();
                        for (int i = 0; i < tagList.size(); i++) {
                            tags.append(tagList.get(i).getDBName());
                            if (i < (tagList.size() - 1)) {
                                tags.append(",");
                            }
                        }
                        PacketPlayerListInfo.Player pl = new PacketPlayerListInfo.Player(p.getUniqueId(), p.getUsername(),
                                p.getAddress(), p.getServerName(), p.getRank().getDBName(), tags.toString(), tp.getPendingConnection().getVersion());
                        list.add(pl);
                    }
                    PacketPlayerListInfo packet = new PacketPlayerListInfo(list);
                    DashboardConnection.this.send(packet);
                }
                if (offline) {
                    offline = false;
                    for (Player player : PalaceBungee.getOnlinePlayers()) {
                        player.sendMessage(ChatColor.GREEN + "Connected to chat servers!");
                    }
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println(code + " Disconnected from Dashboard! Reconnecting...");
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            start();
                        } catch (URISyntaxException | InterruptedException | KeyManagementException | NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
                    }
                }, 5000L);
                if (!offline) {
                    offline = true;
                    for (Player player : PalaceBungee.getOnlinePlayers()) {
                        player.sendMessage(ChatColor.RED + "Disconnected from chat servers, chat is temporarily disabled...");
                    }
                }
            }

            @Override
            public void onError(Exception ex) {
                System.out.println("Error in Dashboard connection");
                ex.printStackTrace();
            }

        };
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }
        }};
        if (dashboardURL.toLowerCase().startsWith("wss")) {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            ws.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(sc));
            ws.connectBlocking();
        } else {
            ws.connect();
        }
    }

    public void send(String s) {
        if (isDisconnected()) {
            PalaceBungee.getProxyServer().getLogger().severe("WebSocket disconnected, cannot send packet!");
            return;
        }
        try {
            PalaceBungee.getProxyServer().getLogger().info("[DEBUG] Outgoing to Dashboard: " + s);
            ws.send(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isDisconnected() {
        return ws == null || ws.getConnection() == null || ws.getConnection().isConnecting() || !ws.getConnection().isOpen();
    }

    public void send(BasePacket packet) {
        send(packet.getJSON().toString());
    }

    public void playerChat(UUID uuid, String message) {
        PacketPlayerChat packet = new PacketPlayerChat(uuid, message);
        send(packet);
    }

    public String getServerIconBase64() {
        String encodedfile = "";
        File serverIcon = new File("server-icon.png");
        if (!serverIcon.exists()) {
            return "";
        }
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(serverIcon);
            byte[] bytes = new byte[(int) serverIcon.length()];
            fileInputStreamReader.read(bytes);
            encodedfile = new String(Base64.getEncoder().encode(bytes), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return encodedfile;
    }
}