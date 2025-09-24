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
    private static FeatureService INSTANCE;

    private final Main plugin;
    private final DataStore store;
    private final File file;
    private final FileConfiguration data;

    public FeatureService(Main plugin, DataStore store){
        this.plugin = plugin;
        this.store = store;
        this.file = new File(plugin.getDataFolder(), "features.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
        INSTANCE = this;
    }

    public void save(){ try { data.save(file); } catch (IOException ignored) {} }

    /* ================== Helpers / Guards ================== */
    private IslandData ownedIsland(Player p){
        try { return store.findByOwnerName(p.getName()); } catch (Throwable t) { return null; }
    }

    private boolean canOperateHere(Player p){
        List<String> worlds = plugin.getConfig().getStringList("features.require.island-worlds");
        if (worlds != null && !worlds.isEmpty()){
            String wn = p.getWorld() != null ? p.getWorld().getName() : "";
            if (!worlds.contains(wn)){
                p.sendMessage(Text.color(plugin.getConfig().getString("features.messages.only-on-island-world","&c섬 월드에서만 사용 가능")));
                return false;
            }
        }
        if (plugin.getConfig().getBoolean("features.require.owner-only", true)){
            if (ownedIsland(p) == null){
                p.sendMessage(Text.color(plugin.getConfig().getString("features.messages.owner-only","&c섬장만 사용할 수 있습니다.")));
                return false;
            }
        }
        return true;
    }

    private String base(UUID id) { return "islands."+id.toString(); }

    private void setLoc(UUID id, String key, Location loc){
        String b = base(id)+"."+key+".loc";
        if (loc == null || loc.getWorld() == null) {
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

    public boolean hasMine(UUID id) { return getLoc(id, "mine") != null; }
    public boolean hasFarm(UUID id) { return getLoc(id, "farm") != null; }

    /* ================== Regions (used by listeners) ================== */
    public boolean isInMineRegion(Player p, Location loc){
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
            return loc.getBlockX()==ox && loc.getBlockZ()>=oz && loc.getBlockZ()<=oz+(len-1);
        } else {
            return loc.getBlockZ()==oz && loc.getBlockX()>=ox && loc.getBlockX()<=ox+(len-1);
        }
    }

    public boolean isInFarmRegion(Player p, Location loc){
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
    public Map<Material,Double> parseWeighted(List<String> defs){
        Map<Material,Double> out = new HashMap<Material,Double>();
        if (defs == null) return out;
        for (String s : defs){
            try {
                String[] sp = s.trim().split(":");
                Material m = Material.valueOf(sp[0].trim());
                double w = (sp.length>1)? Double.parseDouble(sp[1].trim()) : 1.0;
                out.put(m, out.containsKey(m)? out.get(m)+w : w);
            } catch (Throwable ignore) { }
        }
        return out;
    }

    // === New helpers for farm sizing and resource consumption ===
    public int getFarmInstallRadius(int level){
        int base = plugin.getConfig().getInt("features.farm.install.base-radius", 1); // 1 => 3x3
        int per  = plugin.getConfig().getInt("features.farm.install.per-level", 0);
        int max  = Math.max(0, base + Math.max(0, level) * Math.max(0, per));
        return Math.max(1, max);
    }
    private Material getConfiguredSeed(){
        try{
            String key = plugin.getConfig().getString("features.farm.install.seed-item", "WHEAT_SEEDS");
            return Material.valueOf(key);
        }catch(Throwable t){
            return Material.WHEAT_SEEDS;
        }
    }
    private boolean consumeItems(org.bukkit.entity.Player p, Material mat, int count){
        int need = count;
        org.bukkit.inventory.PlayerInventory inv = p.getInventory();
        for (org.bukkit.inventory.ItemStack it : inv.getContents()){
            if (it == null) continue;
            if (it.getType() == mat){
                int take = Math.min(need, it.getAmount());
                it.setAmount(it.getAmount() - take);
                need -= take;
                if (need <= 0) { p.updateInventory(); return true; }
            }
        }
        p.updateInventory();
        return false;
    }
    private void placeMarker(org.bukkit.World w, org.bukkit.Location loc, String path, String fallback){
        boolean enabled = plugin.getConfig().getBoolean(path+".enabled", true);
        if (!enabled) return;
        String mat = plugin.getConfig().getString(path+".block", fallback);
        try{
            org.bukkit.Material m = org.bukkit.Material.valueOf(mat);
            w.getBlockAt(loc).setType(m, false);
        }catch(Throwable ignore){}
    }

public Material pickOreFor(Player p){
        IslandData is = ownedIsland(p);
        if (is == null) return Material.COAL_ORE;
        int lv = getMineLevel(is.getId());
        List<String> defs = plugin.getConfig().getStringList("features.mine.ores."+lv);
        Map<Material,Double> bag = parseWeighted(defs);
        if (bag.isEmpty()) return Material.COAL_ORE;
        double total = 0.0; for (double w : bag.values()) total += w;
        double r = ThreadLocalRandom.current().nextDouble(Math.max(1.0,total));
        for (Map.Entry<Material,Double> e : bag.entrySet()) {
            r -= e.getValue();
            if (r < 0) return e.getKey();
        }
        return Material.COAL_ORE;
    }

    public Material pickCropFor(Player p, Material fallback){
        IslandData is = ownedIsland(p);
        if (is == null) return fallback;
        int lv = getFarmLevel(is.getId());
        List<String> defs = plugin.getConfig().getStringList("features.farm.crops."+lv);
        Map<Material,Double> bag = parseWeighted(defs);
        if (bag.isEmpty()) return fallback;
        double total = 0.0; for (double w : bag.values()) total += w;
        double r = ThreadLocalRandom.current().nextDouble(Math.max(1.0,total));
        for (Map.Entry<Material,Double> e : bag.entrySet()) {
            r -= e.getValue();
            if (r < 0) return e.getKey();
        }
        return fallback;
    }

    /* ================== Install ================== */
    public void installMine(Player p){

        if (!canOperateHere(p)) return;
        IslandData is = ownedIsland(p); if (is==null) return;
        if (hasMine(is.getId())){ p.sendMessage(Text.color("&c이미 설치된 광산이 있습니다.")); return; }
        Location base = p.getLocation().getBlock().getLocation();
        World w = base.getWorld();
        int len = Math.max(1, plugin.getConfig().getInt("features.mine.install.length", 4));
        // forward direction -> axis
        String axis = Math.abs(p.getLocation().getDirection().getZ()) >= Math.abs(p.getLocation().getDirection().getX()) ? "Z" : "X";
        Location start = base.clone().add(p.getLocation().getDirection().setY(0).normalize());
        start.setY(base.getBlockY());
        for (int i=0;i<len;i++) {
            Location t = start.clone();
            if ("Z".equals(axis)) t.add(0,0,i); else t.add(i,0,0);
            w.getBlockAt(t).setType(Material.STONE, false);
        }
        setLoc(is.getId(),"mine", start);
        data.set(base(is.getId())+".mine.axis", axis);
        save();
        p.sendMessage(Text.color("&a광산 설치 완료")); 
        // marker at base
        placeMarker(w, base, "features.mine.marker", "GLOWSTONE");
    }

    public void installFarm(Player p){
        if (!canOperateHere(p)) return;
        IslandData is = ownedIsland(p); if (is==null) return;
        if (hasFarm(is.getId())){ p.sendMessage(Text.color("&c이미 설치된 농장이 있습니다.")); return; }
        Location origin = p.getLocation().getBlock().getLocation().add(p.getLocation().getDirection().setY(0).normalize());
        World w = origin.getWorld();
        // 3x3 farmland + crop
        for (int dx=-1; dx<=1; dx++){
            for (int dz=-1; dz<=1; dz++){
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
        if (!canOperateHere(p)) return false;
        IslandData is = ownedIsland(p); if (is==null) return false;
        int now = getMineLevel(is.getId());
        int max = plugin.getConfig().getInt("features.mine.max-level", 5);
        if (now >= max){ p.sendMessage(Text.color("&c광산 레벨이 최대입니다.")); return false; }
        // 비용/요구레벨은 기존 구현 사용 전제(여기선 표시만)
        setMineLevel(is.getId(), now+1);
        save();
        p.sendMessage(Text.color("&a광산 레벨 업! 현재: &f"+(now+1)));
        return true;
    }

    public boolean upgradeFarm(Player p){
        if (!canOperateHere(p)) return false;
        IslandData is = ownedIsland(p); if (is==null) return false;
        int now = getFarmLevel(is.getId());
        int max = plugin.getConfig().getInt("features.farm.max-level", 5);
        if (now >= max){ p.sendMessage(Text.color("&c농장 레벨이 최대입니다.")); return false; }
        setFarmLevel(is.getId(), now+1);
        save();
        p.sendMessage(Text.color("&a농장 레벨 업! 현재: &f"+(now+1)));
        return true;
    }

    /* ================== Remove ================== */
    public void removeMine(Player p){
        IslandData is = ownedIsland(p); if (is==null) return;
        Location start = getLoc(is.getId(),"mine"); if (start==null){ p.sendMessage(Text.color("&c설치된 광산이 없습니다.")); return; }
        World w = start.getWorld();
        int len = Math.max(1, plugin.getConfig().getInt("features.mine.install.length", 4));
        String axis = data.getString(base(is.getId())+".mine.axis", "X");
        for (int i=0;i<len;i++) {
            Location t = start.clone();
            if ("Z".equals(axis)) t.add(0,0,i); else t.add(i,0,0);
            w.getBlockAt(t).setType(Material.AIR, false);
        }
        data.set(base(is.getId())+".mine", null);
        save();
        p.sendMessage(Text.color("&a광산 제거 완료"));
    }

    public void removeFarm(Player p){
        IslandData is = ownedIsland(p); if (is==null) return;
        Location origin = getLoc(is.getId(),"farm"); if (origin==null){ p.sendMessage(Text.color("&c설치된 농장이 없습니다.")); return; }
        World w = origin.getWorld();
        for (int dx=-1; dx<=1; dx++){
            for (int dz=-1; dz<=1; dz++){
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
    public int getMineLevel(UUID id){ return data.getInt(base(id)+".mine.level", 0); }
    public int getFarmLevel(UUID id){ return data.getInt(base(id)+".farm.level", 0); }
    public void setMineLevel(UUID id, int v){ data.set(base(id)+".mine.level", v); }
    public void setFarmLevel(UUID id, int v){ data.set(base(id)+".farm.level", v); }

    /* ================== Static wrappers for callers ================== */
    public static int getMineLevelStatic(UUID id){ return INSTANCE!=null ? INSTANCE.getMineLevel(id) : 0; }
    public static int getFarmLevelStatic(UUID id){ return INSTANCE!=null ? INSTANCE.getFarmLevel(id) : 0; }

    /* ================== Optional helpers used elsewhere ================== */
    public boolean isIslandWorld(World world) { return world != null; } // safe default
    public boolean islandOwner(Player p) { return true; }              // safe default (override by reflection version)
    public boolean islandMember(Player p) { return true; }             // safe default


public int getFarmLevelByLoc(org.bukkit.Location loc){
    // naive: find any farm origin within 3x3; return 0 if none
    try{
        org.bukkit.configuration.ConfigurationSection sec = data.getConfigurationSection("islands");
        if (sec != null){
            for (String k : sec.getKeys(false)){
                java.util.UUID id = java.util.UUID.fromString(k);
                org.bukkit.Location origin = getLoc(id, "farm");
                if (origin != null && origin.getWorld()!=null && origin.getWorld().equals(loc.getWorld())){
                    if (Math.abs(loc.getBlockX()-origin.getBlockX())<=1 && Math.abs(loc.getBlockZ()-origin.getBlockZ())<=1){
                        return getFarmLevel(id);
                    }
                }
            }
        }
    }catch(Throwable ignore){}
    return 0;
}

public boolean isInAnyFarmRegion(org.bukkit.Location loc){
    try{
        org.bukkit.configuration.ConfigurationSection sec = data.getConfigurationSection("islands");
        if (sec != null){
            for (String k : sec.getKeys(false)){
                java.util.UUID id = java.util.UUID.fromString(k);
                org.bukkit.Location origin = getLoc(id, "farm");
                if (origin != null && origin.getWorld()!=null && origin.getWorld().equals(loc.getWorld())){
                    if (Math.abs(loc.getBlockX()-origin.getBlockX())<=1 && Math.abs(loc.getBlockZ()-origin.getBlockZ())<=1){
                        return true;
                    }
                }
            }
        }
    }catch(Throwable ignore){}
    return false;
}


    public int getMaxMineLevel(){
        return this.plugin.getConfig().getInt("features.mine.max-level", 5);
    }
    public int getMaxFarmLevel(){
        return this.plugin.getConfig().getInt("features.farm.max-level", 5);
    }

public static boolean islandOwnerStatic(org.bukkit.entity.Player p){
    return INSTANCE != null && INSTANCE.islandOwner(p);
}
public static boolean islandMemberStatic(org.bukkit.entity.Player p){
    return INSTANCE != null && INSTANCE.islandMember(p);
}
public static boolean isIslandWorldStatic(org.bukkit.World w){
    return INSTANCE != null && INSTANCE.isIslandWorld(w);
}
}
