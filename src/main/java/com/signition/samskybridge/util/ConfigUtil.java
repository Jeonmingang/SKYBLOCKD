package com.signition.samskybridge.util;

import com.signition.samskybridge.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigUtil {
    public static FileConfiguration loadBlocks(Main plugin){
        File f = new File(plugin.getDataFolder(), "blocks.yml");
        return YamlConfiguration.loadConfiguration(f);
    }
    public static boolean saveBlocks(Main plugin, FileConfiguration cfg){
        File f = new File(plugin.getDataFolder(), "blocks.yml");
        try {
            cfg.save(f);
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("blocks.yml 저장 실패: " + e.getMessage());
            return false;
        }
    }
}