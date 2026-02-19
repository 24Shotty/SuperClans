package it.shottydeveloper.superclans.model;

import java.util.UUID;

public class ClanMember {

    private final UUID playerUuid;

    private String playerName;

    private final UUID clanId;

    private ClanRole role;

    private final long joinedAt;

    public ClanMember(UUID playerUuid, String playerName, UUID clanId, ClanRole role) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.clanId = clanId;
        this.role = role;
        this.joinedAt = System.currentTimeMillis();
    }

    public ClanMember(UUID playerUuid, String playerName, UUID clanId, ClanRole role, long joinedAt) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.clanId = clanId;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public UUID getClanId() {
        return clanId;
    }

    public ClanRole getRole() {
        return role;
    }

    public void setRole(ClanRole role) {
        this.role = role;
    }

    public long getJoinedAt() {
        return joinedAt;
    }

    public boolean isLeader() {
        return role == ClanRole.LEADER;
    }

    public boolean isOfficerOrHigher() {
        return role.isAtLeast(ClanRole.OFFICER);
    }

    @Override
    public String toString() {
        return "ClanMember{" +
                "playerName='" + playerName + '\'' +
                ", role=" + role +
                ", clanId=" + clanId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClanMember other)) return false;
        return playerUuid.equals(other.playerUuid);
    }

    @Override
    public int hashCode() {
        return playerUuid.hashCode();
    }
}