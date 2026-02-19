package it.shottydeveloper.superclans.hook;

import it.shottydeveloper.superclans.SuperClans;
import it.shottydeveloper.superclans.model.Clan;
import it.shottydeveloper.superclans.model.ClanMember;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceHolderHook extends PlaceholderExpansion {

    private final SuperClans plugin;

    public PlaceHolderHook(SuperClans plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "clans";
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "ShottyDeveloper";
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());

        if (params.equals("player_clan")) {
            return clan != null ? clan.getName() : "";
        }

        if (params.equals("player_tag")) {
            return clan != null ? clan.getTag() : "";
        }

        if (params.equals("player_role")) {
            if (clan == null) return "";
            ClanMember member = clan.getMember(player.getUniqueId());
            return member != null ? member.getRole().getDisplayName() : "";
        }

        if (params.equals("clan_members_online")) {
            if (clan == null) return "0";
            long onlineCount = clan.getAllMembers().stream()
                    .filter(m -> plugin.getServer().getPlayer(m.getPlayerUuid()) != null)
                    .count();
            return String.valueOf(onlineCount);
        }

        if (params.equals("clan_members_total")) {
            return clan != null ? String.valueOf(clan.getMemberCount()) : "0";
        }

        if (params.equals("clan_territories")) {
            return clan != null ? String.valueOf(clan.getTerritoryCount()) : "0";
        }

        if (params.equals("has_clan")) {
            return clan != null ? "true" : "false";
        }

        return null;
    }
}