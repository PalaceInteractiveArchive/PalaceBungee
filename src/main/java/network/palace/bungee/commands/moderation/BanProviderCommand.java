package network.palace.bungee.commands.moderation;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.handlers.moderation.ProviderBan;
import network.palace.bungee.messages.packets.BanProviderPacket;

import java.util.logging.Level;

public class BanProviderCommand extends PalaceCommand {

    public BanProviderCommand() {
        super("banprovider", Rank.MOD);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/banprovider [Provider]");
            return;
        }
        String provider = String.join(" ", args);
        ProviderBan ban = new ProviderBan(provider, player.getUniqueId().toString());
        PalaceBungee.getProxyServer().getScheduler().runAsync(PalaceBungee.getInstance(), () -> {
            try {
                ProviderBan existing = PalaceBungee.getMongoHandler().getProviderBan(provider);
                if (existing != null) {
                    player.sendMessage(ChatColor.RED + "This provider is already banned!");
                    return;
                }
                PalaceBungee.getMongoHandler().banProvider(ban);
                PalaceBungee.getMessageHandler().sendMessage(new BanProviderPacket(ban.getProvider()), PalaceBungee.getMessageHandler().ALL_PROXIES);
                PalaceBungee.getModerationUtil().announceBan(ban);
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "An error occurred while banning that ISP. Check console for errors.");
                PalaceBungee.getProxyServer().getLogger().log(Level.SEVERE, "Error processing provider ban", e);
            }
        });
    }
}