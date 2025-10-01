package com.signition.samskybridge.util;

import org.bukkit.ChatColor;

public final class Text {
    private Text() {}

    /** Remove color codes (& / §) and return plain text. */
    public static String stripColor(String input) {
        if (input == null) return "";
        String colored = ChatColor.translateAlternateColorCodes('&', input);
        return ChatColor.stripColor(colored);
    }
}
