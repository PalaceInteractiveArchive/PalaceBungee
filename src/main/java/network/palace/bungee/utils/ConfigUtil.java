package network.palace.bungee.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class ConfigUtil {
    @Getter @Setter private Favicon favicon;
    @Getter @Setter private BaseComponent motdComponent;

    public ConfigUtil() throws IOException {
        reload();
    }

    public String getDashboardURL() {
        return "null";
    }

    public void reload() throws IOException {
        this.favicon = Favicon.create(ImageIO.read(new File("server-icon.png")));
        this.motdComponent = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "     &8&l>>   &d&lPalace Network &8| &71.12.2-1.15.2   &8&l<<                &a&lSupports &d&l1.12&7/&b&l1.13&7/&6&l1.14&7/&e&l1.15")));
    }

    public DatabaseConnection getRabbitMQInfo() {
        try {
            Configuration config = getConfig().getSection("rabbitmq");
            return new DatabaseConnection(config.getString("host"), config.getString("username"), config.getString("password"), config.getString("virtualhost"), 0);
        } catch (Exception e) {
            e.printStackTrace();
            return new DatabaseConnection("", "", "", "", 0);
        }
    }

    public DatabaseConnection getMongoDBInfo() {
        try {
            Configuration config = getConfig().getSection("mongodb");
            return new DatabaseConnection(config.getString("hostname"), config.getString("username"), config.getString("password"), null, 0);
        } catch (Exception e) {
            e.printStackTrace();
            return new DatabaseConnection("", "", "", "", 0);
        }
    }

    public Configuration getConfig() throws IOException {
        return ConfigurationProvider.getProvider(YamlConfiguration.class).load(getConfigFile());
    }

    public File getConfigFile() throws IOException {
        File folder = new File("plugins/PalaceBungee");
        if (!folder.exists()) folder.mkdir();

        File file = new File(folder, "config.yml");
        if (!file.exists()) {
            file.createNewFile();
        }

        return file;
    }

    @Getter
    @AllArgsConstructor
    public static class DatabaseConnection {

        private String host, username, password, database;
        private int port;
    }
}
