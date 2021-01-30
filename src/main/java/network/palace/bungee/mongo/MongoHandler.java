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
import network.palace.bungee.handlers.Party;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.RankTag;
import network.palace.bungee.handlers.Server;
import network.palace.bungee.handlers.moderation.*;
import network.palace.bungee.utils.ConfigUtil;
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

@SuppressWarnings({"rawtypes", "unchecked"})
public class MongoHandler {

    private final MongoClient client;
    private final MongoCollection<Document> bansCollection;
    private final MongoCollection<Document> partyCollection;
    @Getter private final MongoCollection<Document> playerCollection;
    @Getter private final MongoCollection<Document> resourcePackCollection;
    private final MongoCollection<Document> serversCollection;
    private final MongoCollection<Document> serviceConfigCollection;
    private final MongoCollection<Document> spamIpWhitelist;
    private final MongoCollection<Document> helpRequestsCollection;
    private final MongoCollection<Document> announcementRequestsCollection;

    public MongoHandler() throws IOException {
        ConfigUtil.DatabaseConnection mongo = PalaceBungee.getConfigUtil().getMongoDBInfo();
        String hostname = mongo.getHost();
        String username = mongo.getUsername();
        String password = mongo.getPassword();
        MongoClientURI connectionString = new MongoClientURI("mongodb://" + username + ":" + password + "@" + hostname);
        client = new MongoClient(connectionString);
        MongoDatabase database = client.getDatabase("palace");
        bansCollection = database.getCollection("bans");
        partyCollection = database.getCollection("parties");
        playerCollection = database.getCollection("players");
        resourcePackCollection = database.getCollection("resourcepacks");
        serversCollection = database.getCollection("servers");
        serviceConfigCollection = database.getCollection("service_configs");
        spamIpWhitelist = database.getCollection("spamipwhitelist");
        helpRequestsCollection = database.getCollection("help_requests");
        announcementRequestsCollection = database.getCollection("announcement_requests");
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

    public int getOnlineCount() {
        return (int) playerCollection.count(Filters.eq("online", true));
    }

    public void login(UUID uuid) {
        playerCollection.updateOne(new Document("uuid", uuid.toString()), new Document("$set",
                new Document("online", true)
                        .append("onlineData", new Document("proxy", PalaceBungee.getProxyID().toString()).append("server", "Hub1"))
        ));
    }

    public void logout(UUID uuid) {
        playerCollection.updateOne(new Document("uuid", uuid.toString()), Updates.set("online", false));
        playerCollection.updateOne(new Document("uuid", uuid.toString()), Updates.unset("onlineData"));
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
                config.getString("minVersionString"), config.get("mutedChats", ArrayList.class));
    }

    public void setBungeeConfig(ConfigUtil.BungeeConfig config) throws Exception {
        serviceConfigCollection.updateOne(Filters.eq("type", "bungeecord"), new Document("$set",
                new Document("maintenance", config.isMaintenance())
                        .append("chatDelay", config.getChatDelay())
                        .append("dmEnabled", config.isDmEnabled())
                        .append("strictChat", config.isStrictChat())
                        .append("strictThreshold", config.getStrictThreshold())
                        .append("mutedChats", config.getMutedChats())
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

    public void addSpamIPWhitelist(SpamIPWhitelist whitelist) {
        spamIpWhitelist.insertOne(new Document("ip", whitelist.getAddress()).append("limit", whitelist.getLimit()));
    }

    public SpamIPWhitelist getSpamIPWhitelist(String address) {
        Document doc = spamIpWhitelist.find(Filters.eq("ip", address)).first();
        if (doc == null) return null;
        return new SpamIPWhitelist(doc.getString("ip"), doc.getInteger("limit"));
    }

    public void removeSpamIPWhitelist(String address) {
        spamIpWhitelist.deleteMany(Filters.eq("ip", address));
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
                    doc.getBoolean("park"), doc.getString("type"), doc.getBoolean("online")));
        }
        return list;
    }

    public int getServerCount(String name) {
        return (int) playerCollection.count(Filters.eq("onlineData.server", name));
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
}
