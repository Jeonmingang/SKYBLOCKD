package com.signition.samskybridge.feature;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.util.Text;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class FeatureService {

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

    public void reload() {
        this.plugin.reloadConfig();
    }

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}
 /* no cache; keys read live */ }
    private static FeatureService INSTANCE;

    private final Main plugin;
    private final DataStore store;
    private final File file;
    private final FileConfiguration data;

    public FeatureService(Main plugin, DataStore store){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

        this.plugin = plugin;
        this.store = store;
        this.file = new File(plugin.getDataFolder(), "features.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
        INSTANCE = this;
    }

    public void save(){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}
 try {

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}
 data.save(file); } catch (IOException ignored) {

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}
} }

    /* ================== Helpers / Guards ================== */
    private IslandData ownedIsland(Player p){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

        try {

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}
 return store.findByOwnerName(p.getName()); } catch (Throwable t) {

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}
 return null; }
    }

    private boolean canOperateHere(Player p){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

        List<String> worlds = plugin.getConfig().getStringList("features.require.island-worlds");
        if (worlds != null && !worlds.isEmpty()){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

            String wn = p.getWorld() != null ? p.getWorld().getName() : "";
            if (!worlds.contains(wn)){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

                p.sendMessage(Text.color(plugin.getConfig().getString("features.messages.only-on-island-world","&c섬 월드에서만 사용 가능")));
                return false;
            }
        }
        if (plugin.getConfig().getBoolean("features.require.owner-only", true)){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

            if (ownedIsland(p) == null){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

                p.sendMessage(Text.color(plugin.getConfig().getString("features.messages.owner-only","&c섬장만 사용할 수 있습니다.")));
                return false;
            }
        }
        return true;
    }

    private String base(UUID id) {

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}
 return "islands."+id.toString(); }

    private void setLoc(UUID id, String key, Location loc){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

        String b = base(id)+"."+key+".loc";
        if (loc == null || loc.getWorld() == null) {

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

            data.set(b, null);
            save();
            return;
        }
        data.set(b+".world", loc.getWorld().getName());
        data.set(b+".x", loc.getBlockX());
        data.set(b+".y", loc.getBlockY());
        data.set(b+".z", loc.getBlockZ());
        save();
    }
    private Location getLoc(UUID id, String key){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

        String b = base(id)+"."+key+".loc";
        String w = data.getString(b+".world", null);
        if (w == null) return null;
        World world = plugin.getServer().getWorld(w);
        if (world == null) return null;
        int x = data.getInt(b+".x", 0);
        int y = data.getInt(b+".y", 0);
        int z = data.getInt(b+".z", 0);
        return new Location(world, x, y, z);
    }

    public boolean hasMine(UUID id) {

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}
 return getLoc(id, "mine") != null; }
    public boolean hasFarm(UUID id) {

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}
 return getLoc(id, "farm") != null; }

    /* ================== Regions (used by listeners) ================== */
    public boolean isInMineRegion(Player p, Location loc){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

        IslandData is = ownedIsland(p);
        if (is == null) return false;
        Location origin = getLoc(is.getId(), "mine");
        if (origin == null || loc == null || origin.getWorld() == null || loc.getWorld() == null) return false;
        if (!origin.getWorld().equals(loc.getWorld())) return false;
        // 1x1xlen line starting from origin along player's stored axis (default X)
        int len = Math.max(1, plugin.getConfig().getInt("features.mine.install.length", 4));
        String axis = data.getString(base(is.getId())+".mine.axis", "X");
        int ox = origin.getBlockX(), oy = origin.getBlockY(), oz = origin.getBlockZ();
        if (loc.getBlockY() != oy) return false;
        if ("Z".equalsIgnoreCase(axis)) {

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

            return loc.getBlockX()==ox && loc.getBlockZ()>=oz && loc.getBlockZ()<=oz+(len-1);
        } else {

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

            return loc.getBlockZ()==oz && loc.getBlockX()>=ox && loc.getBlockX()<=ox+(len-1);
        }
    }

    public boolean isInFarmRegion(Player p, Location loc){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

        IslandData is = ownedIsland(p);
        if (is == null) return false;
        Location origin = getLoc(is.getId(), "farm");
        if (origin == null || loc == null || origin.getWorld() == null || loc.getWorld() == null) return false;
        if (!origin.getWorld().equals(loc.getWorld())) return false;
        // 3x3 centered on origin on XZ
        int half = 1;
        return Math.abs(loc.getBlockX()-origin.getBlockX())<=half && Math.abs(loc.getBlockZ()-origin.getBlockZ())<=half;
    }

    /* ================== Weighted tables ================== */
    public Map<Material,Integer> parseWeighted(List<String> defs){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

        Map<Material,Integer> out = new HashMap<Material,Integer>();
        if (defs == null) return out;
        for (String s : defs){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

            try {

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

                String[] sp = s.trim().split(":");
                Material m = Material.valueOf(sp[0].trim());
                int w = (sp.length>1)? Integer.parseInt(sp[1].trim()) : 1;
                out.put(m, out.containsKey(m)? out.get(m)+w : w);
            } catch (Throwable ignore) {

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}
 }
        }
        return out;
    }

    public Material pickOreFor(Player p){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

        IslandData is = ownedIsland(p);
        if (is == null) return Material.COAL_ORE;
        int lv = getMineLevel(is.getId());
        List<String> defs = plugin.getConfig().getStringList("features.mine.ores."+lv);
        Map<Material,Integer> bag = parseWeighted(defs);
        if (bag.isEmpty()) return Material.COAL_ORE;
        int total = 0; for (int w : bag.values()) total += w;
        int r = ThreadLocalRandom.current().nextInt(Math.max(1,total));
        for (Map.Entry<Material,Integer> e : bag.entrySet()) {

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

            r -= e.getValue();
            if (r < 0) return e.getKey();
        }
        return Material.COAL_ORE;
    }

    public Material pickCropFor(Player p, Material fallback){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

        IslandData is = ownedIsland(p);
        if (is == null) return fallback;
        int lv = getFarmLevel(is.getId());
        List<String> defs = plugin.getConfig().getStringList("features.farm.crops."+lv);
        Map<Material,Integer> bag = parseWeighted(defs);
        if (bag.isEmpty()) return fallback;
        int total = 0; for (int w : bag.values()) total += w;
        int r = ThreadLocalRandom.current().nextInt(Math.max(1,total));
        for (Map.Entry<Material,Integer> e : bag.entrySet()) {

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

            r -= e.getValue();
            if (r < 0) return e.getKey();
        }
        return fallback;
    }

    /* ================== Install ================== */
    public void installMine(Player p){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

        if (!canOperateHere(p)) return;
        IslandData is = ownedIsland(p); if (is==null) return;
        if (hasMine(is.getId())){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}
 p.sendMessage(Text.color("&c이미 설치된 광산이 있습니다.")); return; }
        Location base = p.getLocation().getBlock().getLocation();
        World w = base.getWorld();
        int len = Math.max(1, plugin.getConfig().getInt("features.mine.install.length", 4));
        // forward direction -> axis
        String axis = Math.abs(p.getLocation().getDirection().getZ()) >= Math.abs(p.getLocation().getDirection().getX()) ? "Z" : "X";
        Location start = base.clone().add(p.getLocation().getDirection().setY(0).normalize());
        start.setY(base.getBlockY());
        for (int i=0;i<len;i++) {

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

            Location t = start.clone();
            if ("Z".equals(axis)) t.add(0,0,i); else t.add(i,0,0);
            w.getBlockAt(t).setType(Material.STONE, false);
        }
        setLoc(is.getId(),"mine", start);
        data.set(base(is.getId())+".mine.axis", axis);
        save();
        p.sendMessage(Text.color("&a광산 설치 완료 (&7방향:&f"+axis+" &7길이:&f"+len+"&a)"));
    }

    public void installFarm(Player p){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

        if (!canOperateHere(p)) return;
        IslandData is = ownedIsland(p); if (is==null) return;
        if (hasFarm(is.getId())){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}
 p.sendMessage(Text.color("&c이미 설치된 농장이 있습니다.")); return; }
        Location origin = p.getLocation().getBlock().getLocation().add(p.getLocation().getDirection().setY(0).normalize());
        World w = origin.getWorld();
        // 3x3 farmland + crop
        for (int dx=-1; dx<=1; dx++){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

            for (int dz=-1; dz<=1; dz++){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

                Location fl = origin.clone().add(dx,0,dz);
                w.getBlockAt(fl).setType(Material.FARMLAND, false);
                w.getBlockAt(fl.clone().add(0,1,0)).setType(Material.WHEAT, false);
            }
        }
        setLoc(is.getId(),"farm", origin);
        save();
        p.sendMessage(Text.color("&a농장 설치 완료 (&73x3&a)"));
    }

    /* ================== Upgrade ================== */
    public boolean upgradeMine(Player p){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

        if (!canOperateHere(p)) return false;
        IslandData is = ownedIsland(p); if (is==null) return false;
        int now = getMineLevel(is.getId());
        int max = plugin.getConfig().getInt("features.mine.max-level", 5);
        if (now >= max){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}
 p.sendMessage(Text.color("&c광산 레벨이 최대입니다.")); return false; }
        // 비용/요구레벨은 기존 구현 사용 전제(여기선 표시만)
        setMineLevel(is.getId(), now+1);
        save();
        p.sendMessage(Text.color("&a광산 레벨 업! 현재: &f"+(now+1)));
        return true;
    }

    public boolean upgradeFarm(Player p){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

        if (!canOperateHere(p)) return false;
        IslandData is = ownedIsland(p); if (is==null) return false;
        int now = getFarmLevel(is.getId());
        int max = plugin.getConfig().getInt("features.farm.max-level", 5);
        if (now >= max){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}
 p.sendMessage(Text.color("&c농장 레벨이 최대입니다.")); return false; }
        setFarmLevel(is.getId(), now+1);
        save();
        p.sendMessage(Text.color("&a농장 레벨 업! 현재: &f"+(now+1)));
        return true;
    }

    /* ================== Remove ================== */
    public void removeMine(Player p){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

        IslandData is = ownedIsland(p); if (is==null) return;
        Location start = getLoc(is.getId(),"mine"); if (start==null){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}
 p.sendMessage(Text.color("&c설치된 광산이 없습니다.")); return; }
        World w = start.getWorld();
        int len = Math.max(1, plugin.getConfig().getInt("features.mine.install.length", 4));
        String axis = data.getString(base(is.getId())+".mine.axis", "X");
        for (int i=0;i<len;i++) {

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

            Location t = start.clone();
            if ("Z".equals(axis)) t.add(0,0,i); else t.add(i,0,0);
            w.getBlockAt(t).setType(Material.AIR, false);
        }
        data.set(base(is.getId())+".mine", null);
        save();
        p.sendMessage(Text.color("&a광산 제거 완료"));
    }

    public void removeFarm(Player p){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

        IslandData is = ownedIsland(p); if (is==null) return;
        Location origin = getLoc(is.getId(),"farm"); if (origin==null){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}
 p.sendMessage(Text.color("&c설치된 농장이 없습니다.")); return; }
        World w = origin.getWorld();
        for (int dx=-1; dx<=1; dx++){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

            for (int dz=-1; dz<=1; dz++){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

                Location fl = origin.clone().add(dx,0,dz);
                w.getBlockAt(fl).setType(Material.AIR, false);
                w.getBlockAt(fl.clone().add(0,1,0)).setType(Material.AIR, false);
            }
        }
        data.set(base(is.getId())+".farm", null);
        save();
        p.sendMessage(Text.color("&a농장 제거 완료"));
    }

    /* ================== Levels (persist) ================== */
    public int getMineLevel(UUID id){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}
 return data.getInt(base(id)+".mine.level", 0); }
    public int getFarmLevel(UUID id){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}
 return data.getInt(base(id)+".farm.level", 0); }
    public void setMineLevel(UUID id, int v){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}
 data.set(base(id)+".mine.level", v); }
    public void setFarmLevel(UUID id, int v){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}
 data.set(base(id)+".farm.level", v); }

    /* ================== Static wrappers for callers ================== */
    public static int getMineLevelStatic(UUID id){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}
 return INSTANCE!=null ? INSTANCE.getMineLevel(id) : 0; }
    public static int getFarmLevelStatic(UUID id){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}
 return INSTANCE!=null ? INSTANCE.getFarmLevel(id) : 0; }

    /* ================== Optional helpers used elsewhere ================== */
    public boolean isIslandWorld(World world) {

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}
 return world != null; } // safe default
    public boolean islandOwner(Player p) {

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}
 return true; }              // safe default (override by reflection version)
    public boolean islandMember(Player p) {

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}
 return true; }             // safe default


