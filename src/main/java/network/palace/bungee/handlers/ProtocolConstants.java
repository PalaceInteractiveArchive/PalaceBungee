package network.palace.bungee.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.palace.bungee.PalaceBungee;

public class ProtocolConstants {
    public static final MinecraftVersion MINECRAFT_1_8 = new MinecraftVersion(47, "1.8");
    public static final MinecraftVersion MINECRAFT_1_9 = new MinecraftVersion(107, "1.9");
    public static final MinecraftVersion MINECRAFT_1_9_1 = new MinecraftVersion(108, "1.9.1");
    public static final MinecraftVersion MINECRAFT_1_9_2 = new MinecraftVersion(109, "1.9.2");
    public static final MinecraftVersion MINECRAFT_1_9_4 = new MinecraftVersion(110, "1.9.4");
    public static final MinecraftVersion MINECRAFT_1_10 = new MinecraftVersion(210, "1.10");
    public static final MinecraftVersion MINECRAFT_1_11 = new MinecraftVersion(315, "1.11");
    public static final MinecraftVersion MINECRAFT_1_11_1 = new MinecraftVersion(316, "1.11.2");
    public static final MinecraftVersion MINECRAFT_1_12 = new MinecraftVersion(335, "1.12");
    public static final MinecraftVersion MINECRAFT_1_12_1 = new MinecraftVersion(338, "1.12.1");
    public static final MinecraftVersion MINECRAFT_1_12_2 = new MinecraftVersion(340, "1.12.2");
    public static final MinecraftVersion MINECRAFT_1_13 = new MinecraftVersion(393, "1.13");
    public static final MinecraftVersion MINECRAFT_1_13_1 = new MinecraftVersion(404, "1.13.1");
    public static final MinecraftVersion MINECRAFT_1_13_2 = new MinecraftVersion(404, "1.13.2");

    public static MinecraftVersion LOWEST_VERSION = new MinecraftVersion(-1, "0");

    public static MinecraftVersion HIGHEST_VERSION = new MinecraftVersion(-1, "0");

    private static String VERSION_STRING = null;

    public static String getVersionString() {
        if (VERSION_STRING == null || VERSION_STRING.isEmpty()) {
            VERSION_STRING = "Minecraft " + LOWEST_VERSION.getVersionName() + " - " + HIGHEST_VERSION.getVersionName();
        }
        return VERSION_STRING;
    }

    public static void setLowVersion(int protocolId, String versionName) {
        LOWEST_VERSION = new MinecraftVersion(protocolId, versionName);
        VERSION_STRING = null;
        PalaceBungee.getInstance().getProxy().getLogger().info("Set lowest version to " + LOWEST_VERSION.toString());
    }

    public static void setHighVersion(int protocolId, String versionName) {
        HIGHEST_VERSION = new MinecraftVersion(protocolId, versionName);
        VERSION_STRING = null;
        PalaceBungee.getInstance().getProxy().getLogger().info("Set highest version to " + HIGHEST_VERSION.toString());
    }

    @Getter
    @AllArgsConstructor
    public static class MinecraftVersion {
        private final int protocolId;
        private final String versionName;

        @Override
        public String toString() {
            return protocolId + ":" + versionName;
        }
    }
}
