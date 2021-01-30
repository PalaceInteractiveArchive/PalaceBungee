package network.palace.bungee.handlers.moderation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ProviderData {
    @Getter private final String isp, country, region, regionName, timezone;

    @Override
    public String toString() {
        return "isp:" + isp + ";country:" + country + ";region:" + region + ";regionName:" + regionName +
                ";timezone:" + timezone;
    }
}
