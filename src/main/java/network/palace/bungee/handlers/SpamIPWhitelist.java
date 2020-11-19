package network.palace.bungee.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SpamIPWhitelist {
    private String address;
    private int limit;
}