public int getFarmLevelByLoc(org.bukkit.Location loc){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

    // naive: find any farm origin within 3x3; return 0 if none
    try{

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

        org.bukkit.configuration.ConfigurationSection sec = data.getConfigurationSection("islands");
        if (sec != null){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

            for (String k : sec.getKeys(false)){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

                java.util.UUID id = java.util.UUID.fromString(k);
                org.bukkit.Location origin = getLoc(id, "farm");
                if (origin != null && origin.getWorld()!=null && origin.getWorld().equals(loc.getWorld())){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

                    if (Math.abs(loc.getBlockX()-origin.getBlockX())<=1 && Math.abs(loc.getBlockZ()-origin.getBlockZ())<=1){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

                        return getFarmLevel(id);
                    }
                }
            }
        }
    }catch(Throwable ignore){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}
}
    return 0;
}

public boolean isInAnyFarmRegion(org.bukkit.Location loc){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

    try{

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

        org.bukkit.configuration.ConfigurationSection sec = data.getConfigurationSection("islands");
        if (sec != null){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

            for (String k : sec.getKeys(false)){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

                java.util.UUID id = java.util.UUID.fromString(k);
                org.bukkit.Location origin = getLoc(id, "farm");
                if (origin != null && origin.getWorld()!=null && origin.getWorld().equals(loc.getWorld())){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

                    if (Math.abs(loc.getBlockX()-origin.getBlockX())<=1 && Math.abs(loc.getBlockZ()-origin.getBlockZ())<=1){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

                        return true;
                    }
                }
            }
        }
    }catch(Throwable ignore){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}
}
    return false;
}


    public int getMaxMineLevel(){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

        return this.plugin.getConfig().getInt("features.mine.max-level", 5);
    }
    public int getMaxFarmLevel(){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

        return this.plugin.getConfig().getInt("features.farm.max-level", 5);
    }

