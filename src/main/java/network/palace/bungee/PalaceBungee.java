package network.palace.bungee;

import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;
import network.palace.bungee.handlers.ProtocolConstants;
import network.palace.bungee.listeners.ProxyPing;
import network.palace.bungee.utils.ConfigUtil;

import java.io.IOException;

public class PalaceBungee extends Plugin {
    @Getter private static PalaceBungee instance;
    @Getter private static ConfigUtil configUtil;

    @Override
    public void onEnable() {
        instance = this;

        ProtocolConstants.setHighVersion(753, "1.16.3");
        ProtocolConstants.setLowVersion(735, "1.16");

        try {
            configUtil = new ConfigUtil();
        } catch (IOException e) {
            e.printStackTrace();
        }

        registerListeners();
    }

    @Override
    public void onDisable() {
    }

    private void registerListeners() {
        getProxy().getPluginManager().registerListener(this, new ProxyPing());
    }
}
