package com.signition.samskybridge.feature;

import com.signition.samskybridge.integration.BentoSync;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FeatureService implements Listener {

    private final org.bukkit.plugin.Plugin plugin;
    private final FileConfiguration cfg;
    private final BentoSync bento;

    private final List<String> islandWorlds;
    private final List<String> mineWorlds;
    private final List<String> farmWorlds;

    private final boolean mineOwnerOnly;
    private final boolean farmOwnerOnly;
    private final boolean onlyOnOwnIslandMine;
    private final boolean onlyOnOwnIslandFarm;

    public FeatureService(org.bukkit.plugin.Plugin plugin, BentoSync bento){
        this.plugin = plugin;
        this.cfg = plugin.getConfig();
        this.bento = bento;

        this.islandWorlds = new ArrayList<String>();
        String islandWorld = cfg.getString("worlds.island-world", "bskyblock_world");
        if (islandWorld != null && !islandWorld.isEmpty()) islandWorlds.add(islandWorld);

        this.mineWorlds = cfg.getStringList("features.mine.allowed-worlds");
        this.farmWorlds = cfg.getStringList("features.farm.allowed-worlds");

        this.mineOwnerOnly = cfg.getBoolean("features.mine.only-owner-install", true);
        this.farmOwnerOnly = cfg.getBoolean("features.farm.only-owner-install", true);
        this.onlyOnOwnIslandMine = cfg.getBoolean("features.mine.only-on-own-island", true);
        this.onlyOnOwnIslandFarm = cfg.getBoolean("features.farm.only-on-own-island", true);
    }

    private String msg(String k){
        return Text.color(cfg.getString("messages."+k, "&c" + k));
    }

    private boolean worldAllowed(World w, List<String> featureWorlds){
        if (featureWorlds != null && !featureWorlds.isEmpty()){
            return featureWorlds.contains(w.getName());
        }
        // fallback to island world
        return islandWorlds.isEmpty() || islandWorlds.contains(w.getName());
    }

    public boolean canInstallMine(Player p, Location loc){
        if (!worldAllowed(loc.getWorld(), mineWorlds)){
            p.sendMessage(msg("not-island-world").replace("{world}", String.join(",", islandWorlds)));
            return false;
        }
        if (onlyOnOwnIslandMine && !bento.isInOwnIsland(p, loc)){
            p.sendMessage(msg("must-be-inside-own-island"));
            return false;
        }
        if (mineOwnerOnly && !bento.isOwner(p, loc)){
            p.sendMessage(msg("must-be-owner"));
            return false;
        }
        return true;
    }

    public boolean canInstallFarm(Player p, Location loc){
        if (!worldAllowed(loc.getWorld(), farmWorlds)){
            p.sendMessage(msg("not-island-world").replace("{world}", String.join(",", islandWorlds)));
            return false;
        }
        if (onlyOnOwnIslandFarm && !bento.isInOwnIsland(p, loc)){
            p.sendMessage(msg("must-be-inside-own-island"));
            return false;
        }
        if (farmOwnerOnly && !bento.isOwner(p, loc)){
            p.sendMessage(msg("must-be-owner"));
            return false;
        }
        return true;
    }

    // --- Simple Install/Remove Implementations ---
    public void installMine(Player p){
        Location loc = p.getLocation().getBlock().getLocation();
        if (!canInstallMine(p, loc)) return;
        Material m = Material.matchMaterial(cfg.getString("features.mine.block", "IRON_BLOCK"));
        if (m == null) m = Material.IRON_BLOCK;
        Block b = loc.getBlock();
        b.setType(m, false);
        p.sendMessage(Text.color("&a광산이 설치되었습니다."));
    }

    public void removeMine(Player p){
        Location loc = p.getLocation().getBlock().getLocation();
        Material m = Material.matchMaterial(cfg.getString("features.mine.block", "IRON_BLOCK"));
        if (m == null) m = Material.IRON_BLOCK;
        Block b = loc.getBlock();
        if (b.getType() == m){
            b.setType(Material.AIR, false);
            p.sendMessage(Text.color("&c광산을 제거했습니다."));
        } else {
            p.sendMessage(Text.color("&7여기는 광산 블록이 아닙니다."));
        }
    }

    public void installFarm(Player p){
        Location loc = p.getLocation().getBlock().getLocation();
        if (!canInstallFarm(p, loc)) return;
        Material m = Material.matchMaterial(cfg.getString("features.farm.block", "HAY_BLOCK"));
        if (m == null) m = Material.HAY_BLOCK;
        Block b = loc.getBlock();
        b.setType(m, false);
        p.sendMessage(Text.color("&a농장이 설치되었습니다."));
    }

    public boolean isInMineRegion(org.bukkit.entity.Player p, org.bukkit.Location loc){
        return canInstallMine(p, loc);
    }


    public org.bukkit.Material pickOreFor(org.bukkit.entity.Player p){
        int lv = plugin.getConfig().getInt("progress."+p.getUniqueId().toString()+".mine", 1);
        org.bukkit.configuration.ConfigurationSection sec = plugin.getConfig().getConfigurationSection("features.mine.levels."+lv+".weights");
        if (sec == null) return org.bukkit.Material.STONE;
        java.util.List<String> keys = new java.util.ArrayList<String>(sec.getKeys(false));
        int total = 0; int[] cum = new int[keys.size()]; int i=0;
        for (String k: keys){ int w = Math.max(1, sec.getInt(k,1)); total += w; cum[i++] = total; }
        int r = 1 + new java.util.Random().nextInt(Math.max(1,total));
        for (i=0;i<cum.length;i++){ if (r <= cum[i]){ org.bukkit.Material m = org.bukkit.Material.matchMaterial(keys.get(i)); return m==null? org.bukkit.Material.STONE : m; } }
        return org.bukkit.Material.STONE;
    }


    public boolean isInFarmRegion(org.bukkit.entity.Player p, org.bukkit.Location loc){
        return canInstallFarm(p, loc);
    }


    public org.bukkit.Material pickCropFor(org.bukkit.entity.Player p, org.bukkit.Material current){
        int lv = plugin.getConfig().getInt("progress."+p.getUniqueId().toString()+".farm", 1);
        // Prefer current crop if allowable; else roll from features.farm.crops.<lv>
        if (isCrop(current)) return current;
        org.bukkit.configuration.ConfigurationSection sec = plugin.getConfig().getConfigurationSection("features.farm.crops."+lv);
        if (sec == null) return org.bukkit.Material.WHEAT;
        java.util.List<String> keys = new java.util.ArrayList<String>(sec.getKeys(false));
        int total = 0; int[] cum = new int[keys.size()]; int i=0;
        for (String k: keys){ int w = Math.max(1, sec.getInt(k,1)); total += w; cum[i++] = total; }
        int r = 1 + new java.util.Random().nextInt(Math.max(1,total));
        for (i=0;i<cum.length;i++){ if (r <= cum[i]){ org.bukkit.Material m = org.bukkit.Material.matchMaterial(keys.get(i)); return m==null? org.bukkit.Material.WHEAT : m; } }
        return org.bukkit.Material.WHEAT;
    }


    public boolean isInAnyFarmRegion(org.bukkit.Location loc){
        java.util.List<String> worlds = this.farmWorlds;
        if (worlds == null || worlds.isEmpty()) return true;
        return worlds.contains(loc.getWorld().getName());
    }


    public int getFarmLevelByLoc(org.bukkit.Location loc){
        java.util.UUID owner = bento.getOwnerAt(loc);
        if (owner == null) return 0;
        return plugin.getConfig().getInt("progress."+owner.toString()+".farm", 0);
    }


    private static org.bukkit.plugin.Plugin STATIC_PLUGIN;
    private static com.signition.samskybridge.integration.BentoSync STATIC_BENTO;
    private static java.util.List<String> STATIC_ISLAND_WORLDS;
    public static void initStatic(org.bukkit.plugin.Plugin plugin, com.signition.samskybridge.integration.BentoSync bento){
        STATIC_PLUGIN = plugin; STATIC_BENTO = bento;
        STATIC_ISLAND_WORLDS = plugin.getConfig().getStringList("features.mine.allowed-worlds");
        if (STATIC_ISLAND_WORLDS == null || STATIC_ISLAND_WORLDS.isEmpty()){
            STATIC_ISLAND_WORLDS = new java.util.ArrayList<String>();
            String w = plugin.getConfig().getString("worlds.island-world", "bskyblock_world");
            STATIC_ISLAND_WORLDS.add(w);
        }
    }
    public static boolean isIslandWorldStatic(org.bukkit.World w){
        if (STATIC_ISLAND_WORLDS == null) return true;
        return STATIC_ISLAND_WORLDS.contains(w.getName());
    }
    public static boolean islandOwnerStatic(org.bukkit.entity.Player p){
        try { return STATIC_BENTO == null || STATIC_BENTO.isOwner(p, p.getLocation()); } catch (Throwable t){ return true; }
    }
    public static boolean islandMemberStatic(org.bukkit.entity.Player p){
        try { return STATIC_BENTO == null || STATIC_BENTO.isMember(p, p.getLocation()); } catch (Throwable t){ return false; }
    }


    private static boolean isCrop(org.bukkit.Material m){
        if (m == null) return false;
        switch (m){
            case WHEAT: case CARROTS: case POTATOES: case BEETROOTS:
            case NETHER_WART: case SUGAR_CANE: case MELON_STEM: case PUMPKIN_STEM:
            case COCOA: case SWEET_BERRY_BUSH:
                return true;
            default:
                return m.name().endswith("_SEEDS") || m.name().endswith("_SAPLING");
        }
    }
}