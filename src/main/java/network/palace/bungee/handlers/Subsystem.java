package network.palace.bungee.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

@Getter
@AllArgsConstructor
public enum Subsystem {
    PARTY("Party", ChatColor.BLUE), CHAT("Palace Chat", ChatColor.GREEN);

    String name;
    ChatColor color;

    public String getPrefix() {
        return color + "[" + name + "] ";
    }

    public String getComponentPrefix() {
        return "[" + name + "] ";
    }
}
