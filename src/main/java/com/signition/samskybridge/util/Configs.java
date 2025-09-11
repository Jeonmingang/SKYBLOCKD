package com.signition.samskybridge.util;

import com.signition.samskybridge.Main;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.*;

public class Configs {
  public static void ensureDefaults(Main plugin){
    FileConfiguration c = plugin.getConfig();

    if (!c.contains("debug.xp-blocks")) c.set("debug.xp-blocks", false);

    // Worlds where XP is enabled
    if (!c.contains("xp.allowed-worlds"))
      c.set("xp.allowed-worlds", Arrays.asList("bskyblock_world", "BSkyBlock_world"));

    // Ranking display
    if (!c.contains("ranking.unranked-label"))
      c.set("ranking.unranked-label", "등록안됨");
    if (!c.contains("ranking.tab-prefix"))
      c.set("ranking.tab-prefix", "&7[ &a섬 랭킹 &f<rank> &7| &bLv.<level> &7] &r");

    // XP blocks defaults (example values)
    if (!c.isConfigurationSection("xp.blocks")){
      Map<String,Integer> defaults = new LinkedHashMap<String,Integer>();
      defaults.put("minecraft:stone", 1);
      defaults.put("minecraft:cobblestone", 1);
      defaults.put("minecraft:oak_log", 1);
      defaults.put("minecraft:coal_ore", 2);
      defaults.put("minecraft:iron_ore", 3);
      defaults.put("minecraft:gold_ore", 4);
      defaults.put("minecraft:redstone_ore", 2);
      defaults.put("minecraft:lapis_ore", 3);
      defaults.put("minecraft:diamond_ore", 8);
      defaults.put("minecraft:emerald_ore", 10);
      defaults.put("minecraft:obsidian", 3);
      for (Map.Entry<String,Integer> e : defaults.entrySet()){
        c.set("xp.blocks."+e.getKey(), e.getValue());
      }
    }

    plugin.saveConfig();
  }
}