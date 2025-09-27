package com.signition.samskybridge.util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public final class Text {
    private static Plugin plugin;
    public static void init(Plugin p){ plugin = p; }
    public static String color(String s){ return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s); }
    public static String msg(String key){
        if (plugin == null) return "";
        String d = "&cMissing message: " + key;
        // Try messages_ko.yml first (loaded as resource), then config.yml
        String res = plugin.getConfig().getString("messages." + key);
        if (res == null) {
            try {
                java.io.InputStream is = plugin.getResource("messages_ko.yml");
                if (is != null) {
                    org.bukkit.configuration.file.YamlConfiguration y = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(new java.io.InputStreamReader(is));
                    res = y.getString("messages." + key, d);
                }
            } catch (Throwable ignored){}
        }
        return color(res == null ? d : res);
    }
}