public static boolean islandOwnerStatic(org.bukkit.entity.Player p){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

    return INSTANCE != null && INSTANCE.islandOwner(p);
}
public static boolean islandMemberStatic(org.bukkit.entity.Player p){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

    return INSTANCE != null && INSTANCE.islandMember(p);
}
public static boolean isIslandWorldStatic(org.bukkit.World w){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

    return INSTANCE != null && INSTANCE.isIslandWorld(w);
}

    private static boolean isCrop(org.bukkit.Material m){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

        if (m == null) return false;
        switch (m){

// === Config mapping helpers (22 base + 20 feature mapping) ===
private List<String> readAllowedWorlds(String featureKey) {
    // Primary: features.<feature>.allowed-worlds (list)
    List<String> worlds = this.plugin.getConfig().getStringList("features." + featureKey + ".allowed-worlds");
    if (worlds != null && !worlds.isEmpty()) return worlds;
    // Fallback: features.require.island-worlds (legacy list)
    List<String> legacy = this.plugin.getConfig().getStringList("features.require.island-worlds");
    return legacy != null ? legacy : java.util.Collections.emptyList();
}
private boolean readOnlyOwnerInstall(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-owner-install"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-owner-install");
    // legacy/global fallback
    if (this.plugin.getConfig().isSet("features.require.owner-only"))
        return this.plugin.getConfig().getBoolean("features.require.owner-only");
    return false;
}
private boolean readOnlyOnOwnIsland(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".only-on-own-island"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".only-on-own-island");
    // default true for safety on SkyBlock environments
    return true;
}
private boolean readAllowMembersUse(String featureKey){
    if (this.plugin.getConfig().isSet("features." + featureKey + ".allow-members-use"))
        return this.plugin.getConfig().getBoolean("features." + featureKey + ".allow-members-use");
    // default true (members allowed) if not specified
    return true;
}
private int readMineRegenTicks(int level){
    // Primary: features.levels.<lv>.regen-seconds
    String lp = "features.levels."+level+".regen-seconds";
    if (this.plugin.getConfig().isSet(lp)) {
        int seconds = this.plugin.getConfig().getInt(lp, 20);
        return Math.max(1, seconds) * 20;
    }
    // Fallback: features.mine.regen.delay-ticks
    return this.plugin.getConfig().getInt("features.mine.regen.delay-ticks", 20);
}
private Map<Material,Integer> readOreWeights(int level){
    Map<Material,Integer> map = new java.util.HashMap<>();
    String base = "features.levels."+level+".weights";
    if (this.plugin.getConfig().isConfigurationSection(base)){
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection(base);
        for (String k : sec.getKeys(false)){
            try {
                Material m = Material.valueOf(k);
                int v = sec.getInt(k, 0);
                if (v > 0) map.put(m, v);
            } catch (IllegalArgumentException ignore){}
        }
    }
    if (!map.isEmpty()) return map;
    // Fallback list: features.mine.ores.<lv> : [STONE:70, COAL_ORE:20, ...]
    String fb = "features.mine.ores."+level;
    java.util.List<String> list = this.plugin.getConfig().getStringList(fb);
    for (String entry : list){
        String s = entry.trim();
        if (s.isEmpty()) continue;
        String[] kv = s.split("[:=]");
        if (kv.length>=2){
            try {
                Material m = Material.valueOf(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                if (v>0) map.put(m, v);
            } catch (Exception ignore){}
        }
    }
    return map;
}
private boolean isWorldAllowed(String featureKey, World w){
    java.util.List<String> worlds = readAllowedWorlds(featureKey);
    if (worlds.isEmpty()) return true; // no restriction set
    return worlds.contains(w.getName());
}

            case WHEAT: case CARROTS: case POTATOES: case BEETROOTS:
            case NETHER_WART: case SUGAR_CANE: case MELON_STEM: case PUMPKIN_STEM:
            case COCOA: case SWEET_BERRY_BUSH:
                return true;
            default:
                String name = m.name();
                return name.endsWith("_SEEDS") || name.endsWith("_SAPLING");
        }
    }

