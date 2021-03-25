package network.palace.bungee.commands.moderation;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.utils.HTTPUtil;

import java.util.ArrayList;
import java.util.List;

public class NamecheckCommand extends PalaceCommand {

    public NamecheckCommand() {
        super("namecheck", Rank.TRAINEE);
        tabComplete = true;
        tabCompletePlayers = true;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "/namecheck [username]");
            return;
        }
        player.sendMessage(ChatColor.GREEN + "Requesting name history for " + ChatColor.AQUA + args[0] +
                ChatColor.GREEN + " from Mojang...");
        List<String> names = getNames(args[0]);
        if (names.isEmpty()) {
            player.sendMessage(ChatColor.RED + "That user could not be found!");
            return;
        }
        StringBuilder msg = new StringBuilder(ChatColor.GREEN + "Previous usernames of " + args[0] + " are:");
        for (int i = 0; i < names.size(); i++) {
            msg.append(ChatColor.AQUA).append("\n").append(i + 1).append(": ").append(ChatColor.GREEN).append(names.get(i));
        }
        player.sendMessage(msg.toString());
    }

    private List<String> getNames(String name) {
        List<String> names = new ArrayList<>();
        try {
            String webData = HTTPUtil.readUrl("https://api.mojang.com/users/profiles/minecraft/" + name);
            Gson gson = new Gson();
            JsonObject uuidData = gson.fromJson(webData, JsonObject.class);
            String uuid = "";
            if (uuidData != null) {
                uuid = uuidData.get("id").getAsString();
            }
            if (!uuid.equals("")) {
                List<String> list = getNames(name, uuid);
                names.addAll(list);
            }
        } catch (Exception e) {
            PalaceBungee.getProxyServer().getLogger().warning("Error retrieving name history from Mojang API");
            e.printStackTrace();
        }
        return names;
    }

    public static List<String> getNames(String n, String uuid) throws Exception {
        Gson gson = new Gson();
        List<String> names = new ArrayList<>();
        String namesData = HTTPUtil.readUrl("https://api.mojang.com/user/profiles/" + uuid + "/names");
        JsonArray pastNames = gson.fromJson(namesData, JsonArray.class);
        if (pastNames == null) {
            return names;
        }
        for (int i = 0; i < pastNames.size(); i++) {
            JsonElement element = gson.fromJson(pastNames.get(i), JsonElement.class);
            JsonObject nameObj = element.getAsJsonObject();
            String name = nameObj.get("name").getAsString();
            names.add(name);
        }
        return names;
    }
}