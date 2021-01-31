package network.palace.bungee.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

@AllArgsConstructor
public enum Rank {
    OWNER("Owner", ChatColor.RED + "Owner ", ChatColor.RED, ChatColor.YELLOW, true, 13),
    DIRECTOR("Director", ChatColor.RED + "Director ", ChatColor.RED, ChatColor.YELLOW, true, 13),
    MANAGER("Manager", ChatColor.RED + "Manager ", ChatColor.RED, ChatColor.YELLOW, true, 13),
    LEAD("Lead", ChatColor.GOLD + "Lead ", ChatColor.GOLD, ChatColor.YELLOW, true, 13),
    DEVELOPER("Developer", ChatColor.BLUE + "Developer ", ChatColor.BLUE, ChatColor.AQUA, true, 13),
    COORDINATOR("Coordinator", ChatColor.YELLOW + "Coordinator ", ChatColor.YELLOW, ChatColor.GREEN, true, 12),
    BUILDER("Builder", ChatColor.BLUE + "Builder ", ChatColor.BLUE, ChatColor.AQUA, true, 11),
    TECHNICIAN("Technician", ChatColor.BLUE + "Technician ", ChatColor.BLUE, ChatColor.AQUA, true, 11),
    MEDIA("Media", ChatColor.BLUE + "Media ", ChatColor.BLUE, ChatColor.AQUA, true, 11),
    MOD("Mod", ChatColor.GREEN + "Mod ", ChatColor.GREEN, ChatColor.GREEN, true, 11),
    TRAINEETECH("Trainee", ChatColor.BLUE + "Trainee ", ChatColor.BLUE, ChatColor.AQUA, false, 10),
    TRAINEEBUILD("Trainee", ChatColor.BLUE + "Trainee ", ChatColor.BLUE, ChatColor.AQUA, false, 10),
    TRAINEE("Trainee", ChatColor.DARK_GREEN + "Trainee ", ChatColor.DARK_GREEN, ChatColor.DARK_GREEN, false, 9),
    CHARACTER("Character", ChatColor.BLUE + "Character ", ChatColor.BLUE, ChatColor.BLUE, false, 8),
    SPECIALGUEST("Special Guest", ChatColor.DARK_PURPLE + "SG ", ChatColor.DARK_PURPLE, ChatColor.WHITE, false, 7),
    SHAREHOLDER("Shareholder", ChatColor.LIGHT_PURPLE + "Shareholder ", ChatColor.LIGHT_PURPLE, ChatColor.WHITE, false, 6),
    HONORABLE("Honorable", ChatColor.LIGHT_PURPLE + "Honorable ", ChatColor.LIGHT_PURPLE, ChatColor.WHITE, false, 5),
    MAJESTIC("Majestic", ChatColor.DARK_PURPLE + "Majestic ", ChatColor.DARK_PURPLE, ChatColor.WHITE, false, 4),
    NOBLE("Noble", ChatColor.BLUE + "Noble ", ChatColor.BLUE, ChatColor.WHITE, false, 3),
    DWELLER("Dweller", ChatColor.AQUA + "Dweller ", ChatColor.AQUA, ChatColor.WHITE, false, 2),
    SETTLER("Settler", ChatColor.GRAY + "", ChatColor.DARK_AQUA, ChatColor.WHITE, false, 1);

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
        if (name == null) return SETTLER;
        if (name.equalsIgnoreCase("admin")) return LEAD;
        String rankName = name.replaceAll(" ", "");

        for (Rank rank : Rank.values()) {
            if (rank.getDBName().equalsIgnoreCase(rankName)) return rank;
        }
        return SETTLER;
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
        String bold = getRankId() >= Rank.TRAINEE.getRankId() ? "" + ChatColor.BOLD : "";
        return getTagColor() + bold + getName();
    }
}