public boolean canInstallMine(org.bukkit.entity.Player p, org.bukkit.Location loc){
    if (!isWorldAllowed("mine", loc.getWorld())) return false;
    if (readOnlyOnOwnIsland("mine")){
        if (!this.bento.isMemberOrOwnerAt(p, loc)) return false;
    }
    if (readOnlyOwnerInstall("mine")){
        // owner install only
        return this.bento.isOwner(p, loc);
    }
    return true;
}

public boolean canUseMine(org.bukkit.entity.Player p, org.bukkit.Location loc){
    if (!isWorldAllowed("mine", loc.getWorld())) return false;
    if (readOnlyOnOwnIsland("mine")){
        if (!this.bento.isMemberOrOwnerAt(p, loc)) return false;
    }
    if (!readAllowMembersUse("mine")){
        return this.bento.isOwner(p, loc);
    }
    return true;
}

public boolean canInstallFarm(org.bukkit.entity.Player p, org.bukkit.Location loc){
    if (!isWorldAllowed("farm", loc.getWorld())) return false;
    if (readOnlyOnOwnIsland("farm")){
        if (!this.bento.isMemberOrOwnerAt(p, loc)) return false;
    }
    if (readOnlyOwnerInstall("farm")){
        return this.bento.isOwner(p, loc);
    }
    return true;
}

public boolean canUseFarm(org.bukkit.entity.Player p, org.bukkit.Location loc){
    if (!isWorldAllowed("farm", loc.getWorld())) return false;
    if (readOnlyOnOwnIsland("farm")){
        if (!this.bento.isMemberOrOwnerAt(p, loc)) return false;
    }
    if (!readAllowMembersUse("farm")){
        return this.bento.isOwner(p, loc);
    }
    return true;
}

public int getMineRegenTicks(int level){
    return readMineRegenTicks(level);
}

public java.util.Map<Material,Integer> getOreWeights(int level){
    return readOreWeights(level);
}
}