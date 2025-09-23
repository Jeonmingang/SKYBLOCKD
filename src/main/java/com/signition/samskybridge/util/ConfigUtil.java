package com.signition.samskybridge.util;

import com.signition.samskybridge.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigUtil {
    public static FileConfiguration loadBlocks(Main plugin){
        return YamlConfiguration.loadConfiguration(f);
    }
    public static boolean saveBlocks(Main plugin, FileConfiguration cfg){
        try {
            cfg.save(f);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}