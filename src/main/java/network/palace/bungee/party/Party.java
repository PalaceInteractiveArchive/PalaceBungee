package network.palace.bungee.party;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCallback;
import network.palace.bungee.messages.packets.MessagePacket;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Party {
    public static final String MESSAGE_BARS = ChatColor.GOLD + "-----------------------------------------------------";

    @Getter private final String partyID;
    @Getter private UUID leader;
    private final HashMap<UUID, String> members;
    private final HashMap<UUID, Long> invited;

    public Party(String partyID, UUID leader, HashMap<UUID, String> members, HashMap<UUID, Long> invited) {
        this.partyID = partyID;
        this.leader = leader;
        this.members = members;
        this.invited = invited;
    }

    public Party(Document doc) throws Exception {
        this.partyID = doc.getObjectId("_id").toHexString();
        this.leader = UUID.fromString(doc.getString("leader"));
        this.members = new HashMap<>();
        this.invited = new HashMap<>();
        for (Object o : doc.get("members", ArrayList.class)) {
            UUID uuid = UUID.fromString((String) o);
            members.put(uuid, PalaceBungee.getUsername(uuid));
        }
        for (Object o : doc.get("invited", ArrayList.class)) {
            Document d = (Document) o;
            invited.put(UUID.fromString(d.getString("uuid")), d.getLong("expires"));
        }
    }

    public List<UUID> getMembers() {
        return new ArrayList<>(members.keySet());
    }

    public ImmutableMap<UUID, String> getMemberMap() {
        return ImmutableMap.copyOf(members);
    }

    public boolean isLeader(UUID uuid) {
        return leader.equals(uuid);
    }

    public void addMember(UUID uuid, String username) {
        members.put(uuid, username);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public boolean isMember(UUID uuid) {
        return members.containsKey(uuid);
    }

    public void addInvite(UUID uuid, long expires) {
        invited.put(uuid, expires);
    }

    public void removeInvite(UUID uuid) {
        invited.remove(uuid);
    }

    public boolean isInvited(UUID uuid) {
        return invited.containsKey(uuid);
    }

    public void messageMember(UUID uuid, String message, boolean bars) throws Exception {
        if (bars) message = MESSAGE_BARS + "\n" + message + "\n" + MESSAGE_BARS;
        PalaceBungee.getMessageHandler().sendMessageToPlayer(uuid, message);
    }

    public void messageAllMembers(String message, boolean bars) throws Exception {
        if (bars) message = MESSAGE_BARS + "\n" + message + "\n" + MESSAGE_BARS;
        PalaceBungee.getMessageHandler().sendMessage(new MessagePacket(message, getMembers()), PalaceBungee.getMessageHandler().ALL_PROXIES);
    }

    public void forAllMembers(PalaceCallback.UUIDCallback callback) {
        for (UUID uuid : getMembers()) {
            callback.run(uuid);
        }
    }
}