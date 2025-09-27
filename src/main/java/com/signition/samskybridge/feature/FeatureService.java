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
    private final boolean ownerOnly;
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
        this.ownerOnly = cfg.getBoolean("features.require.owner-only", cfg.getBoolean("features.mine.only-owner-install", true));
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
        if (ownerOnly && !bento.isOwner(p, loc)){
            p.sendMessage(msg("must-be-owner")); return false;
        }
        if (!mineWorlds.isEmpty() && !mineWorlds.contains(loc.getWorld().getName())){
            p.sendMessage(msg("not-island-world").replace("{world}", String.join(",", mineWorlds))); return false;
        }
        if (onlyOnOwnIslandMine && !bento.isMemberOrOwnerAt(p, loc)){
            p.sendMessage(msg("must-be-inside-own-island")); return false;
        }
        return true;
    }

    public boolean canInstallFarm(Player p, Location loc){
        if (!isWorldAllowed(loc.getWorld())){
            p.sendMessage(msg("not-island-world").replace("{world}", String.join(",", islandWorlds)));
            return false;
        }
        if (ownerOnly && !bento.isOwner(p, loc)){
            p.sendMessage(msg("must-be-owner")); return false;
        }
        if (!farmWorlds.isEmpty() && !farmWorlds.contains(loc.getWorld().getName())){
            p.sendMessage(msg("not-island-world").replace("{world}", String.join(",", farmWorlds))); return false;
        }
        if (onlyOnOwnIslandFarm && !bento.isMemberOrOwnerAt(p, loc)){
            p.sendMessage(msg("must-be-inside-own-island")); return false;
        }
        return true;
    }

    public void installMine(Player p){
        Location base = p.getLocation().getBlock().getLocation();
        if (!canInstallMine(p, base)) return;
        int len = cfg.getInt("features.mine.install.length", 4);
        java.util.List<Location> list = installedMines.computeIfAbsent(p.getUniqueId(), k -> new java.util.ArrayList<Location>());
        list.clear();
        for (int i=0;i<len;i++){
            Location l = base.clone().add(i,0,0);
            list.add(l);
            mineOwnerByLocation.put(l, p.getUniqueId());
            l.getBlock().setType(Material.STONE);
        }
        p.sendMessage("§a광산 설치 완료: 길이 " + len);
    }

    public void removeMine(Player p){
        java.util.List<Location> list = installedMines.get(p.getUniqueId());
        if (list == null || list.isEmpty()){ p.sendMessage("§c설치된 광산이 없습니다."); return; }
        for (Location l : list){
            if (l.getBlock().getType() != Material.AIR) l.getBlock().setType(Material.AIR);
            mineOwnerByLocation.remove(l);
        }
        list.clear();
        p.sendMessage("§a광산 제거 완료");
    }

    public void installFarm(Player p){
        Location base = p.getLocation().getBlock().getLocation();
        if (!canInstallFarm(p, base)) return;
        p.sendMessage("§a농장 설치 체크 완료 (자동 재심기 포함).");
    }

    private java.util.Map<Material, Integer> getMineWeights(int level){
        java.util.Map<Material, Integer> map = new java.util.LinkedHashMap<Material, Integer>();
        org.bukkit.configuration.ConfigurationSection sec = cfg.getConfigurationSection("features.mine.levels." + level + ".weights");
        if (sec != null){
            for (String k : sec.getKeys(false)){
                Material m = Material.matchMaterial(k);
                if (m != null) map.put(m, sec.getInt(k));
            }
        } else {
            java.util.List<String> list = cfg.getStringList("features.mine.ores." + level);
            for (String s : list){
                String[] parts = s.split(":");
                if (parts.length == 2){
                    Material m = Material.matchMaterial(parts[0]);
                    try { int v = Integer.parseInt(parts[1]); if (m != null) map.put(m, v); } catch (Exception ignored){}
                }
            }
        }
        if (map.isEmpty()) map.put(Material.STONE, 100);
        return map;
    }

    private Material pick(java.util.Map<Material,Integer> weights){
        int total=0; for (int v : weights.values()) total += v;
        int r = new java.util.Random().nextInt(Math.max(1, total));
        int c=0; for (java.util.Map.Entry<Material,Integer> e : weights.entrySet()){
            c += e.getValue(); if (r < c) return e.getKey();
        }
        return Material.STONE;
    }

    private int getMineRegenTicks(int level){
        int sec = cfg.getInt("features.mine.levels." + level + ".regen-seconds", 0);
        if (sec > 0) return sec * 20;
        return cfg.getInt("features.mine.regen.delay-ticks", 8);
    }
    private int getFarmReplantDelay(){ return cfg.getInt("features.farm.replant.delay-ticks", 4); }
    private boolean requireSeed(){ return cfg.getBoolean("features.farm.replant.require-seed", true); }
    private boolean denyWhenMissing(){ return cfg.getBoolean("features.farm.replant.deny-when-missing", true); }

    public int getMineLevel(Player p){ return com.signition.samskybridge.Main.get().getUpgradeService().getLevel(p.getUniqueId(), "mine"); }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e){
        Player p = e.getPlayer();
        Block block = e.getBlock();
        if (!isWorldAllowed(block.getWorld())) return;

        java.util.UUID ownerId = mineOwnerByLocation.get(block.getLocation());
        if (ownerId != null){
            boolean allowed = ownerId.equals(p.getUniqueId());
            if (!allowed && membersUseMine){
                if (bento.isMemberOrOwnerAt(p, block.getLocation())) allowed = true;
            }
            if (!allowed){ e.setCancelled(true); p.sendMessage("§c소유자/섬원만 사용 가능"); return; }
        }

        java.util.List<Location> list = installedMines.getOrDefault(p.getUniqueId(), java.util.Collections.<Location>emptyList());
        for (Location l : list){
            if (l.equals(block.getLocation())){
                int lv = getMineLevel(p);
                java.util.Map<Material,Integer> weights = getMineWeights(lv);
                int delay = getMineRegenTicks(lv);
                org.bukkit.scheduler.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    l.getBlock().setType(pick(weights));
                }, delay);
                break;
            }
        }

        Material type = block.getType();
        if (type == Material.WHEAT || type == Material.CARROTS || type == Material.POTATOES || type == Material.BEETROOTS){
            org.bukkit.block.data.BlockData data = block.getBlockData();
            if (data instanceof org.bukkit.block.data.Ageable){
                org.bukkit.block.data.Ageable age = (org.bukkit.block.data.Ageable) data;
                if (age.getAge() >= age.getMaximumAge()){
                    if (requireSeed()){
                        Material seed = (type == Material.CARROTS) ? Material.CARROT :
                                        (type == Material.POTATOES) ? Material.POTATO :
                                        (type == Material.BEETROOTS) ? Material.BEETROOT_SEEDS : Material.WHEAT_SEEDS;
                        if (!p.getInventory().containsAtLeast(new ItemStack(seed), 1)){
                            if (denyWhenMissing()){ e.setCancelled(true); p.sendMessage(Text.msg("need-seed")); return; }
                        } else {
                            p.getInventory().removeItem(new ItemStack(seed, 1));
                        }
                    }
                    int delay = getFarmReplantDelay();
                    Location loc = block.getLocation();
                    org.bukkit.scheduler.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        loc.getBlock().setType(type);
                        org.bukkit.block.data.Ageable a = (org.bukkit.block.data.Ageable) loc.getBlock().getBlockData();
                        a.setAge(0); loc.getBlock().setBlockData(a);
                    }, delay);
                }
            }
        }
    }
}
