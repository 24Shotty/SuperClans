package it.shottydeveloper.superclans.listener;

import it.shottydeveloper.superclans.SuperClans;
import it.shottydeveloper.superclans.model.Clan;
import it.shottydeveloper.superclans.model.ClanTerritory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class TerritoryProtectionListener implements Listener {

    private final SuperClans plugin;

    public TerritoryProtectionListener(SuperClans plugin) {
        this.plugin = plugin;
    }

    private boolean canBypass(Player player) {
        return plugin.getConfigManager().isAdminBypassEnabled()
                && (player.isOp() || player.hasPermission("superclans.admin"));
    }

    private boolean isProtectedTerritory(ClanTerritory territory, Player actor) {
        if (territory == null) return false;
        Clan ownerClan = plugin.getClanManager().getClanById(territory.getClanId());
        return ownerClan == null || !ownerClan.isMember(actor.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (plugin.getSettingsConfig().shouldUseWorldGuard()) return;
        if (!plugin.getConfigManager().isProtectBlockBreak()) return;

        Player player = event.getPlayer();
        if (canBypass(player)) return;

        ClanTerritory territory = plugin.getTerritoryManager().getTerritoryAtChunk(event.getBlock().getChunk());
        if (territory == null) return;
        if (!isProtectedTerritory(territory, player)) return;

        event.setCancelled(true);
        player.sendMessage(plugin.getMessagesConfig().get("general.no-permission"));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (plugin.getSettingsConfig().shouldUseWorldGuard()) return;
        if (!plugin.getConfigManager().isProtectBlockPlace()) return;

        Player player = event.getPlayer();
        if (canBypass(player)) return;

        ClanTerritory territory = plugin.getTerritoryManager().getTerritoryAtChunk(event.getBlock().getChunk());
        if (territory == null) return;
        if (!isProtectedTerritory(territory, player)) return;

        event.setCancelled(true);
        player.sendMessage(plugin.getMessagesConfig().get("general.no-permission"));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (plugin.getSettingsConfig().shouldUseWorldGuard()) return;
        if (!plugin.getConfigManager().isProtectPvp()) return;

        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        if (canBypass(attacker)) return;

        ClanTerritory territory = plugin.getTerritoryManager().getTerritoryAt(attacker);
        if (territory == null) return;

        event.setCancelled(true);
        attacker.sendMessage(plugin.getMessagesConfig().getRaw("general.no-permission"));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (plugin.getSettingsConfig().shouldUseWorldGuard()) return;
        if (!plugin.getConfigManager().isProtectExplosions()) return;

        event.blockList().removeIf(block -> {
            ClanTerritory territory = plugin.getTerritoryManager().getTerritoryAtChunk(block.getChunk());
            return territory != null;
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (plugin.getSettingsConfig().shouldUseWorldGuard()) return;
        if (!plugin.getConfigManager().isProtectMobSpawning()) return;

        ClanTerritory territory = plugin.getTerritoryManager().getTerritoryAtChunk(event.getLocation().getChunk());
        if (territory != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        if (plugin.getSettingsConfig().shouldUseWorldGuard()) return;
        if (!plugin.getConfigManager().isProtectBlockBreak() && !plugin.getConfigManager().isProtectBlockPlace()) return;

        for (org.bukkit.block.Block block : event.getBlocks()) {
            ClanTerritory territory = plugin.getTerritoryManager().getTerritoryAtChunk(block.getChunk());
            if (territory != null) {
                event.setCancelled(true);
                return;
            }
        }
        org.bukkit.block.Block destBlock = event.getBlock().getRelative(event.getDirection());
        ClanTerritory destTerritory = plugin.getTerritoryManager().getTerritoryAtChunk(destBlock.getChunk());
        if (destTerritory != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        if (plugin.getSettingsConfig().shouldUseWorldGuard()) return;
        if (!plugin.getConfigManager().isProtectBlockBreak() && !plugin.getConfigManager().isProtectBlockPlace()) return;

        for (org.bukkit.block.Block block : event.getBlocks()) {
            ClanTerritory territory = plugin.getTerritoryManager().getTerritoryAtChunk(block.getChunk());
            if (territory != null) {
                event.setCancelled(true);
                return;
            }
        }
        org.bukkit.block.Block destBlock = event.getBlock().getRelative(event.getDirection());
        ClanTerritory destTerritory = plugin.getTerritoryManager().getTerritoryAtChunk(destBlock.getChunk());
        if (destTerritory != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        if (plugin.getSettingsConfig().shouldUseWorldGuard()) return;
        if (!plugin.getConfigManager().isProtectBlockPlace()) return;

        ClanTerritory fromTerritory = plugin.getTerritoryManager().getTerritoryAtChunk(event.getBlock().getChunk());
        ClanTerritory toTerritory = plugin.getTerritoryManager().getTerritoryAtChunk(event.getToBlock().getChunk());
        if (toTerritory == null) return;
        if (fromTerritory != null && fromTerritory.getClanId().equals(toTerritory.getClanId())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        if (plugin.getSettingsConfig().shouldUseWorldGuard()) return;
        if (!plugin.getConfigManager().isProtectFireSpread()) return;

        ClanTerritory territory = plugin.getTerritoryManager().getTerritoryAtChunk(event.getBlock().getChunk());
        if (territory != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        if (plugin.getSettingsConfig().shouldUseWorldGuard()) return;
        if (!plugin.getConfigManager().isProtectFireSpread()) return;

        ClanTerritory territory = plugin.getTerritoryManager().getTerritoryAtChunk(event.getBlock().getChunk());
        if (territory != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (plugin.getSettingsConfig().shouldUseWorldGuard()) return;
        if (!plugin.getConfigManager().isProtectBlockBreak() && !plugin.getConfigManager().isProtectBlockPlace()) return;

        ClanTerritory territory = plugin.getTerritoryManager().getTerritoryAtChunk(event.getBlock().getChunk());
        if (territory != null) {
            event.setCancelled(true);
        }
    }
}