package it.shottydeveloper.superclans.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LocationSerializer {

    private LocationSerializer() {}

    public static String serialize(Location location) {
        if (location == null || location.getWorld() == null) return null;

        return location.getWorld().getName() + ":" +
                location.getX() + ":" +
                location.getY() + ":" +
                location.getZ() + ":" +
                location.getYaw() + ":" +
                location.getPitch();
    }

    public static Location deserialize(String serialized) {
        if (serialized == null || serialized.isEmpty()) return null;

        try {
            String[] parts = serialized.split(":");
            if (parts.length < 4) return null;

            String worldName = parts[0];
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0f;
            float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0f;

            if (Bukkit.getWorld(worldName) == null) {
                return null;
            }

            return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);

        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Location fromResultSet(ResultSet rs) throws SQLException {
        String worldName = rs.getString("home_world");
        if (rs.wasNull() || worldName == null) return null;

        double x = rs.getDouble("home_x");
        double y = rs.getDouble("home_y");
        double z = rs.getDouble("home_z");
        float yaw = rs.getFloat("home_yaw");
        float pitch = rs.getFloat("home_pitch");

        if (Bukkit.getWorld(worldName) == null) {
            return null;
        }

        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }

    public static String formatForDisplay(Location location) {
        if (location == null || location.getWorld() == null) return "non impostata";

        return String.format("%s (%d, %d, %d)",
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }
}