package it.shottydeveloper.superclans.command.subcommands;

import it.shottydeveloper.superclans.SuperClans;
import it.shottydeveloper.superclans.command.ClanCommand;
import it.shottydeveloper.superclans.model.Clan;
import it.shottydeveloper.superclans.model.ClanMember;
import it.shottydeveloper.superclans.model.ClanTerritory;
import it.shottydeveloper.superclans.util.PermissionUtil;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ClaimCommand implements ClanCommand.SubCommand {

    private final SuperClans plugin;

    public ClaimCommand(SuperClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String subCommand, String[] args) {
        if (!PermissionUtil.canClaim(player)) {
            player.sendMessage(plugin.getMessagesConfig().get("general.no-permission"));
            return;
        }

        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(plugin.getMessagesConfig().get("general.must-be-in-clan"));
            return;
        }

        ClanMember member = clan.getMember(player.getUniqueId());
        if (member == null || !member.isOfficerOrHigher()) {
            player.sendMessage(plugin.getMessagesConfig().get("general.must-be-officer-or-higher"));
            return;
        }

        var result = plugin.getTerritoryManager().claimArea(player, clan);

        if (result.failure() == it.shottydeveloper.superclans.core.TerritoryManager.ClaimAreaFailure.MAX_CLAIMS) {
            int maxClaims = plugin.getSettingsConfig().getMaxClaims();
            player.sendMessage(plugin.getMessagesConfig().get("claim.max-claims-reached",
                    "max", String.valueOf(maxClaims)));
            return;
        }

        if (result.failure() == it.shottydeveloper.superclans.core.TerritoryManager.ClaimAreaFailure.CLAIMED_BY_OTHER) {
            Clan otherClan = result.conflictClanId() != null
                    ? plugin.getClanManager().getClanById(result.conflictClanId())
                    : null;
            String otherClanName = otherClan != null ? otherClan.getName() : "Sconosciuto";
            player.sendMessage(plugin.getMessagesConfig().get("claim.already-claimed-other",
                    "clan", otherClanName));
            return;
        }

        if (result.failure() == it.shottydeveloper.superclans.core.TerritoryManager.ClaimAreaFailure.ERROR) {
            player.sendMessage(plugin.getMessagesConfig().get("general.error-occurred"));
            return;
        }

        if (result.claimed() <= 0) {
            player.sendMessage(plugin.getMessagesConfig().get("claim.already-claimed-own"));
            return;
        }

        if (result.claimed() == 1) {
            player.sendMessage(plugin.getMessagesConfig().get("claim.success", "clan", clan.getName()));
        } else {
            player.sendMessage(plugin.getMessagesConfig().get("claim.success-multi",
                    "clan", clan.getName(),
                    "count", String.valueOf(result.claimed())));
        }

        plugin.getChatManager().broadcastToClan(clan,
                plugin.getMessagesConfig().getRaw("claim.broadcast",
                        "player", player.getName(),
                        "count", String.valueOf(result.claimed())));
    }

    @Override
    public List<String> tabComplete(Player player, String subCommand, String[] args) {
        return Collections.emptyList();
    }
}