package network.palace.bungee.commands.moderation;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Party;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PartiesCommand extends PalaceCommand {

    public PartiesCommand() {
        super("parties", Rank.TRAINEE);
    }

    @Override
    public void execute(Player player, String[] args) {
        try {
            if (args.length != 2 || !args[0].equalsIgnoreCase("info")) {
                List<Party> parties = PalaceBungee.getMongoHandler().getParties();
                if (parties.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "There are no Parties right now!");
                    return;
                }
                player.sendMessage(ChatColor.YELLOW + "Server Parties:");
                StringBuilder msg = new StringBuilder();
                for (Party p : parties) {
                    String leader = PalaceBungee.getMongoHandler().uuidToUsername(p.getLeader());
                    if (leader == null) continue;
                    if (msg.length() > 0) msg.append("\n");
                    msg.append("- ").append(leader).append(" ").append(p.getMembers().size()).append(" Member").append(p.getMembers().size() > 1 ? "s" : "");
                }
                player.sendMessage(ChatColor.GREEN + msg.toString());
                player.sendMessage(ChatColor.YELLOW + "/parties info [Party Leader] " + ChatColor.GREEN + "- Display info on that Party");
                return;
            }
            UUID uuid = PalaceBungee.getMongoHandler().usernameToUUID(args[1]);
            if (uuid == null || !PalaceBungee.getMongoHandler().isPlayerOnline(uuid)) {
                player.sendMessage(ChatColor.RED + "Player not found!");
                return;
            }
            Party p = PalaceBungee.getMongoHandler().getPartyByMember(uuid);
            if (p == null) {
                player.sendMessage(ChatColor.RED + "This player is not in a Party!");
                return;
            }
            List<UUID> members = p.getMembers();
            List<String> names = new ArrayList<>();
            for (UUID uuid2 : members) {
                String name = PalaceBungee.getMongoHandler().uuidToUsername(uuid2);
                if (name != null) names.add(name);
            }
            String leader = PalaceBungee.getMongoHandler().uuidToUsername(p.getLeader());
            if (leader == null) return;
            StringBuilder msg = new StringBuilder("Party Leader: " + leader + "\nParty Members: ");
            for (int i = 0; i < names.size(); i++) {
                msg.append(names.get(i));
                if (i < (names.size() - 1)) {
                    msg.append(", ");
                }
            }
            player.sendMessage(ChatColor.YELLOW + msg.toString());
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred while listing party info. Check console for errors.");
        }
    }
}