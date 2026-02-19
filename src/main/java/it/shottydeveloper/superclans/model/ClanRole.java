package it.shottydeveloper.superclans.model;
public enum ClanRole {
    MEMBER("Member", 1),

    OFFICER("Officer", 2),

    LEADER("Leader", 3);

    private final String displayName;

    private final int level;

    ClanRole(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLevel() {
        return level;
    }

    public boolean isHigherThan(ClanRole other) {
        return this.level > other.level;
    }

    public boolean isAtLeast(ClanRole other) {
        return this.level >= other.level;
    }

    public ClanRole getNextRole() {
        return switch (this) {
            case MEMBER -> OFFICER;
            case OFFICER -> LEADER;
            case LEADER -> null;
        };
    }

    public ClanRole getPreviousRole() {
        return switch (this) {
            case LEADER -> OFFICER;
            case OFFICER -> MEMBER;
            case MEMBER -> null;
        };
    }

    public static ClanRole fromString(String name) {
        if (name == null) return null;
        for (ClanRole role : values()) {
            if (role.name().equalsIgnoreCase(name) || role.displayName.equalsIgnoreCase(name)) {
                return role;
            }
        }
        return null;
    }
}