package it.shottydeveloper.superclans.core;

import it.shottydeveloper.superclans.SuperClans;
import it.shottydeveloper.superclans.database.repository.ClanRepository;
import it.shottydeveloper.superclans.database.repository.MemberRepository;
import it.shottydeveloper.superclans.database.repository.TerritoryRepository;
import it.shottydeveloper.superclans.model.Clan;
import it.shottydeveloper.superclans.model.ClanMember;
import it.shottydeveloper.superclans.model.ClanRole;
import it.shottydeveloper.superclans.model.ClanTerritory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClanManager {

    private final SuperClans plugin;

    private final ClanRepository clanRepository;
    private final MemberRepository memberRepository;
    private final TerritoryRepository territoryRepository;

    private final Map<UUID, Clan> clansByUuid = new ConcurrentHashMap<>();
    private final Map<String, Clan> clansByName = new ConcurrentHashMap<>();
    private final Map<String, Clan> clansByTag = new ConcurrentHashMap<>();
    private final Map<UUID, Clan> playerToClan = new ConcurrentHashMap<>();

    public ClanManager(SuperClans plugin) {
        this.plugin = plugin;
        this.clanRepository = new ClanRepository(plugin.getDatabaseManager());
        this.memberRepository = new MemberRepository(plugin.getDatabaseManager());
        this.territoryRepository = new TerritoryRepository(plugin.getDatabaseManager());

        loadAllData();
    }

    private void loadAllData() {
        plugin.getLogger().info("Caricamento dati in memoria...");

        List<Clan> allClans = clanRepository.findAll();
        for (Clan clan : allClans) {
            indexClan(clan);
        }

        List<ClanMember> allMembers = memberRepository.findAll();
        for (ClanMember member : allMembers) {
            Clan clan = clansByUuid.get(member.getClanId());
            if (clan != null) {
                clan.addMember(member);
                playerToClan.put(member.getPlayerUuid(), clan);
            } else {
                plugin.getLogger().warning("Membro orfano trovato: " + member.getPlayerName() +
                        " (clan UUID: " + member.getClanId() + ")");
                memberRepository.deleteMember(member.getPlayerUuid());
            }
        }

        List<ClanTerritory> allTerritories = territoryRepository.findAll();
        for (ClanTerritory territory : allTerritories) {
            Clan clan = clansByUuid.get(territory.getClanId());
            if (clan != null) {
                clan.addTerritory(territory);
            } else {
                territoryRepository.deleteByChunk(
                        territory.getWorldName(), territory.getChunkX(), territory.getChunkZ()
                );
            }
        }

        plugin.getLogger().info("Caricati " + allClans.size() + " clan, " +
                allMembers.size() + " membri, " + allTerritories.size() + " territori.");
    }

    private void indexClan(Clan clan) {
        clansByUuid.put(clan.getClanId(), clan);
        clansByName.put(clan.getName().toLowerCase(), clan);
        clansByTag.put(clan.getTag().toLowerCase(), clan);
    }

    private void deindexClan(Clan clan) {
        clansByUuid.remove(clan.getClanId());
        clansByName.remove(clan.getName().toLowerCase());
        clansByTag.remove(clan.getTag().toLowerCase());

        for (ClanMember member : clan.getAllMembers()) {
            playerToClan.remove(member.getPlayerUuid());
        }
    }

    public Clan createClan(Player leaderPlayer, String name, String tag) {
        UUID clanId = UUID.randomUUID();
        Clan clan = new Clan(clanId, name, tag, leaderPlayer.getUniqueId());

        ClanMember leaderMember = new ClanMember(
                leaderPlayer.getUniqueId(),
                leaderPlayer.getName(),
                clanId,
                ClanRole.LEADER
        );

        if (!clanRepository.saveClan(clan)) {
            plugin.getLogger().severe("Impossibile salvare il clan nel database!");
            return null;
        }

        if (!memberRepository.saveMember(leaderMember)) {
            plugin.getLogger().severe("Impossibile salvare il leader nel database!");
            clanRepository.deleteClan(clanId);
            return null;
        }

        clan.addMember(leaderMember);
        indexClan(clan);
        playerToClan.put(leaderPlayer.getUniqueId(), clan);

        return clan;
    }

    public void disbandClan(Clan clan) {
        deindexClan(clan);

        clanRepository.deleteClan(clan.getClanId());

        if (plugin.isWorldGuardEnabled()) {
            for (ClanTerritory territory : clan.getAllTerritories()) {
                if (territory.getWorldGuardRegionName() != null) {
                    plugin.getWorldGuardHook().removeRegion(
                            territory.getWorldName(),
                            territory.getWorldGuardRegionName()
                    );
                }
            }
        }
    }

    public boolean addMember(Player player, Clan clan) {
        ClanMember newMember = new ClanMember(
                player.getUniqueId(),
                player.getName(),
                clan.getClanId(),
                ClanRole.MEMBER
        );

        if (!memberRepository.saveMember(newMember)) {
            return false;
        }

        clan.addMember(newMember);
        playerToClan.put(player.getUniqueId(), clan);

        if (plugin.isWorldGuardEnabled() && plugin.getWorldGuardHook() != null) {
            for (ClanTerritory territory : clan.getAllTerritories()) {
                if (territory.getWorldGuardRegionName() != null) {
                    plugin.getWorldGuardHook().addMemberToRegion(
                            territory.getWorldName(),
                            territory.getWorldGuardRegionName(),
                            player.getUniqueId()
                    );
                }
            }
        }

        return true;
    }

    public boolean removeMember(UUID playerUuid, Clan clan) {
        if (!memberRepository.deleteMember(playerUuid)) {
            return false;
        }

        clan.removeMember(playerUuid);
        playerToClan.remove(playerUuid);
        return true;
    }

    public boolean setMemberRole(ClanMember member, ClanRole newRole) {
        if (!memberRepository.updateMemberRole(member.getPlayerUuid(), newRole)) {
            return false;
        }
        member.setRole(newRole);
        return true;
    }

    public Clan getClanByPlayer(UUID playerUuid) {
        return playerToClan.get(playerUuid);
    }

    public Clan getClanByName(String name) {
        return clansByName.get(name.toLowerCase());
    }

    public Clan getClanByTag(String tag) {
        return clansByTag.get(tag.toLowerCase());
    }

    public Clan getClanById(UUID clanId) {
        return clansByUuid.get(clanId);
    }

    public boolean isClanNameTaken(String name) {
        return clansByName.containsKey(name.toLowerCase());
    }

    public boolean isClanTagTaken(String tag) {
        return clansByTag.containsKey(tag.toLowerCase());
    }

    public boolean isInClan(UUID playerUuid) {
        return playerToClan.containsKey(playerUuid);
    }

    public Collection<Clan> getAllClans() {
        return Collections.unmodifiableCollection(clansByUuid.values());
    }

    public void updatePlayerName(Player player) {
        Clan clan = getClanByPlayer(player.getUniqueId());
        if (clan == null) return;

        ClanMember member = clan.getMember(player.getUniqueId());
        if (member == null) return;

        if (!member.getPlayerName().equals(player.getName())) {
            member.setPlayerName(player.getName());
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                    memberRepository.updatePlayerName(player.getUniqueId(), player.getName())
            );
        }
    }

    public void saveAll() {
        for (Clan clan : clansByUuid.values()) {
            clanRepository.updateClan(clan);
        }
        plugin.getLogger().info("Dati salvati nel database.");
    }

    public ClanRepository getClanRepository() {
        return clanRepository;
    }

    public MemberRepository getMemberRepository() {
        return memberRepository;
    }

    public TerritoryRepository getTerritoryRepository() {
        return territoryRepository;
    }
}