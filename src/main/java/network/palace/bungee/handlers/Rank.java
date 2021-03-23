package network.palace.bungee.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

@AllArgsConstructor
public enum Rank {
    OWNER("Owner", ChatColor.RED + "Owner ", ChatColor.RED, ChatColor.YELLOW, true, 13),
    EXEC("Executive", ChatColor.RED + "Director ", ChatColor.RED, ChatColor.YELLOW, true, 13),
    MANAGER("Manager", ChatColor.GOLD + "Manager ", ChatColor.GOLD, ChatColor.YELLOW, true, 13),
    LEAD("Lead", ChatColor.GREEN + "Lead ", ChatColor.DARK_GREEN, ChatColor.GREEN, true, 13),
    DEVELOPER("Developer", ChatColor.BLUE + "Developer ", ChatColor.BLUE, ChatColor.AQUA, true, 13),
    COORDINATOR("Coordinator", ChatColor.BLUE + "Coordinator ", ChatColor.BLUE, ChatColor.AQUA, true, 12),
    BUILDER("Imagineer", ChatColor.AQUA + "Imagineer ", ChatColor.AQUA, ChatColor.AQUA, true, 11),
    IMAGINEER("Imagineer", ChatColor.AQUA + "Imagineer ", ChatColor.AQUA, ChatColor.AQUA, true, 11),
    MEDIA("Media", ChatColor.BLUE + "Media ", ChatColor.BLUE, ChatColor.AQUA, true, 11),
    CM("Cast Member", ChatColor.AQUA + "CM ", ChatColor.AQUA, ChatColor.AQUA, true, 11),
    TRAINEETECH("Trainee", ChatColor.AQUA + "Trainee ", ChatColor.AQUA, ChatColor.AQUA, false, 10),
    TRAINEEBUILD("Trainee", ChatColor.AQUA + "Trainee ", ChatColor.AQUA, ChatColor.AQUA, false, 10),
    TRAINEE("Trainee", ChatColor.DARK_GREEN + "Trainee ", ChatColor.DARK_GREEN, ChatColor.DARK_GREEN, false, 9),
    CHARACTER("Character", ChatColor.DARK_PURPLE + "Character ", ChatColor.DARK_PURPLE, ChatColor.DARK_PURPLE, false, 8),
    INFLUENCER("Influencer", ChatColor.DARK_PURPLE + "Influencer ", ChatColor.DARK_PURPLE, ChatColor.WHITE, false, 7),
    VIP("VIP", ChatColor.DARK_PURPLE + "VIP ", ChatColor.DARK_PURPLE, ChatColor.WHITE, false, 7),
    SHAREHOLDER("Shareholder", ChatColor.LIGHT_PURPLE + "Shareholder ", ChatColor.LIGHT_PURPLE, ChatColor.WHITE, false, 6),
    CLUB("Club 33", ChatColor.DARK_RED + "C33 ", ChatColor.DARK_RED, ChatColor.WHITE, false, 5),
    DVC("DVC", ChatColor.GOLD + "DVC ", ChatColor.GOLD, ChatColor.WHITE, false, 4),
    PASSPORT("Premier Passport", ChatColor.YELLOW + "Premier ", ChatColor.YELLOW, ChatColor.WHITE, false, 3),
    PASSHOLDER("Passholder", ChatColor.DARK_AQUA + "Passholder ", ChatColor.DARK_AQUA, ChatColor.WHITE, false, 2),
    GUEST("Guest", ChatColor.GRAY + "", ChatColor.GRAY, ChatColor.GRAY, false, 1);

    @Getter private final String name;
    @Getter private final String scoreboardName;
    @Getter private final ChatColor tagColor;
    @Getter private final ChatColor chatColor;
    @Getter private final boolean isOp;
    @Getter private final int rankId;

    /**
     * Get rank object from a string
     *
     * @param name rank name in string
     * @return rank object
     */
    public static Rank fromString(String name) {
        if (name == null) return GUEST;
        if (name.equalsIgnoreCase("admin")) return LEAD;
        String rankName = name.replaceAll(" ", "");

        for (Rank rank : Rank.values()) {
            if (rank.getDBName().equalsIgnoreCase(rankName)) return rank;
        }
        return GUEST;
    }

    public String getDBName() {
        String s;
        switch (this) {
            case TRAINEEBUILD:
            case TRAINEETECH:
                s = name().toLowerCase();
                break;
            default:
                s = name.toLowerCase().replaceAll(" ", "");
        }
        return s;
    }

    /**
     * Get the formatted name of a rank
     *
     * @return the rank name with any additional formatting that should exist
     */
    public String getFormattedName() {
        return getTagColor() + getName();
    }
}
