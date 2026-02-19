package it.shottydeveloper.superclans.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class ChatUtil {

    private ChatUtil() {}

    public static String color(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String stripColor(String text) {
        if (text == null) return "";
        return ChatColor.stripColor(color(text));
    }

    public static void send(CommandSender sender, String message) {
        sender.sendMessage(color(message));
    }

    public static String separator() {
        return color("&8&m" + "─".repeat(40));
    }

    public static String centerMessage(String message) {
        if (message == null || message.isEmpty()) return message;

        String stripped = ChatColor.stripColor(color(message));

        int messagePxSize = stripped.length() * 6;
        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = 160 - halvedMessageSize;
        int spaceLength = toCompensate / 4;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < spaceLength; i++) {
            sb.append(" ");
        }
        return sb + message;
    }

    public static boolean isValidString(String text, String allowedCharsRegex) {
        if (text == null || text.isEmpty()) return false;
        return text.chars().allMatch(c -> String.valueOf((char) c).matches(allowedCharsRegex));
    }
}