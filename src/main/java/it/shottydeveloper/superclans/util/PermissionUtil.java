package it.shottydeveloper.superclans.util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PermissionUtil {

    private static final String BASE = "superclans.";

    private PermissionUtil() {}

    public static boolean has(CommandSender sender, String permission) {
        return sender.hasPermission(BASE + permission) || sender.hasPermission(BASE + "admin");
    }

    public static boolean isAdmin(Player player) {
        return player.hasPermission(BASE + "admin") || player.isOp();
    }


    public static boolean canCreate(Player player) {
        return has(player, "clan.create");
    }

    public static boolean canDisband(Player player) {
        return has(player, "clan.disband");
    }

    public static boolean canInvite(Player player) {
        return has(player, "clan.invite");
    }

    public static boolean canKick(Player player) {
        return has(player, "clan.kick");
    }

    public static boolean canPromote(Player player) {
        return has(player, "clan.promote");
    }

    public static boolean canDemote(Player player) {
        return has(player, "clan.demote");
    }

    public static boolean canChat(Player player) {
        return has(player, "clan.chat");
    }

    public static boolean canClaim(Player player) {
        return has(player, "clan.claim");
    }

    public static boolean canUnclaim(Player player) {
        return has(player, "clan.unclaim");
    }

    public static boolean canHome(Player player) {
        return has(player, "clan.home");
    }

    public static boolean canSetHome(Player player) {
        return has(player, "clan.sethome");
    }

    public static boolean canInfo(Player player) {
        return has(player, "clan.info");
    }
}