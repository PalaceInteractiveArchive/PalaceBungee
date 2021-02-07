package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;

import java.util.Random;

public class AudioCommand extends PalaceCommand {
    private final Random random = new Random();

    public AudioCommand() {
        super("audio");
    }

    @Override
    public void execute(Player player, String[] args) {
        String token = getRandomToken();
        //TODO If the player is currently connected, they should be disconnected since their token changed
        PalaceBungee.getMongoHandler().setOnlineData(player.getUniqueId(), "audioToken", token);
        player.sendMessage(new ComponentBuilder("\nClick here to connect to the Audio Server!\n")
                .color(ChatColor.GREEN).underlined(true).bold(true)
                .event((new ClickEvent(ClickEvent.Action.OPEN_URL,
                        (PalaceBungee.isTestNetwork() ? "https://audio-test.palace.network/?t=" :
                                "https://audio.palace.network/?t=") + token))).create());
    }

    public String getRandomToken() {
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }
}
