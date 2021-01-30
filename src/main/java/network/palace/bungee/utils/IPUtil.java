package network.palace.bungee.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.moderation.ProviderData;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.logging.Level;

public class IPUtil {
    private static final HashMap<String, Match> cache = new HashMap<>();
    private static int count = 0;
    private static long lastReset = System.currentTimeMillis();

    public static ProviderData getProviderData(String address) {
        if (address == null || address.isEmpty()) {
            PalaceBungee.getProxyServer().getLogger().info("Empty address!");
            return null;
        }
        if (System.currentTimeMillis() - (60 * 1000) > lastReset) {
            lastReset = System.currentTimeMillis();
            count = 0;
            PalaceBungee.getProxyServer().getLogger().info("Over one minute");
        }
        if (count >= 45) {
            PalaceBungee.getProxyServer().getLogger().info("count >= 45");
            return null;
        }
        if (cache.containsKey(address)) {
            Match m = cache.get(address);
            if (System.currentTimeMillis() - (6 * 60 * 60 * 1000) < m.getTime()) {
                PalaceBungee.getProxyServer().getLogger().info("Cached value: " + address + " -> " + m.getData().toString());
                return m.getData();
            }
            cache.remove(address);
        }
        count++;
        ProviderData data = request(address);
        if (data == null) {
            PalaceBungee.getProxyServer().getLogger().info("Error requesting provider info for " + address + "!");
            return null;
        }
        Match m = new Match(data);
        cache.put(address, m);
        PalaceBungee.getProxyServer().getLogger().info("New request: " + address + " -> " + m.getData().toString());
        return m.getData();
    }

    private static ProviderData request(String address) {
        String url = "http://ip-api.com/json/" + address + "?fields=33550";
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            JsonParser parser = new JsonParser();
            JsonObject obj = parser.parse(jsonText).getAsJsonObject();
            if (obj.has("message")) {
                return null;
            }
            return new ProviderData(obj.get("isp").getAsString(), obj.get("countryCode").getAsString(),
                    obj.get("region").getAsString(), obj.get("regionName").getAsString(),
                    obj.get("timezone").getAsString());
        } catch (IOException e) {
            PalaceBungee.getProxyServer().getLogger().log(Level.SEVERE, "Error retrieving IP address", e);
            return null;
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    private static class Match {
        @Getter private final ProviderData data;
        @Getter private final long time;

        public Match(ProviderData data) {
            this.data = data;
            this.time = System.currentTimeMillis();
        }
    }
}