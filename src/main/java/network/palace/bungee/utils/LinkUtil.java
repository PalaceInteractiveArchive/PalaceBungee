package network.palace.bungee.utils;

import com.google.common.collect.ImmutableMap;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import network.palace.bungee.handlers.ChatModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Marc on 2/10/17.
 */
public class LinkUtil {

    private static class StringMessage {
        private static final Map<Character, ChatColor> formatMap;
        private static final Pattern INCREMENTAL_PATTERN = Pattern.compile("(" + ChatColor.COLOR_CHAR + "[0-9a-fk-or])|(\\n)|((?:(?:https?)://)?(?:[-\\w_.]{2,}\\.[a-z]{2,4}.*?(?=[.?!,;:]?(?:[" + ChatColor.COLOR_CHAR + " \\n]|$))))", Pattern.CASE_INSENSITIVE);

        static {
            ImmutableMap.Builder<Character, ChatColor> builder = ImmutableMap.builder();
            for (ChatColor format : ChatColor.values()) {
                builder.put(Character.toLowerCase(format.toString().charAt(1)), format);
            }
            formatMap = builder.build();
        }

        private final List<BaseComponent> list = new ArrayList<>();
        private BaseComponent currentChatComponent = new TextComponent("");
        private ChatModifier modifier = new ChatModifier();
        private final BaseComponent[] output;
        private int currentIndex;
        private final String message;

        private StringMessage(String message, boolean keepNewlines) {
            this.message = message;
            if (message == null) {
                output = new BaseComponent[]{currentChatComponent};
                return;
            }
            list.add(currentChatComponent);

            Matcher matcher = INCREMENTAL_PATTERN.matcher(message);
            String match;
            while (matcher.find()) {
                int groupId = 0;
                while ((match = matcher.group(++groupId)) == null) {
                    // NOOP
                }
                appendNewComponent(matcher.start(groupId));
                switch (groupId) {
                    case 1:
                        ChatColor format = formatMap.get(match.toLowerCase(java.util.Locale.ENGLISH).charAt(1));
                        if (format == ChatColor.RESET) {
                            modifier = new ChatModifier();
                        } else if (isFormat(format)) {
                            if (format.equals(ChatColor.BOLD)) {
                                modifier.setBold(true);
                            } else if (format.equals(ChatColor.ITALIC)) {
                                modifier.setItalic(true);
                            } else if (format.equals(ChatColor.STRIKETHROUGH)) {
                                modifier.setStrikethrough(true);
                            } else if (format.equals(ChatColor.UNDERLINE)) {
                                modifier.setUnderline(true);
                            } else if (format.equals(ChatColor.MAGIC)) {
                                modifier.setRandom(true);
                            } else {
                                throw new AssertionError("Unexpected message format");
                            }
                        } else { // Color resets formatting
                            modifier = new ChatModifier();
                            modifier.setColor(format);
                        }
                        break;
                    case 2:
                        if (keepNewlines) {
                            currentChatComponent.addExtra(new TextComponent("\n"));
                        } else {
                            currentChatComponent = null;
                        }
                        break;
                    case 3:
                        if (!(match.startsWith("http://") || match.startsWith("https://"))) {
                            match = "http://" + match;
                        }
                        modifier.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, match));
                        modifier.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open ").color(ChatColor.AQUA).append(match).color(ChatColor.GREEN).create()));
                        appendNewComponent(matcher.end(groupId));
                        modifier.setClickEvent(null);
                        modifier.setHoverEvent(null);
                }
                currentIndex = matcher.end(groupId);
            }

            if (currentIndex < message.length()) {
                appendNewComponent(message.length());
            }

            output = list.toArray(new BaseComponent[list.size()]);
        }

        private boolean isFormat(ChatColor format) {
            return format.equals(ChatColor.BOLD) || format.equals(ChatColor.ITALIC) || format.equals(ChatColor.STRIKETHROUGH) || format.equals(ChatColor.UNDERLINE) || format.equals(ChatColor.MAGIC);
        }

        private void appendNewComponent(int index) {
            if (index <= currentIndex) {
                return;
            }
            BaseComponent addition = modifier.apply(new TextComponent(message.substring(currentIndex, index)));
            currentIndex = index;
            try {
                modifier = modifier.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            if (currentChatComponent == null) {
                currentChatComponent = new TextComponent("");
                list.add(currentChatComponent);
            }
            currentChatComponent.addExtra(addition);
        }

        private BaseComponent[] getOutput() {
            return output;
        }
    }

    public static BaseComponent[] fromString(String message) {
        return new StringMessage(message, true).getOutput();
    }

    public static BaseComponent fromComponent(BaseComponent component) {
        BaseComponent[] arr = new StringMessage(component.toPlainText(), true).getOutput();
        for (BaseComponent c : arr) {
            if (c == null || c.getExtra() == null || c.getExtra().isEmpty()) {
                continue;
            }
            BaseComponent c2 = c.getExtra().get(0);
            if (c2.getClickEvent() != null) {
                component.setClickEvent(c2.getClickEvent());
            }
            if (c2.getHoverEvent() != null) {
                component.setHoverEvent(c2.getHoverEvent());
            }
        }
        return component;
    }
}
