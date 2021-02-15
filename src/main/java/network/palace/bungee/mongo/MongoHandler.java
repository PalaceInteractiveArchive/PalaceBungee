package network.palace.bungee.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.Favicon;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.*;
import network.palace.bungee.handlers.moderation.*;
import network.palace.bungee.messages.packets.FriendJoinPacket;
import network.palace.bungee.utils.ConfigUtil;
import network.palace.bungee.utils.NameUtil;
import org.bson.BsonInt32;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;

@SuppressWarnings({"rawtypes", "unchecked"})
public class MongoHandler {
    private final MongoClient client;
    private final MongoCollection<Document> bansCollection;
    private final MongoCollection<Document> partyCollection;
    @Getter private final MongoCollection<Document> playerCollection;
    @Getter private final MongoCollection<Document> chatCollection;
    @Getter private final MongoCollection<Document> resourcePackCollection;
    private final MongoCollection<Document> serversCollection;
    private final MongoCollection<Document> serviceConfigCollection;
    private final MongoCollection<Document> helpRequestsCollection;
    private final MongoCollection<Document> announcementRequestsCollection;
    private final MongoCollection<Document> friendsCollection;
    private final MongoCollection<Document> staffLoginCollection;
    private final MongoCollection<Document> virtualQueuesCollection;

    public MongoHandler() throws IOException {
        ConfigUtil.DatabaseConnection mongo = PalaceBungee.getConfigUtil().getMongoDBInfo();
        String hostname = mongo.getHost();
        String username = mongo.getUsername();
        String password = mongo.getPassword();
        MongoClientURI connectionString = new MongoClientURI("mongodb://" + username + ":" + password + "@" + hostname);
        client = new MongoClient(connectionString);
        MongoDatabase database = client.getDatabase(mongo.getDatabase());
        playerCollection = database.getCollection("players");
        chatCollection = database.getCollection("chat");
        bansCollection = database.getCollection("bans");
        partyCollection = database.getCollection("parties");
        resourcePackCollection = database.getCollection("resourcepacks");
        serversCollection = database.getCollection("servers");
        serviceConfigCollection = database.getCollection("service_configs");
        helpRequestsCollection = database.getCollection("help_requests");
        announcementRequestsCollection = database.getCollection("announcement_requests");
        friendsCollection = database.getCollection("friends");
        staffLoginCollection = database.getCollection("stafflogin");
        virtualQueuesCollection = database.getCollection("virtual_queues");
    }

    public void stop() {
        client.close();
    }

    /**
     * Get a specific set of a player's data from the database
     *
     * @param uuid  the uuid
     * @param limit a Document specifying which keys to return from the database
     * @return a Document with the limited data
     */
    public Document getPlayer(UUID uuid, Document limit) {
        FindIterable<Document> doc = playerCollection.find(Filters.eq("uuid", uuid.toString())).projection(limit);
        if (doc == null) return null;
        return doc.first();
    }

    /**
     * Get a specific set of a player's data from the database
     *
     * @param username the username
     * @param limit    a Document specifying which keys to return from the database
     * @return a Document with the limited data
     */
    public Document getPlayer(String username, Document limit) {
        FindIterable<Document> doc = playerCollection.find(Filters.eq("username", username)).projection(limit);
        if (doc == null) return null;
        return doc.first();
    }

    /**
     * Set previous usernames for a player
     *
     * @param uuid the uuid
     * @param list the list of previous usernames
     */
    public void setPreviousNames(UUID uuid, List<String> list) {
        playerCollection.updateOne(Filters.eq("uuid", uuid.toString()), Updates.set("previousNames", list));
    }

    public void updatePreviousUsernames(UUID uuid, String username) {
        PalaceBungee.getProxyServer().getScheduler().runAsync(PalaceBungee.getInstance(), () -> {
            String current = "";
            try {
                List<String> list = NameUtil.getNames(username, uuid.toString().replaceAll("-", ""));
                Collections.reverse(list);
                current = list.get(0);
                setPreviousNames(uuid, list.subList(1, list.size()));
            } catch (Exception e) {
                PalaceBungee.getProxyServer().getLogger().log(Level.SEVERE, "Error retrieving previous usernames", e);
            }
            if (!username.isEmpty() && !current.equals(username)) {
                playerCollection.updateOne(Filters.eq("uuid", uuid.toString()), Updates.set("username", current));
            }
        });
    }

    private void checkForOldDuplicateUsernames(String username) throws Exception {
        FindIterable<Document> sameName = playerCollection.find(Filters.eq("username", username)).projection(new Document("_id", true).append("uuid", true).append("username", true));
        for (Document userWithSameName : sameName) {
            try {
                PalaceBungee.getProxyServer().getLogger().warning("Found a duplicate! " + userWithSameName.getString("uuid") + "|" + userWithSameName.getString("username"));
                List<String> previousUsernames = NameUtil.getNames("", userWithSameName.getString("uuid"));
                Collections.reverse(previousUsernames);
                playerCollection.updateOne(Filters.eq("_id", userWithSameName.getObjectId("_id")), Updates.set("username", previousUsernames.get(0)));
                playerCollection.updateOne(Filters.eq("_id", userWithSameName.getObjectId("_id")), Updates.set("previousNames", previousUsernames.subList(1, previousUsernames.size())));
                PalaceBungee.getProxyServer().getLogger().warning("Updated duplicate to " + userWithSameName.getString("uuid") + "|" + previousUsernames.get(0));
            } catch (Exception e) {
                PalaceBungee.getProxyServer().getLogger().severe("Failed to check username history for " + userWithSameName.getString("uuid"));
            }
        }
    }

    public boolean isPlayerInDB(UUID uuid) {
        return getPlayer(uuid, new Document("uuid", 1)) != null;
    }

    public boolean isPlayerOnline(UUID uuid) {
        return playerCollection.find(Filters.and(Filters.eq("uuid", uuid.toString()), Filters.eq("online", true))).first() != null;
    }

    public Document getSettings(UUID uuid) {
        return getPlayer(uuid, new Document("settings", 1));
    }

    public void setSetting(UUID uuid, String key, Object value) {
        playerCollection.updateOne(Filters.eq("uuid", uuid.toString()), Updates.set("settings." + key, value),
                new UpdateOptions().upsert(true));
    }

    public void updateAddress(UUID uuid, String address) {
        playerCollection.updateOne(Filters.eq("uuid", uuid.toString()), Updates.set("ip", address));
    }

