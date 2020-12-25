package network.palace.bungee.party;

import com.google.common.collect.ImmutableMap;
import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.handlers.Player;

import java.util.Map;
import java.util.UUID;

public class PartyUtil {
    public void inviteToParty(Player player) {
    }

    public void removeFromParty(Player player) {
    }

    public void acceptRequest(Player player) {
    }

    public void denyRequest(Player player) {
    }

    public void closeParty(Player player) {
    }

    public void leaveParty(Player player) {
    }

    public void listParty(Player player) {
        Party party = getParty(player);
        if (party == null) return;
        ImmutableMap<UUID, String> members = party.getMemberMap();
        StringBuilder sb = new StringBuilder();

        int i = 0;
        for (Map.Entry<UUID, String> entry : members.entrySet()) {
            UUID uuid = entry.getKey();
            String name = entry.getValue();
            if (uuid.equals(party.getLeader())) {
                sb.append("*");
            }
            sb.append(name);
            if (i < members.size() - 1) {
                sb.append(", ");
            }
        }
        player.sendMessage(Party.MESSAGE_BARS);
        player.sendMessage(ChatColor.YELLOW + "Members of your Party: " + sb.toString());
        player.sendMessage(Party.MESSAGE_BARS);
    }

    public void warpParty(Player player) {
    }

    public void promoteToLeader(Player player) {
    }

    public void chat(Player player) {
    }

    public Party getParty(Player player) {
        return null;
    }
}
