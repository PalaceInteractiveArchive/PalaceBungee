package network.palace.bungee.utils;


import network.palace.bungee.PalaceBungee;
import network.palace.bungee.slack.SlackAttachment;
import network.palace.bungee.slack.SlackMessage;
import network.palace.bungee.slack.SlackService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class SlackUtil {
    public SlackService s = new SlackService();

    public void sendDashboardMessage(SlackMessage msg) {
        sendDashboardMessage(msg, new ArrayList<>());
    }

    public void sendDashboardMessage(SlackMessage msg, List<SlackAttachment> attachments) {
        sendDashboardMessage(msg, attachments, true);
    }

    public void sendDashboardMessage(SlackMessage msg, List<SlackAttachment> attachments, boolean status) {
        if (PalaceBungee.isTestNetwork()) {
            StringBuilder attch = new StringBuilder();
            for (SlackAttachment a : attachments) {
                attch.append(a.getText()).append(" ");
            }
            PalaceBungee.getProxyServer().getLogger().log(Level.CONFIG, msg.toString(), attch.toString());
            return;
        }
        for (SlackAttachment a : attachments) {
            a.addMarkdownIn("text");
        }
        String webhook;
        if (status) {
            webhook = "https://hooks.slack.com/services/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";
        } else {
            webhook = "https://hooks.slack.com/services/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";
        }
        try {
            s.push(webhook, msg, attachments);
        } catch (IOException e) {
            PalaceBungee.getProxyServer().getLogger().log(Level.SEVERE, "Error sending slack message", e);
        }
    }
}