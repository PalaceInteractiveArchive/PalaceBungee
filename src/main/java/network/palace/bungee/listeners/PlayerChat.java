package network.palace.bungee.listeners;

import com.google.gson.JsonObject;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

public class PlayerChat implements Listener {
    // create a client
    private final HttpClient client = HttpClient.newHttpClient();

    @EventHandler
    public void onChat(ChatEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer)) return;
        Player player = PalaceBungee.getPlayer(((ProxiedPlayer) event.getSender()).getUniqueId());
        if (player == null) {
            event.setCancelled(true);
            return;
        }
        if (event.isProxyCommand()) return;
        if (event.isCommand()) return;
        String msg = event.getMessage();
    }

    public void analyzeMessage(UUID uuid, Rank rank, String message, String server) {
        JsonObject obj = new JsonObject();
        obj.addProperty("uuid", uuid.toString());
        obj.addProperty("rank", rank.getDBName());
        obj.addProperty("message", message);
        obj.addProperty("server", server);
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(obj.toString());
        HttpRequest request = HttpRequest.newBuilder(
                URI.create("https://internal-api.palace.network/minecraft/chat/analyze"))
                .header("accept", "application/json").POST(bodyPublisher)
                .build();

        var responseFuture = client.sendAsync(request, new HttpResponse.BodyHandler<Object>() {
            @Override
            public HttpResponse.BodySubscriber<Object> apply(HttpResponse.ResponseInfo responseInfo) {
                return null;
            }
        });
//
//// We can do other things here while the request is in-flight
//
//// This blocks until the request is complete
//        var response = responseFuture.get();
//
//// the response:
//        System.out.println(response.body().get().title);
    }
}
