package network.palace.bungee.utils;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class ConfigUtil {
    @Getter @Setter private Favicon favicon;
    @Getter @Setter private BaseComponent motdComponent;

    public ConfigUtil() throws IOException {
        this.favicon = Favicon.create(ImageIO.read(new File("server-icon.png")));
        this.motdComponent = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "     &8&l>>   &d&lPalace Network &8| &71.12.2-1.15.2   &8&l<<                &a&lSupports    &d&l1.12&7/&b&l1.13&7/&6&l1.14&7/&e&l1.15")));
    }
}
