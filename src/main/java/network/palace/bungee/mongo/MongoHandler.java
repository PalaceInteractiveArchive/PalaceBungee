package network.palace.bungee.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.Favicon;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.AddressBan;
import network.palace.bungee.handlers.Ban;
import network.palace.bungee.utils.ConfigUtil;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;

@SuppressWarnings("rawtypes")
public class MongoHandler {

    private final MongoClient client;
    private final MongoCollection<Document> bansCollection;
    private final MongoCollection<Document> playerCollection;
    private final MongoCollection<Document> serviceConfigCollection;

    public MongoHandler() throws IOException {
        ConfigUtil.DatabaseConnection mongo = PalaceBungee.getConfigUtil().getMongoDBInfo();
        String hostname = mongo.getHost();
        String username = mongo.getUsername();
        String password = mongo.getPassword();
        MongoClientURI connectionString = new MongoClientURI("mongodb://" + username + ":" + password + "@" + hostname);
        client = new MongoClient(connectionString);
        MongoDatabase database = client.getDatabase("palace");
        bansCollection = database.getCollection("bans");
        playerCollection = database.getCollection("players");
        serviceConfigCollection = database.getCollection("service_configs");

        PalaceBungee.getConfigUtil().reload();
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
        FindIterable<Document> doc = playerCollection.find(MongoFilter.UUID.getFilter(uuid.toString())).projection(limit);
        if (doc == null) return null;
        return doc.first();
    }

    public boolean isPlayerInDB(UUID uuid) {
        return getPlayer(uuid, new Document("uuid", 1)) != null;
    }

    public boolean isBanned(UUID uuid) {
        return getCurrentBan(uuid) != null;
    }

    public AddressBan getAddressBan(String address) {
        Document doc = bansCollection.find(new Document("type", "ip").append("data", address)).first();
        if (doc == null) return null;
        return new AddressBan(doc.getString("data"), doc.getString("reason"), doc.getString("source"));
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

    /**
     * Unban the player
     * <p>
     * Sets any active bans to inactive
     *
     * @param uuid the uuid of the player
     */
    public void unbanPlayer(UUID uuid) {
        playerCollection.updateMany(new Document("uuid", uuid.toString()).append("bans.active", true),
                new Document("$set", new Document("bans.$.active", false).append("bans.$.expires", System.currentTimeMillis())));
        playerCollection.updateOne(new Document("uuid", uuid.toString()).append("bans.reason", "MCLeaks Account"),
                Updates.pull("bans", new Document("reason", "MCLeaks Account")));
    }

    public int getOnlineCount() {
        return (int) playerCollection.count(Filters.exists("online"));
    }

    public void login(UUID uuid) {
        playerCollection.updateOne(new Document("uuid", uuid.toString()), Updates.set("online", new Document("proxy", "bungee1").append("server", "Hub1")));
    }

    public void logout(UUID uuid) {
        playerCollection.updateOne(new Document("uuid", uuid.toString()), Updates.unset("online"));
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
                config.getBoolean("maintenance"), config.getInteger("chatDelay"), config.getBoolean("parkChatMuted"),
                config.getBoolean("dmEnabled"), config.getBoolean("strictChat"), config.getDouble("strictThreshold"),
                config.getInteger("maxVersion"), config.getInteger("minVersion"), config.getString("maxVersionString"),
                config.getString("minVersionString"));
    }

    public enum MongoFilter {
        UUID, USERNAME, RANK;

        public Bson getFilter(String s) {
            switch (this) {
                case UUID:
                    return Filters.eq("uuid", s);
                case USERNAME:
                    return Filters.regex("username", "^" + s + "$", "i");
                case RANK:
                    return Filters.eq("rank", s);
            }
            return null;
        }
    }
}
