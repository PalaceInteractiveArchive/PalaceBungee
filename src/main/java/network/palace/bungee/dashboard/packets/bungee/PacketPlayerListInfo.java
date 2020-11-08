package network.palace.bungee.dashboard.packets.bungee;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import network.palace.bungee.dashboard.packets.BasePacket;
import network.palace.bungee.dashboard.packets.PacketID;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Marc on 12/3/16
 */
public class PacketPlayerListInfo extends BasePacket {
    private List<Player> players;

    public PacketPlayerListInfo() {
        this(new ArrayList<>());
    }

    public PacketPlayerListInfo(List<Player> players) {
        this.id = PacketID.Bungee.PLAYERLISTINFO.getID();
        this.players = players;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public PacketPlayerListInfo fromJSON(JsonObject obj) {
        this.id = obj.get("id").getAsInt();
        JsonArray list = obj.get("players").getAsJsonArray();
        for (JsonElement e : list) {
            Player p = fromString(e.toString());
            this.players.add(p);
        }
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            Gson gson = new Gson();
            List<String> list = new ArrayList<>();
            for (Player p : players) {
                list.add(p.toString());
            }
            obj.add("players", gson.toJsonTree(list).getAsJsonArray());
        } catch (Exception e) {
            return null;
        }
        return obj;
    }

    public Player fromString(String s) {
        Player p = new Player();
        String sr = s.replace("\"Player{", "").replace("}\"", "");
        String[] list = sr.split(",");
        for (String st : list) {
            String[] list2 = st.split("=");
            String next = "";
            boolean first = true;
            for (String str : list2) {
                if (first) {
                    next = str;
                    first = false;
                } else {
                    switch (next.toLowerCase()) {
                        case "uuid":
                            try {
                                p.setUuid(UUID.fromString(str));
                            } catch (Exception ignored) {
                            }
                            break;
                        case "username":
                            p.setUsername(str);
                            break;
                        case "address":
                            p.setAddress(str);
                            break;
                        case "server":
                            p.setServer(str);
                            break;
                        case "rank":
                            p.setRank(str);
                            break;
                        case "tags":
                            p.setTags(str);
                            break;
                        case "mcversion":
                            p.setMcVersion(Integer.parseInt(str));
                            break;
                    }
                }
            }
        }
        return p;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Player {
        private UUID uuid;
        private String username;
        private String address;
        private String server;
        private String rank;
        private String tags;
        private int mcVersion;

        @Override
        public String toString() {
            return "Player{uuid=" + uuid.toString() + ",username=" + username + ",address=" + address + ",server=" +
                    server + ",rank=" + rank + ",tags=" + tags + ",mcversion=" + mcVersion + "}";
        }
    }
}