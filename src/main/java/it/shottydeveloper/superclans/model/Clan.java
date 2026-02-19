package it.shottydeveloper.superclans.model;

import org.bukkit.Location;

import java.util.*;

public class Clan {

    private final UUID clanId;

    private String name;

    private String tag;

    private UUID leaderUuid;

    private final Map<UUID, ClanMember> members;

    private final Map<String, ClanTerritory> territories;

    private Location home;

    private final long createdAt;

    public Clan(UUID clanId, String name, String tag, UUID leaderUuid) {
        this.clanId = clanId;
        this.name = name;
        this.tag = tag;
        this.leaderUuid = leaderUuid;
        this.members = new HashMap<>();
        this.territories = new HashMap<>();
        this.home = null;
        this.createdAt = System.currentTimeMillis();
    }

    public Clan(UUID clanId, String name, String tag, UUID leaderUuid,
                Location home, long createdAt) {
        this.clanId = clanId;
        this.name = name;
        this.tag = tag;
        this.leaderUuid = leaderUuid;
        this.members = new HashMap<>();
        this.territories = new HashMap<>();
        this.home = home;
        this.createdAt = createdAt;
    }

    public void addMember(ClanMember member) {
        members.put(member.getPlayerUuid(), member);
    }

    public boolean removeMember(UUID playerUuid) {
        return members.remove(playerUuid) != null;
    }

    public ClanMember getMember(UUID playerUuid) {
        return members.get(playerUuid);
    }

    public boolean isMember(UUID playerUuid) {
        return members.containsKey(playerUuid);
    }

    public Collection<ClanMember> getAllMembers() {
        return Collections.unmodifiableCollection(members.values());
    }

    public int getMemberCount() {
        return members.size();
    }

    public ClanMember getLeaderMember() {
        return members.get(leaderUuid);
    }

    public void addTerritory(ClanTerritory territory) {
        territories.put(territory.getChunkKey(), territory);
    }

    public ClanTerritory removeTerritory(String chunkKey) {
        return territories.remove(chunkKey);
    }

    public boolean hasTerritory(String chunkKey) {
        return territories.containsKey(chunkKey);
    }

    public ClanTerritory getTerritory(String chunkKey) {
        return territories.get(chunkKey);
    }

    public Collection<ClanTerritory> getAllTerritories() {
        return Collections.unmodifiableCollection(territories.values());
    }

    public int getTerritoryCount() {
        return territories.size();
    }

    public UUID getClanId() {
        return clanId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public UUID getLeaderUuid() {
        return leaderUuid;
    }

    public void setLeaderUuid(UUID leaderUuid) {
        this.leaderUuid = leaderUuid;
    }

    public Location getHome() {
        return home;
    }

    public void setHome(Location home) {
        this.home = home;
    }

    public boolean hasHome() {
        return home != null;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Clan{" +
                "name='" + name + '\'' +
                ", tag='" + tag + '\'' +
                ", members=" + members.size() +
                ", territories=" + territories.size() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Clan other)) return false;
        return clanId.equals(other.clanId);
    }

    @Override
    public int hashCode() {
        return clanId.hashCode();
    }
}