package network.palace.bungee.commands.admin;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.Server;
import network.palace.bungee.messages.packets.SendPlayerPacket;

import java.io.IOException;
import java.util.UUID;

public class SendCommand extends PalaceCommand {

    public SendCommand() {
        super("send", Rank.DEVELOPER);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "/send [player|all|current] [Target]");
            return;
        }
        Server server = PalaceBungee.getServerUtil().getServer(args[1], true);
        if (server == null) {
            player.sendMessage(ChatColor.RED + "The server '" + args[1] + "' does not exist!");
            return;
        }
        switch (args[0]) {
            case "all": {
                player.sendMessage(ChatColor.GREEN + "Sending " + ChatColor.GOLD + "all players" + ChatColor.GREEN +
                        " to " + ChatColor.YELLOW + server.getName());
                try {
                    PalaceBungee.getMessageHandler().sendMessage(new SendPlayerPacket("all", server.getName()), PalaceBungee.getMessageHandler().ALL_PROXIES);
                } catch (IOException e) {
                    e.printStackTrace();
                    player.sendMessage(ChatColor.RED + "An error occurred while sending all players to that server! See console for details.");
                }
                return;
            }
            case "current": {
                Server currentServer = PalaceBungee.getServerUtil().getServer(player.getServerName(), true);
                if (currentServer == null) {
                    player.sendMessage(ChatColor.RED + "An error occurred while attempting to perform this command!");
                    return;
                }
                player.sendMessage(ChatColor.GREEN + "Sending " + ChatColor.GOLD + "players on " + currentServer.getName() + " (" +
                        currentServer.getCount() + " players)" + ChatColor.GREEN + " to " +
                        ChatColor.YELLOW + server.getName());
                try {
                    PalaceBungee.getMessageHandler().sendMessage(new SendPlayerPacket("Server:" + currentServer.getName(), server.getName()), PalaceBungee.getMessageHandler().ALL_PROXIES);
                } catch (IOException e) {
                    e.printStackTrace();
                    player.sendMessage(ChatColor.RED + "An error occurred while sending all players on your server to that server! See console for details.");
                }
                return;
            }
            default: {
                Player targetPlayer = PalaceBungee.getPlayer(args[0]);
                if (targetPlayer != null) {
                    player.sendMessage(ChatColor.GREEN + "Sending " + ChatColor.GOLD + targetPlayer.getUsername() + ChatColor.GREEN +
                            " to " + ChatColor.YELLOW + server.getName());
                    server.join(targetPlayer);
                } else {
                    try {
                        String target = args[0];
                        UUID targetProxy = PalaceBungee.getMongoHandler().findPlayer(target);
                        if (targetProxy == null) {
                            player.sendMessage(ChatColor.RED + "Player not found!");
                            return;
                        }
                        PalaceBungee.getMessageHandler().sendToProxy(new SendPlayerPacket(target, server.getName()), targetProxy);
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "An error occurred while sending " + args[0] + " to that server! See console for details.");
                    }
                }
            }
        }
    }
}