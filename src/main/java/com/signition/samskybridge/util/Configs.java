
package com.signition.samskybridge.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Configs {
    public static void ensureDefaults(JavaPlugin plugin){
        FileConfiguration cfg = plugin.getConfig();
        boolean changed = false;

        if (!cfg.isConfigurationSection("ranking.tab-prefix")) {
            cfg.set("ranking.tab-prefix.force", true);
            cfg.set("ranking.tab-prefix.format", "&7[ &a섬 랭킹 &f<rank>위 &7| &bLv.<level> &7] &r");
            changed = true;
        }
        if (changed) {
            plugin.saveConfig();
        }
    }
}
