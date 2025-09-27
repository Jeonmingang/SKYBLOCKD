package com.signition.samskybridge.feature;

import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class FeatureService implements Listener {

    private final Plugin plugin;
    private final FileConfiguration cfg;
    private final com.signition.samskybridge.integration.BentoSync bento;

    private final java.util.Set<String> islandWorlds;
    private final java.util.Set<String> mineWorlds;
    private final java.util.Set<String> farmWorlds;
    private final boolean mineOwnerOnly;
    private final boolean farmOwnerOnly;
    private final boolean onlyOnOwnIslandMine;
    private final boolean onlyOnOwnIslandFarm;
    private final boolean membersUseMine;
    private final boolean membersUseFarm;

    private final java.util.Map<java.util.UUID, java.util.List<Location>> installedMines = new java.util.HashMap<java.util.UUID, java.util.List<Location>>();
    private final java.util.Map<Location, java.util.UUID> mineOwnerByLocation = new java.util.HashMap<Location, java.util.UUID>();

    public FeatureService(Plugin plugin){
        this.plugin = plugin;
        this.cfg = plugin.getConfig();
        this.bento = new com.signition.samskybridge.integration.BentoSync(plugin);

        this.islandWorlds = new java.util.HashSet<String>(cfg.getStringList("features.require.island-worlds"));
        if (this.islandWorlds.isEmpty()) {
            String w = cfg.getString("worlds.island-world", "");
            if (w != null && !w.isEmpty()) this.islandWorlds.add(w);
        }
        this.mineWorlds = new java.util.HashSet<String>(cfg.getStringList("features.mine.allowed-worlds"));
        this.farmWorlds = new java.util.HashSet<String>(cfg.getStringList("features.farm.allowed-worlds"));
        this.mineOwnerOnly = cfg.getBoolean("features.require.owner-only", cfg.getBoolean("features.mine.only-owner-install", true));
        this.farmOwnerOnly = cfg.getBoolean("features.require.owner-only", cfg.getBoolean("features.farm.only-owner-install", true));
        this.onlyOnOwnIslandMine = cfg.getBoolean("features.mine.only-on-own-island", false);
        this.onlyOnOwnIslandFarm = cfg.getBoolean("features.farm.only-on-own-island", false);
        this.membersUseMine = cfg.getBoolean("features.mine.allow-members-use", true);
        this.membersUseFarm = cfg.getBoolean("features.farm.allow-members-use", true);

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private String msg(String key){
        return com.signition.samskybridge.util.Text.color(plugin.getConfig().getString("messages." + key, "&cMissing message: " + key));
    }

    public void shutdown(){}

    private boolean isWorldAllowed(World w){
        if (w == null) return false;
        if (!islandWorlds.isEmpty() && !islandWorlds.contains(w.getName())) return false;
        return true;
    }

    public boolean canInstallMine(Player p, Location loc){
        if (!isWorldAllowed(loc.getWorld())){
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
