package network.palace.bungee.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.palace.bungee.PalaceBungee;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class NameUtil {

    public static List<String> getNames(String name) {
        List<String> names = new ArrayList<>();
        try {
            String webData = readUrl("https://api.mojang.com/users/profiles/minecraft/" + name);
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
            PalaceBungee.getProxyServer().getLogger().log(Level.SEVERE, "Error retrieving name history from Mojang API", e);
        }
        return names;
    }

    public static List<String> getNames(String n, String uuid) throws Exception {
        Gson gson = new Gson();
        List<String> names = new ArrayList<>();
        String namesData = readUrl("https://api.mojang.com/user/profiles/" + uuid + "/names");
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

    public static String readUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) buffer.append(chars, 0, read);
            return buffer.toString();
        }
    }
}