package network.palace.bungee.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

@NoArgsConstructor
@AllArgsConstructor
public class ChatModifier {
    @Getter @Setter private ClickEvent clickEvent;
    @Getter @Setter private HoverEvent hoverEvent;
    @Getter @Setter private boolean bold = false;
    @Getter @Setter private boolean italic = false;
    @Getter @Setter private boolean strikethrough = false;
    @Getter @Setter private boolean underline = false;
    @Getter @Setter private boolean random = false;
    @Getter @Setter private ChatColor color = null;

    public TextComponent apply(TextComponent component) {
        component.setClickEvent(clickEvent);
        component.setHoverEvent(hoverEvent);
        component.setBold(bold);
        component.setItalic(italic);
        component.setStrikethrough(strikethrough);
        component.setUnderlined(underline);
        component.setObfuscated(random);
        if (color != null) {
            component.setColor(color);
        }
        return component;
    }

    @Override
    public ChatModifier clone() throws CloneNotSupportedException {
        return new ChatModifier(clickEvent, hoverEvent, bold, italic, strikethrough, underline, random, color);
    }
}