    public int getOnlineCount() {
        return (int) playerCollection.count(Filters.eq("online", true));
    }

    public void createPlayer(Player player) {
        player.setNewGuest(true);
        player.setOnlineTime(1);

        try {
            PalaceBungee.getProxyServer().getLogger().warning("New user! Checking for existing duplicate usernames...");
            checkForOldDuplicateUsernames(player.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
            PalaceBungee.getProxyServer().getLogger().severe("Error checking for duplicate username for " + player.getUsername() + "|" + player.getUniqueId());
        }

        Document playerDocument = new Document();
        playerDocument.put("uuid", player.getUniqueId().toString());
        playerDocument.put("username", player.getUsername());
        playerDocument.put("previousNames", new ArrayList<>());
        playerDocument.put("balance", 250);
        playerDocument.put("tokens", 0);
        playerDocument.put("server", player.getServerName().isEmpty() ? "Unknown" : player.getServerName());
        playerDocument.put("isp", "");
        playerDocument.put("country", "");
        playerDocument.put("region", "");
        playerDocument.put("regionName", "");
        playerDocument.put("timezone", "");
        playerDocument.put("lang", "en_US");
        playerDocument.put("minecraftVersion", player.getMcVersion());
        playerDocument.put("honor", 1);
        playerDocument.put("ip", player.getAddress());
        playerDocument.put("rank", player.getRank().getDBName());
        playerDocument.put("lastOnline", System.currentTimeMillis());
        playerDocument.put("onlineTime", 1L);

        Map<String, String> skinData = new HashMap<>();
        skinData.put("hash", "");
        skinData.put("signature", "");
        playerDocument.put("skin", skinData);

        List<Integer> cosmeticData = new ArrayList<>();
        playerDocument.put("cosmetics", cosmeticData);

        List<Object> kicks = new ArrayList<>();
        List<Object> mutes = new ArrayList<>();
        List<Object> bans = new ArrayList<>();
        playerDocument.put("kicks", kicks);
        playerDocument.put("mutes", mutes);
        playerDocument.put("bans", bans);

        Map<String, Object> parkData = new HashMap<>();
        List<Object> storageData = new ArrayList<>();
        parkData.put("storage", storageData);

        Map<String, String> magicBandData = new HashMap<>();
        magicBandData.put("bandtype", "blue");
        magicBandData.put("namecolor", "orange");
        parkData.put("magicband", magicBandData);

        Map<String, Integer> fpData = new HashMap<>();
        fpData.put("slow", 0);
        fpData.put("moderate", 0);
        fpData.put("thrill", 0);
        fpData.put("sday", 0);
        fpData.put("mday", 0);
        fpData.put("tday", 0);
        parkData.put("fastpass", fpData);

        List<Object> rideData = new ArrayList<>();
        parkData.put("rides", rideData);

        parkData.put("outfit", "0,0,0,0");
        List<Object> outfitData = new ArrayList<>();
        parkData.put("outfitPurchases", outfitData);

        Map<String, Object> parkSettings = new HashMap<>();
        parkSettings.put("visibility", true);
        parkSettings.put("flash", true);
        parkSettings.put("hotel", true);
        parkSettings.put("pack", "");
        parkData.put("settings", parkSettings);

        playerDocument.put("parks", parkData);

        Map<String, Object> creativeData = new HashMap<>();
        creativeData.put("particle", "none");
        creativeData.put("rptag", false);
        creativeData.put("rplimit", 5);
        creativeData.put("showcreator", false);
        creativeData.put("creator", false);
        creativeData.put("creatortag", false);
        creativeData.put("resourcepack", "none");

        playerDocument.put("creative", creativeData);

        Map<String, Object> voteData = new HashMap<>();
        voteData.put("lastTime", 0L);
        voteData.put("lastSite", 0);
        playerDocument.put("vote", voteData);

        Map<String, Long> monthlyRewards = new HashMap<>();
        monthlyRewards.put("settler", 0L);
        playerDocument.put("monthlyRewards", monthlyRewards);

        playerDocument.put("tutorial", false);

        Map<String, Object> settings = new HashMap<>();
        settings.put("mentions", true);
        settings.put("friendRequestToggle", true);
        playerDocument.put("settings", settings);

        List<Object> achievements = new ArrayList<>();
        playerDocument.put("achievements", achievements);

        List<Object> autographs = new ArrayList<>();
        playerDocument.put("autographs", autographs);

        List<Object> transactions = new ArrayList<>();
        playerDocument.put("transactions", transactions);

        List<Object> ignoring = new ArrayList<>();
        playerDocument.put("ignoring", ignoring);

        playerDocument.put("online", true);

        Map<String, Object> onlineData = new HashMap<>();
        onlineData.put("proxy", PalaceBungee.getProxyID().toString());
        onlineData.put("server", "Hub1");
        playerDocument.put("onlineData", onlineData);

        playerCollection.insertOne(playerDocument);

        updatePreviousUsernames(player.getUniqueId(), player.getUsername());
    }

    public void login(Player player) {
        try {
            Document doc = getPlayer(player.getUniqueId(), new Document("ip", 1).append("username", 1)
                    .append("friendRequestToggle", 1).append("onlineTime", 1).append("tutorial", 1)
                    .append("minecraftVersion", 1).append("settings", 1));
            if (doc == null) {
                createPlayer(player);
                return;
            } else {
                playerCollection.updateOne(new Document("uuid", player.getUniqueId().toString()), new Document("$set",
                        new Document("online", true)
                                .append("onlineData", new Document("proxy", PalaceBungee.getProxyID().toString()).append("server", "Hub1"))
                ));
            }
            Rank rank = player.getRank();

            long ot = doc.getLong("onlineTime");
            player.setOnlineTime(ot == 0 ? 1 : ot);

            String ip = doc.getString("ip");
            int protocolVersion = doc.getInteger("minecraftVersion");
            String username = doc.getString("username");

            boolean disable = rank.getRankId() >= Rank.TRAINEE.getRankId() && !player.getAddress().equals(doc.getString("ip"));

            if (!username.equals(player.getUsername())) {
                PalaceBungee.getProxyServer().getLogger().log(Level.WARNING, "Username needs to be updated! Checking for existing duplicates...");
                checkForOldDuplicateUsernames(player.getUsername());
            }

            if (!disable && (!ip.equals(player.getAddress()) || protocolVersion != player.getMcVersion() ||
                    !username.equals(player.getUsername()))) {
                playerCollection.updateOne(Filters.eq("uuid", player.getUniqueId().toString()),
                        new Document("$set", new Document("ip", player.getAddress())
                                .append("username", player.getUsername())
                                .append("minecraftVersion", new BsonInt32(player.getMcVersion()))));
                if (!username.equals(player.getUsername())) {
                    int member_id = getForumMemberId(player.getUniqueId());
                    if (member_id != -1) {
                        PalaceBungee.getForumUtil().updatePlayerName(player.getUniqueId(), member_id, player.getUsername());
                    }
                    updatePreviousUsernames(player.getUniqueId(), player.getUsername());
                }
            }
            Document settings = (Document) doc.get("settings");

            player.setDisabled(disable);
            player.setRank(rank);
            player.setFriendRequestToggle(!settings.getBoolean("friendRequestToggle"));
            player.setMentions(settings.getBoolean("mentions"));
            player.setNewGuest(!doc.getBoolean("tutorial"));
            PalaceBungee.getProxyServer().getLogger().info("Player Join: " + player.getUsername() + "|" + player.getUniqueId());

            List<UUID> ignored = getIgnoredUsers(player.getUniqueId());
            ignored.forEach(uuid -> player.setIgnored(uuid, true));

            HashMap<UUID, String> friends = getFriendList(player.getUniqueId());
            HashMap<UUID, String> requests = getFriendRequestList(player.getUniqueId());
            if (requests.size() > 0) {
                player.sendMessage(ChatColor.AQUA + "You have " + ChatColor.YELLOW + "" + ChatColor.BOLD +
                        requests.size() + " " + ChatColor.AQUA +
                        "pending friend request" + (requests.size() > 1 ? "s" : "") + "! View them with " +
                        ChatColor.YELLOW + ChatColor.BOLD + "/friend requests");
            }
            if (friends.size() > 0) {
                PalaceBungee.getMessageHandler().sendMessage(new FriendJoinPacket(player.getUniqueId(), rank.getTagColor() + player.getUsername(),
                        new ArrayList<>(friends.keySet()), true, rank.getRankId() >= Rank.CHARACTER.getRankId()), PalaceBungee.getMessageHandler().ALL_PROXIES);
            }
            player.setMute(getCurrentMute(player.getUniqueId()));
        } catch (Exception e) {
            PalaceBungee.getProxyServer().getLogger().log(Level.SEVERE, "Error handling player login", e);
        }
    }

    public void logout(UUID uuid, Player player) {
        playerCollection.updateOne(new Document("uuid", uuid.toString()), Updates.set("online", false));
        playerCollection.updateOne(new Document("uuid", uuid.toString()), Updates.unset("onlineData"));

        if (player != null) {
            String server = "Unknown";
            if (player.getServerName() != null) {
                server = player.getServerName();
            }
            playerCollection.updateOne(Filters.eq("uuid", player.getUniqueId().toString()),
                    new Document("$set", new Document("server", server).append("lastOnline", System.currentTimeMillis()))
                            .append("$inc", new Document("onlineTime", (int) ((System.currentTimeMillis() / 1000) -
                                    (player.getLoginTime() / 1000)))));
        }
    }

    public ConfigUtil.BungeeConfig getBungeeConfig() throws Exception {
        Document config = serviceConfigCollection.find(Filters.eq("type", "bungeecord")).first();
        if (config == null) throw new Exception();

        String base64 = config.getString("icon");
        byte[] array = Base64.getDecoder().decode(base64);
        InputStream in = new ByteArrayInputStream(array);
        BufferedImage bImageFromConvert = ImageIO.read(in);
        ImageIO.write(bImageFromConvert, "png", new File("server-icon.png"));
        Favicon icon = Favicon.create(bImageFromConvert);

        ArrayList infoArray = config.get("motdInfo", ArrayList.class);
        String[] motdInfo = new String[infoArray.size()];
        for (int i = 0; i < infoArray.size(); i++) {
            motdInfo[i] = ChatColor.translateAlternateColorCodes('&', (String) infoArray.get(i));
        }

        return new ConfigUtil.BungeeConfig(icon, ChatColor.translateAlternateColorCodes('&', config.getString("motd")),
                motdInfo, ChatColor.translateAlternateColorCodes('&', config.getString("maintenanceMotd")),
                config.getBoolean("maintenance"), config.getInteger("chatDelay"), config.getBoolean("dmEnabled"),
                config.getBoolean("strictChat"), config.getDouble("strictThreshold"), config.getInteger("maxVersion"),
                config.getInteger("minVersion"), config.getString("maxVersionString"),
                config.getString("minVersionString"), config.get("mutedChats", ArrayList.class), config.get("announcements", ArrayList.class));
    }

    public void setBungeeConfig(ConfigUtil.BungeeConfig config) throws Exception {
        serviceConfigCollection.updateOne(Filters.eq("type", "bungeecord"), new Document("$set",
                new Document("maintenance", config.isMaintenance())
                        .append("chatDelay", config.getChatDelay())
                        .append("dmEnabled", config.isDmEnabled())
                        .append("strictChat", config.isStrictChat())
                        .append("strictThreshold", config.getStrictThreshold())
                        .append("mutedChats", config.getMutedChats())
                        .append("announcements", config.getAnnouncements())
        ));
    }

    /**
     * Get the proxyID for the proxy the player is connected to
     *
     * @param username the username
     * @return the proxyID for the proxy the player is connected to, or null if they are offline
     */
    public UUID findPlayer(String username) {
        Document doc = playerCollection.find(Filters.and(Filters.eq("username", username), Filters.eq("online", true))).projection(new Document("onlineData", true).append("uuid", true)).first();
        if (doc == null) return null;
        return UUID.fromString(doc.get("onlineData", Document.class).getString("proxy"));
    }

    /**
     * Get the proxyID for the proxy the player is connected to
     *
     * @param uuid the uuid
     * @return the proxyID for the proxy the player is connected to, or null if they are offline
     */
    public UUID findPlayer(UUID uuid) {
        Document doc = playerCollection.find(Filters.and(Filters.eq("uuid", uuid.toString()), Filters.eq("online", true))).projection(new Document("onlineData", true).append("uuid", true)).first();
        if (doc == null) return null;
        return UUID.fromString(doc.get("onlineData", Document.class).getString("proxy"));
    }

    /**
     * Get the server the player is connected to
     *
     * @param username the username
     * @return the server the player is connected to, or null if they are offline
     */
    public String getPlayerServer(String username) {
        Document doc = playerCollection.find(Filters.and(Filters.eq("username", username), Filters.eq("online", true))).projection(new Document("onlineData", true).append("username", true)).first();
        if (doc == null) return null;
        return doc.get("onlineData", Document.class).getString("server");
    }

    /**
     * Set the server the player is connected to
     *
     * @param uuid the uuid
     * @param name the server name
     */
    public void setPlayerServer(UUID uuid, String name) {
        playerCollection.updateOne(Filters.eq("uuid", uuid.toString()), Updates.set("onlineData.server", name));
    }

    /**
     * Get a player's UUID from their username
     *
     * @param username the username
     * @return the UUID, or null if not found
     */
    public UUID usernameToUUID(String username) {
        Document doc = playerCollection.find(Filters.eq("username", username)).projection(new Document("uuid", 1)).first();
        if (doc == null || !doc.containsKey("uuid")) return null;
        return UUID.fromString(doc.getString("uuid"));
    }

    /**
     * Get a player's username from their UUID
     *
     * @param uuid the uuid
     * @return the username, or null if not found
     */
    public String uuidToUsername(UUID uuid) {
        Document doc = playerCollection.find(Filters.eq("uuid", uuid.toString())).projection(new Document("username", 1)).first();
        if (doc == null || !doc.containsKey("username")) return null;
        return doc.getString("username");
    }

    public String verifyModerationSource(String source) {
        source = source.trim();
        if (source.length() == 36) {
            try {
                UUID sourceUUID = UUID.fromString(source);
                String name = PalaceBungee.getUsernameCache().get(sourceUUID);
                if (name == null) {
                    name = uuidToUsername(sourceUUID);
                    if (name == null) {
                        name = "Unknown";
                    } else {
                        PalaceBungee.getUsernameCache().put(sourceUUID, name);
                    }
                }
                source = name;
            } catch (Exception ignored) {
            }
        }
        return source;
    }

    public List<String> getPlayersFromIP(String ip) {
        List<String> players = new ArrayList<>();

        playerCollection.find(Filters.eq("ip", ip)).projection(new Document("username", 1))
                .forEach((Consumer<Document>) document -> players.add(document.getString("username")));
        return players;
    }

    public List<UUID> getPlayersByRank(Rank... ranks) {
        List<UUID> foundPlayers = new ArrayList<>();
        for (Rank rank : ranks) {
            playerCollection.find(Filters.eq("rank", rank.getDBName())).forEach((Consumer<Document>) document ->
                    foundPlayers.add(UUID.fromString(document.getString("uuid"))));
        }
        return foundPlayers;
    }

    public List<String> getPlayerNamesFromRank(Rank rank) {
        List<String> list = new ArrayList<>();
        playerCollection.find(Filters.eq("rank", rank.getDBName())).projection(new Document("username", 1))
                .forEach((Consumer<Document>) d -> list.add(d.getString("username")));
        return list;
    }

    public List<UUID> getPlayerUUIDsFromRank(Rank rank) {
        List<UUID> list = new ArrayList<>();
        playerCollection.find(Filters.eq("rank", rank.getDBName())).projection(new Document("uuid", 1))
                .forEach((Consumer<Document>) d -> list.add(UUID.fromString(d.getString("uuid"))));
        return list;
    }

    /*
    Party Methods
     */

    public Party getPartyById(String partyId) throws Exception {
        Document doc = partyCollection.find(Filters.eq("_id", new ObjectId(partyId))).first();
        if (doc == null) return null;
        return new Party(doc);
    }

    public Party getPartyByLeader(UUID leader) throws Exception {
        Document doc = partyCollection.find(Filters.eq("leader", leader.toString())).first();
        if (doc == null) return null;
        return new Party(doc);
    }

    public Party getPartyByMember(UUID member) throws Exception {
        Document doc = partyCollection.find(Filters.elemMatch("members", new Document("$eq", member.toString()))).first();
        if (doc == null) return null;
        return new Party(doc);
    }

    public List<Party> getParties() throws Exception {
        List<Party> parties = new ArrayList<>();
        FindIterable<Document> find = partyCollection.find();
        for (Document doc : find) {
            try {
                parties.add(new Party(doc));
            } catch (Exception ignored) {
            }
        }
        return parties;
    }

    public Party createParty(UUID leader) throws Exception {
        Document doc = new Document("leader", leader.toString()).append("members", Collections.singletonList(leader.toString()))
                .append("createdOn", System.currentTimeMillis()).append("invited", new ArrayList<>());
        partyCollection.insertOne(doc);
        doc = partyCollection.find(Filters.eq("leader", leader.toString())).first();
        return new Party(doc.getObjectId("_id").toHexString(), leader, new HashMap<>(), new HashMap<>());
    }

    public boolean hasPendingInvite(UUID uuid) {
        Document doc = partyCollection.find(Filters.elemMatch("invited", Filters.eq("uuid", uuid.toString()))).first();
        if (doc == null) return false;
        String id = doc.getObjectId("_id").toHexString();
        removeExpiredInvites(id);
        return partyCollection.find(Filters.elemMatch("invited", Filters.eq("uuid", uuid.toString()))).first() != null;
    }

    public void addPartyInvite(String partyID, UUID uuid, long expires) {
        Document doc = new Document("uuid", uuid.toString()).append("expires", expires);
        partyCollection.updateOne(Filters.eq("_id", new ObjectId(partyID)), Updates.push("invited", doc));
    }

    public void removePartyInvite(UUID uuid) {
        partyCollection.updateMany(Filters.elemMatch("invited", Filters.eq("uuid", uuid.toString())),
                Updates.pull("invited", Filters.eq("uuid", uuid.toString())));
    }

    public void removeExpiredInvites() {
        long time = System.currentTimeMillis();
        partyCollection.updateMany(Filters.elemMatch("invited", Filters.lte("expires", time)),
                Updates.pull("invited", Filters.lte("expires", time)));
    }

    public void removeExpiredInvites(String id) {
        partyCollection.updateOne(Filters.eq("_id", new ObjectId(id)),
                Updates.pull("invited", Filters.lte("expires", System.currentTimeMillis())));
    }

    public boolean acceptPartyInvite(UUID uuid) {
        Document doc = partyCollection.find(Filters.elemMatch("invited", Filters.eq("uuid", uuid.toString()))).first();
        if (doc == null) return false;
        for (Object o : doc.get("invited", ArrayList.class)) {
            Document inviteDoc = (Document) o;
            if (inviteDoc.getString("uuid").equals(uuid.toString())) {
                long expires = inviteDoc.getLong("expires");
                partyCollection.updateOne(Filters.eq("_id", doc.getObjectId("_id")),
                        Updates.pull("invited", Filters.eq("uuid", uuid.toString())));
                if (System.currentTimeMillis() < expires) {
                    partyCollection.updateOne(Filters.eq("_id", doc.getObjectId("_id")),
                            Updates.push("members", uuid.toString()));
                    return true;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    public void removePartyMember(String partyID, UUID uuid) {
        partyCollection.updateOne(Filters.eq("_id", new ObjectId(partyID)), Updates.pull("members", uuid.toString()));
    }

    public void closeParty(String partyID) {
        partyCollection.deleteOne(Filters.eq("_id", new ObjectId(partyID)));
    }

    public void closePartyByLeader(UUID leader) {
        partyCollection.deleteOne(Filters.eq("leader", leader.toString()));
    }

    public void setPartyLeader(String partyID, UUID currentLeader, UUID newLeader) {
        partyCollection.updateOne(Filters.and(Filters.eq("_id", new ObjectId(partyID)), Filters.eq("leader", currentLeader.toString())), Updates.set("leader", newLeader.toString()));
    }

    public TreeMap<Rank, Set<String>> getRankList(Predicate<? super Rank> rankPredicate) {
        TreeMap<Rank, Set<String>> players = new TreeMap<>(Comparator.comparingInt(Enum::ordinal));
        List<String> ranks = new ArrayList<>();
        Arrays.stream(Rank.values()).filter(rankPredicate).forEach(r -> ranks.add(r.getDBName()));
        FindIterable<Document> list = playerCollection.find(Filters.and(Filters.in("rank", ranks), Filters.eq("online", true)))
                .projection(new Document("username", true).append("rank", true).append("onlineData", true));
        for (Document doc : list) {
            try {
                Rank rank = Rank.fromString(doc.getString("rank"));
                if (rank.equals(Rank.TRAINEEBUILD) || rank.equals(Rank.TRAINEETECH)) rank = Rank.TRAINEE;
                Set<String> l = players.getOrDefault(rank, new TreeSet<>(Comparator.comparing(String::toLowerCase)));
                l.add(doc.getString("username") + ":" + doc.get("onlineData", Document.class).getString("server"));
                if (!players.containsKey(rank)) players.put(rank, l);
            } catch (Exception ignored) {
            }
        }
        return players;
    }

    public TreeMap<RankTag, Set<String>> getRankTagList(Predicate<? super RankTag> rankTagPredicate) {
        TreeMap<RankTag, Set<String>> players = new TreeMap<>(Comparator.comparingInt(Enum::ordinal));
        List<String> tags = new ArrayList<>();
        Arrays.stream(RankTag.values()).filter(rankTagPredicate).forEach(t -> tags.add(t.getDBName()));
        FindIterable<Document> list = playerCollection.find(Filters.and(Filters.in("tags", tags), Filters.eq("online", true)))
                .projection(new Document("username", true).append("tags", true).append("onlineData", true));
        for (Document doc : list) {
            for (Object o : doc.get("tags", ArrayList.class)) {
                try {
                    String tag = (String) o;
                    RankTag rankTag = RankTag.fromString(tag);
                    Set<String> l = players.getOrDefault(rankTag, new TreeSet<>(Comparator.comparing(String::toLowerCase)));
                    l.add(doc.getString("username") + ":" + doc.get("onlineData", Document.class).getString("server"));
                    if (!players.containsKey(rankTag)) players.put(rankTag, l);
                } catch (Exception ignored) {
                }
            }
        }
        return players;
    }

    /* Server Methods */

    public List<Server> getServers(boolean playground) {
        List<Server> list = new ArrayList<>();
        for (Document doc : serversCollection.find()) {
            if (playground) {
                if (!doc.containsKey("playground") || !doc.getBoolean("playground")) continue;
            } else {
                if (doc.containsKey("playground") && doc.getBoolean("playground")) continue;
            }
            list.add(new Server(doc.getString("name"), doc.getString("address"),
                    doc.getBoolean("park", false), doc.getString("type"), doc.getBoolean("online", false)));
        }
        return list;
    }

    public int getServerCount(String name) {
        Document doc;
        if (PalaceBungee.isTestNetwork()) {
            doc = serversCollection.find(Filters.and(
                    Filters.eq("playground", true),
                    Filters.eq("name", name)
            )).first();
        } else {
            doc = serversCollection.find(Filters.eq("name", name)).first();
        }
        if (doc == null) return 0;
        return doc.getInteger("count", 0);
    }

    public boolean isServerOnline(String name) {
        Document doc;
        if (PalaceBungee.isTestNetwork()) {
            doc = serversCollection.find(Filters.and(
                    Filters.eq("playground", true),
                    Filters.eq("name", name)
            )).first();
        } else {
            doc = serversCollection.find(Filters.eq("name", name)).first();
        }
        if (doc == null) return false;
        return doc.getBoolean("online", false);
    }

    public HashMap<String, Integer> getServerCounts() {
        FindIterable<Document> list = playerCollection.find(Filters.eq("online", true)).projection(new Document("onlineData", true));
        HashMap<String, Integer> map = new HashMap<>();
        for (Document doc : list) {
            try {
                String server = doc.get("onlineData", Document.class).getString("server");
                map.put(server, map.getOrDefault(server, 0) + 1);
            } catch (Exception ignored) {
            }
        }
        return map;
    }

    public HashMap<UUID, Integer> getProxyCounts() {
        FindIterable<Document> list = playerCollection.find(Filters.eq("online", true)).projection(new Document("onlineData", true));
        HashMap<UUID, Integer> map = new HashMap<>();
        for (Document doc : list) {
            try {
                UUID proxy = UUID.fromString(doc.get("onlineData", Document.class).getString("proxy"));
                map.put(proxy, map.getOrDefault(proxy, 0) + 1);
            } catch (Exception ignored) {
            }
        }
        return map;
    }

    public void createServer(Server server) {
        Document serverDocument = new Document("name", server.getName()).append("type", server.getServerType())
                .append("address", server.getAddress()).append("park", server.isPark());
        if (PalaceBungee.isTestNetwork()) serverDocument.append("playground", true);
        serversCollection.insertOne(serverDocument);
    }

    public void deleteServer(String name) {
        if (PalaceBungee.isTestNetwork()) {
            serversCollection.deleteMany(Filters.and(Filters.eq("name", name), Filters.eq("playground", true)));
        } else {
            serversCollection.deleteMany(Filters.and(Filters.eq("name", name), Filters.exists("playground", false)));
        }
    }

    /**
     * Log an accepted help request in the database
     *
     * @param requesting the requesting player
     * @param helping    the helping staff member
     */
    public void logHelpRequest(UUID requesting, UUID helping) {
        helpRequestsCollection.insertOne(new Document("requesting", requesting.toString())
                .append("helping", helping.toString()).append("time", System.currentTimeMillis()));
    }

    /**
     * Get the staff member's activity accepting help request
     *
     * @param staffMember the staff member to look up
     * @return a String with comma-separated values for accepted help requests: last day, last week, last month, all time
     */
    public String getHelpActivity(UUID staffMember) {
        List<Long> requests = new ArrayList<>();
        for (Document doc : helpRequestsCollection.find(Filters.eq("helping", staffMember.toString())).projection(new Document("time", true))) {
            if (doc.containsKey("time")) requests.add(doc.getLong("time"));
        }
        requests.sort((o1, o2) -> (int) (o1 - o2));
        long dayAgo = Instant.now().minus(Duration.ofDays(1)).toEpochMilli();
        long weekAgo = Instant.now().minus(Duration.ofDays(7)).toEpochMilli();
        long monthAgo = Instant.now().minus(Duration.ofDays(30)).toEpochMilli();
        int dayTotal = 0, weekTotal = 0, monthTotal = 0, total = 0;
        for (long r : requests) {
            if (r >= dayAgo) dayTotal++;
            if (r >= weekAgo) weekTotal++;
            if (r >= monthAgo) monthTotal++;
            total++;
        }
        return dayTotal + "," + weekTotal + "," + monthTotal + "," + total;
    }

    public long lastHelpRequest(UUID uuid) {
        Document doc = playerCollection.find(Filters.and(Filters.eq("uuid", uuid.toString()), Filters.exists("lastHelpRequest", true))).projection(new Document("lastHelpRequest", true)).first();
        if (doc == null) return 0;
        return (long) doc.getOrDefault("lastHelpRequest", 0L);
    }

    public void setLastHelpRequest(UUID uuid) {
        playerCollection.updateOne(Filters.eq("uuid", uuid.toString()), Updates.set("lastHelpRequest", System.currentTimeMillis()));
    }

    public boolean areHelpRequestsOverloaded() {
        return playerCollection.count(Filters.and(Filters.eq("online", true), Filters.gte("lastHelpRequest", System.currentTimeMillis() - (30 * 1000)))) >= 5;
    }

    public void setPendingHelpRequest(UUID uuid, boolean b) {
        playerCollection.updateOne(Filters.eq("uuid", uuid.toString()), Updates.set("pendingHelpRequest", b));
    }

    public boolean hasPendingHelpRequest(UUID uuid) {
        Document doc = playerCollection.find(Filters.and(Filters.eq("uuid", uuid.toString()), Filters.eq("pendingHelpRequest", true))).first();
        if (doc == null) return false;
        return doc.getBoolean("pendingHelpRequest", false);
    }

    public boolean canMakeAnnouncementRequest(UUID uuid) {
        return announcementRequestsCollection.count(Filters.eq("uuid", uuid.toString())) > 0;
    }

    public void makeAnnouncementRequest(UUID uuid, String announcement) {
        announcementRequestsCollection.insertOne(new Document("uuid", uuid.toString()).append("announcement", announcement));
    }

    public Document findAnnouncementRequest(UUID uuid) {
        return announcementRequestsCollection.find(Filters.eq("uuid", uuid.toString())).first();
    }

    public void removeAnnouncementRequest(UUID uuid) {
        announcementRequestsCollection.deleteMany(Filters.eq("uuid", uuid.toString()));
    }

    /**
     * Check whether any staff members are online
     *
     * @param guide true if guides should be included in this count
     * @return true if a staff member is online, false if not
     */
    public boolean areStaffOnline(boolean guide) {
        FindIterable<Document> find = playerCollection.find(Filters.eq("online", true)).projection(new Document("rank", true).append("tags", true));
        for (Document doc : find) {
            Rank rank = Rank.fromString(doc.getString("rank"));
            if (rank.getRankId() >= Rank.TRAINEE.getRankId()) return true;
            if (guide && doc.containsKey("tags")) {
                ArrayList list = doc.get("tags", ArrayList.class);
                for (Object o : list) {
                    RankTag tag = RankTag.fromString((String) o);
                    if (tag.equals(RankTag.GUIDE)) return true;
                }
            }
        }
        return false;
    }

    /*
    Moderation Methods
     */

    public boolean isPlayerMuted(UUID uuid) {
        Mute m = getCurrentMute(uuid);
        return m != null && m.isMuted();
    }

    public boolean isPlayerBanned(UUID uuid) {
        return getCurrentBan(uuid) != null;
    }

    public Mute getCurrentMute(UUID uuid) {
        Document doc = getPlayer(uuid, new Document("mutes", 1));
        if (doc == null) return null;
        for (Object o : doc.get("mutes", ArrayList.class)) {
            Document muteDoc = (Document) o;
            if (muteDoc == null || !muteDoc.getBoolean("active")) continue;
            return new Mute(uuid, true, muteDoc.getLong("created"), muteDoc.getLong("expires"),
                    muteDoc.getString("reason"), muteDoc.getString("source"));
        }
        return null;
    }

    public void unmutePlayer(UUID uuid) {
        playerCollection.updateMany(new Document("uuid", uuid.toString()).append("mutes.active", true), Updates.set("mutes.$.active", false));
    }

    public void mutePlayer(UUID uuid, Mute mute) {
        if (isPlayerMuted(uuid)) return;

        Document muteDocument = new Document("created", mute.getCreated()).append("expires", mute.getExpires())
                .append("reason", mute.getReason()).append("source", mute.getSource()).append("active", true);

        playerCollection.updateOne(Filters.eq("uuid", uuid.toString()), Updates.push("mutes", muteDocument));
    }

    public Ban getCurrentBan(UUID uuid) {
        return getCurrentBan(uuid, "");
    }

    public Ban getCurrentBan(UUID uuid, String name) {
        Document doc = getPlayer(uuid, new Document("bans", 1));
        for (Object o : doc.get("bans", ArrayList.class)) {
            Document banDoc = (Document) o;
            if (banDoc == null || !banDoc.getBoolean("active")) continue;
            return new Ban(uuid, name, banDoc.getBoolean("permanent"), banDoc.getLong("created"),
                    banDoc.getLong("expires"), banDoc.getString("reason"), banDoc.getString("source"));
        }
        return null;
    }

    public void kickPlayer(UUID uuid, Kick kick) {
        Document kickDocument = new Document("reason", kick.getReason())
                .append("time", System.currentTimeMillis())
                .append("source", kick.getSource());
        playerCollection.updateOne(Filters.eq("uuid", uuid.toString()), Updates.push("kicks", kickDocument));
    }

    public void warnPlayer(Warning warning) {
        Document warningDocument = new Document("reason", warning.getReason())
                .append("time", System.currentTimeMillis())
                .append("source", warning.getSource());
        playerCollection.updateOne(Filters.eq("uuid", warning.getUniqueId().toString()),
                Updates.push("warnings", warningDocument), new UpdateOptions().upsert(true));
    }

    public void unbanPlayer(UUID uuid) {
        playerCollection.updateMany(new Document("uuid", uuid.toString()).append("bans.active", true),
                new Document("$set", new Document("bans.$.active", false).append("bans.$.expires", System.currentTimeMillis())));
    }

    public void banPlayer(UUID uuid, Ban ban) {
        if (isPlayerBanned(uuid)) return;

        Document banDocument = new Document("created", ban.getCreated()).append("expires", ban.getExpires())
                .append("permanent", ban.isPermanent()).append("reason", ban.getReason())
                .append("source", ban.getSource()).append("active", true);

        playerCollection.updateOne(Filters.eq("uuid", uuid.toString()), Updates.push("bans", banDocument));
    }

    public void banProvider(ProviderBan ban) {
        bansCollection.insertOne(new Document("type", "provider").append("data", ban.getProvider())
                .append("source", ban.getSource()));
    }

    public ProviderBan getProviderBan(String isp) {
        Document doc = bansCollection.find(new Document("type", "provider").append("data", isp)).first();
        if (doc == null) return null;
        return new ProviderBan(doc.getString("data"), doc.getString("source"));
    }

    public List<String> getBannedProviders() {
        List<String> list = new ArrayList<>();
        for (Document doc : bansCollection.find(new Document("type", "provider"))) {
            list.add(doc.getString("data"));
        }
        return list;
    }

    public void unbanProvider(String isp) {
        bansCollection.deleteMany(new Document("type", "provider").append("data", isp));
    }

    public void banAddress(AddressBan ban) {
        bansCollection.insertOne(new Document("type", "ip").append("data", ban.getAddress())
                .append("reason", ban.getReason()).append("source", ban.getSource()));
    }

    public AddressBan getAddressBan(String address) {
        Document doc = bansCollection.find(new Document("type", "ip").append("data", address)).first();
        if (doc == null) return null;
        return new AddressBan(doc.getString("data"), doc.getString("reason"), doc.getString("source"));
    }

    public void unbanAddress(String address) {
        bansCollection.deleteMany(new Document("type", "ip").append("data", address));
    }

    public void updateProviderData(UUID uuid, ProviderData data) {
        Document doc = new Document("isp", data.getIsp()).append("country", data.getCountry())
                .append("region", data.getRegion()).append("regionName", data.getRegionName())
                .append("timezone", data.getTimezone());
        playerCollection.updateOne(Filters.eq("uuid", uuid.toString()), new Document("$set", doc));
    }

    public ArrayList getBans(UUID uuid) {
        return getPlayer(uuid, new Document("bans", 1)).get("bans", ArrayList.class);
    }

    public ArrayList getMutes(UUID uuid) {
        return getPlayer(uuid, new Document("mutes", 1)).get("mutes", ArrayList.class);
    }

    public ArrayList getKicks(UUID uuid) {
        return getPlayer(uuid, new Document("kicks", 1)).get("kicks", ArrayList.class);
    }

    public ArrayList getWarnings(UUID uuid) {
        Document doc = getPlayer(uuid, new Document("warnings", 1));
        if (doc == null || !doc.containsKey("warnings")) {
            return new ArrayList();
        }
        return doc.get("warnings", ArrayList.class);
    }

    public Rank getRank(String username) {
        Document doc = getPlayer(username, new Document("rank", true));
        if (doc == null || !doc.containsKey("rank")) return Rank.SETTLER;
        return Rank.fromString(doc.getString("rank"));
    }

    public Rank getRank(UUID uuid) {
        Document doc = getPlayer(uuid, new Document("rank", true));
        if (doc == null || !doc.containsKey("rank")) return Rank.SETTLER;
        return Rank.fromString(doc.getString("rank"));
    }

    public void setWarningCooldown(UUID uuid) {
        playerCollection.updateOne(Filters.and(Filters.eq("uuid", uuid.toString()), Filters.eq("online", true)), Updates.set("lastWarned", System.currentTimeMillis()));
    }

    public long getWarningCooldown(UUID uuid) {
        Document doc = getPlayer(uuid, new Document("lastWarned", true));
        if (doc == null || !doc.containsKey("lastWarned")) return 0;
        return doc.getLong("lastWarned");
    }

    /*
    Password Methods
     */

    public boolean verifyPassword(UUID uuid, String pass) {
        Document doc = getPlayer(uuid, new Document("staffPassword", 1));
        if (doc == null || !doc.containsKey("staffPassword")) return false;
        String dbPassword = doc.getString("staffPassword");
        return PalaceBungee.getPasswordUtil().validPassword(pass, dbPassword);
    }

    public boolean hasPassword(UUID uuid) {
        return getPlayer(uuid, new Document("staffPassword", 1)).containsKey("staffPassword");
    }

    public void setPassword(UUID uuid, String pass) {
        String salt = PalaceBungee.getPasswordUtil().getNewSalt();
        String hashed = PalaceBungee.getPasswordUtil().hashPassword(pass, salt);
        playerCollection.updateOne(Filters.eq("uuid", uuid.toString()), Updates.set("staffPassword", hashed), new UpdateOptions().upsert(true));
    }

    public int getStaffPasswordAttempts(UUID uuid) {
        Document doc = getPlayer(uuid, new Document("staffPasswordAttempts", true));
        if (doc == null || !doc.containsKey("staffPasswordAttempts")) return 0;
        return doc.getInteger("staffPasswordAttempts");
    }

    public void setStaffPasswordAttempts(UUID uuid, int attempts) {
        playerCollection.updateOne(Filters.eq("uuid", uuid.toString()), Updates.set("staffPasswordAttempts", attempts));
    }

    private List<UUID> getIgnoredUsers(UUID uuid) {
        List<UUID> list = new ArrayList<>();
        for (Object o : getPlayer(uuid, new Document("ignoring", 1)).get("ignoring", ArrayList.class)) {
            Document doc = (Document) o;
            list.add(UUID.fromString(doc.getString("uuid")));
        }
        return list;
    }

    public HashMap<UUID, String> getFriendList(UUID uuid) {
        return getList(uuid, 1);
    }

    public HashMap<UUID, String> getFriendRequestList(UUID uuid) {
        return getList(uuid, 0);
    }

    public HashMap<UUID, String> getList(UUID uuid, int id) {
        List<UUID> list = new ArrayList<>();
        for (Document doc : friendsCollection.find(Filters.or(Filters.eq("sender", uuid.toString()),
                Filters.eq("receiver", uuid.toString())))) {
            UUID sender = UUID.fromString(doc.getString("sender"));
            UUID receiver = UUID.fromString(doc.getString("receiver"));
            boolean friend = doc.getLong("started") > 0;
            if (id == 0 && !friend && receiver.equals(uuid)) {
                list.add(sender);
            } else if (id == 1 && friend) {
                if (uuid.equals(sender)) {
                    list.add(receiver);
                } else {
                    list.add(sender);
                }
            }
        }
        HashMap<UUID, String> map = new HashMap<>();
        for (UUID uid : list) {
            map.put(uid, PalaceBungee.getUsername(uid));
        }
        return map;
    }


    public void addFriendRequest(UUID sender, UUID receiver) {
        friendsCollection.insertOne(new Document("sender", sender.toString()).append("receiver", receiver.toString())
                .append("started", 0L));
    }

    public void removeFriend(UUID sender, UUID receiver) {
        friendsCollection.deleteOne(Filters.or(
                new Document("sender", sender.toString()).append("receiver", receiver.toString()),
                new Document("receiver", sender.toString()).append("sender", receiver.toString())
        ));
    }

    public void acceptFriendRequest(UUID receiver, UUID sender) {
        friendsCollection.updateOne(new Document("sender", sender.toString()).append("receiver", receiver.toString()),
                Updates.set("started", System.currentTimeMillis()));
    }

    public void denyFriendRequest(UUID receiver, UUID sender) {
        friendsCollection.deleteOne(new Document("sender", sender.toString()).append("receiver", receiver.toString()).append("started", 0L));
    }

    public boolean getFriendRequestToggle(UUID uuid) {
        Document doc = getPlayer(uuid, new Document("settings", 1));
        if (doc == null) {
            return false;
        }
        Document settings = (Document) doc.get("settings");
        if (settings == null) {
            return false;
        }
        return settings.getBoolean("friendRequestToggle");
    }

    public void setFriendRequestToggle(UUID uuid, boolean value) {
        setSetting(uuid, "friendRequestToggle", value);
    }

    public void staffClock(UUID uuid, boolean b) {
        staffLoginCollection.insertOne(new Document("uuid", uuid.toString()).append("time", System.currentTimeMillis()).append("login", b));
    }

    public int getForumMemberId(UUID uuid) {
        try {
            Document forumDoc = getPlayer(uuid, new Document("forums", 1));
            Document forums = (Document) forumDoc.get("forums");
            return forums.getInteger("member_id");
        } catch (Exception e) {
            return -1;
        }
    }

    public String getForumLinkingCode(UUID uuid) {
        try {
            Document forumDoc = getPlayer(uuid, new Document("forums", 1));
            Document forums = (Document) forumDoc.get("forums");
            return forums.getString("linking-code");
        } catch (Exception e) {
            return null;
        }
    }

    public void setForumLinkingCode(UUID uuid, int member_id, String code) {
        Document forumDoc = new Document("member_id", member_id).append("linking-code", code);
        playerCollection.updateOne(Filters.eq("uuid", uuid.toString()), Updates.set("forums", forumDoc));
    }

    public void setForumAccountData(UUID uuid, int member_id) {
        Document forumDoc = new Document("member_id", member_id);
        playerCollection.updateOne(Filters.eq("uuid", uuid.toString()), Updates.set("forums", forumDoc));
    }

    public void unsetForumLinkingCode(UUID uuid) {
        playerCollection.updateOne(Filters.eq("uuid", uuid.toString()), Updates.unset("forums.linking-code"));
    }

    public void unlinkForumAccount(UUID uuid) {
        playerCollection.updateOne(Filters.eq("uuid", uuid.toString()), Updates.unset("forums"));
    }

    public void ignorePlayer(Player player, UUID uuid) {
        long time = System.currentTimeMillis();
        playerCollection.updateOne(Filters.eq("uuid", player.getUniqueId().toString()), Updates.push("ignoring", new Document("uuid", uuid.toString()).append("started", time)));
    }

    public void unignorePlayer(Player player, UUID uuid) {
        playerCollection.updateOne(Filters.eq("uuid", player.getUniqueId().toString()), Updates.pull("ignoring", new Document("uuid", uuid.toString())));
    }

    /**
     * Does the first player ignore the second player?
     *
     * @param uuid  the first player's uuid
     * @param uuid2 the second player's uuid
     * @return true if uuid ignores uuid2
     */
    public boolean doesPlayerIgnorePlayer(UUID uuid, UUID uuid2) {
        return getIgnoredUsers(uuid).contains(uuid2);
    }

    public void logAFK(UUID uuid) {
        playerCollection.updateOne(Filters.eq("uuid", uuid.toString()),
                Updates.push("afklogs", System.currentTimeMillis()), new UpdateOptions().upsert(true));
    }

    public void setOnlineData(UUID uuid, String key, Object value) {
        playerCollection.updateOne(Filters.eq("uuid", uuid.toString()), Updates.set("onlineData." + key, value));
    }

    public Document getVirtualQueue(String queueId) {
        return virtualQueuesCollection.find(Filters.eq("queueId", queueId)).first();
    }

    public void logChatMessage(UUID sender, String message, String channel, long time, boolean okay, String filterCaught, String offendingText) {
        Document doc = new Document("uuid", sender.toString()).append("message", message).append("channel", channel).append("time", time / 1000).append("okay", okay);
        if (!okay) {
            doc.append("uuid", sender.toString()).append("message", message).append("time", time).append("okay", false).append("filterCaught", filterCaught).append("offendingText", offendingText);
        }
        chatCollection.insertOne(doc);
    }

    public void completeTutorial(UUID uuid) {
        playerCollection.updateOne(Filters.eq("uuid", uuid.toString()), Updates.set("tutorial", true));
    }
}
