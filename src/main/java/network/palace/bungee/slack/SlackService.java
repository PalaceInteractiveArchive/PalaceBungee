package network.palace.bungee.slack;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlackService {

    private final HttpRequestFactory requestFactory;

    public SlackService(Proxy proxy) {
        NetHttpTransport.Builder builder = new NetHttpTransport.Builder();
        builder.setProxy(proxy);
        requestFactory = builder.build().createRequestFactory();
    }

    public SlackService() {
        this(null);
    }

    public void push(String webHookUrl, SlackMessage text, List<SlackAttachment> attachments) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        if (!attachments.isEmpty()) {
            payload.put("attachments", attachments);
        }
        payload.put("text", text.toString());
        execute(webHookUrl, payload);
    }

    public void push(String webHookUrl, SlackMessage text) throws IOException {
        push(webHookUrl, text, new ArrayList<>());
    }

    public void execute(String webHookUrl, Map<String, Object> payload) throws IOException {
        String jsonEncodedMessage = new Gson().toJson(payload);
        HashMap<Object, Object> payloadToSend = Maps.newHashMap();
        payloadToSend.put("payload", jsonEncodedMessage);

        requestFactory.buildPostRequest(new GenericUrl(webHookUrl), new UrlEncodedContent(payloadToSend))
                .execute();
    }
}