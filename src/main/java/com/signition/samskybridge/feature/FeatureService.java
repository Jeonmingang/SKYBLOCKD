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
}
