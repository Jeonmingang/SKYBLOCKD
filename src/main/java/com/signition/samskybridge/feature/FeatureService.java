package com.signition.samskybridge.feature;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.util.Text;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FeatureService {
    private final Main plugin;
    private final DataStore store;
    private final File file;
    private final FileConfiguration data;
    private final Random rng = new Random();

    public FeatureService(Main plugin, DataStore store){
        this.plugin = plugin;
        this.store = store;
        this.file = new File(plugin.getDataFolder(), "features.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    public void save(){ try { data.save(file); } catch (IOException ignored) {} }

    /* ====== Helpers ====== */
    private IslandData ownedIsland(Player p){ return store.findByOwnerName(p.getName()); }

    private boolean canOperateHere(Player p){
        // only in island worlds
        List<String> worlds = plugin.getConfig().getStringList("features.require.island-worlds");
        if (worlds != null && !worlds.isEmpty()){
            String wn = p.getWorld()!=null ? p.getWorld().getName() : "";
            if (!worlds.contains(wn)){
                p.sendMessage(Text.color(plugin.getConfig().getString("features.messages.only-on-island-world","&c섬 월드에서만 사용할 수 있습니다.")));
                return false;
            }
        }
        // owner only
        if (plugin.getConfig().getBoolean("features.require.owner-only", true) && ownedIsland(p)==null){
            p.sendMessage(Text.color(plugin.getConfig().getString("features.messages.owner-only","&c섬장만 사용할 수 있습니다.")));
            return false;
        }
        return true;
    }

    private String base(UUID id){ return "islands."+id.toString(); }
    private void setLoc(UUID id, String key, Location loc){
        if (loc==null || loc.getWorld()==null) return;
        String b = base(id)+"."+key+".loc";
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

    /* ====== Region checks ====== */
    public boolean hasMine(UUID id){ return getLoc(id,"mine") != null; }
    public boolean hasFarm(UUID id){ return getLoc(id,"farm") != null; }

    public boolean isInMineRegion(Player p, Location loc){
        IslandData is = ownedIsland(p);
        if (is == null) return false;
        Location origin = getLoc(is.getId(), "mine");
        if (origin==null) return false;
        if (!Objects.equals(origin.getWorld(), loc.getWorld())) return false;
        String axis = data.getString(base(is.getId())+".mine.axis","X");
        int len = Math.max(1, plugin.getConfig().getInt("features.mine.install.length", 4));
        // tight 1x1 tube along axis at y of origin
        int y = origin.getBlockY();
        if (loc.getBlockY() != y) return false;
        if ("Z".equalsIgnoreCase(axis)){
            int min = Math.min(origin.getBlockZ(), origin.getBlockZ() + (len-1));
            int max = Math.max(origin.getBlockZ(), origin.getBlockZ() + (len-1));
            return loc.getBlockX()==origin.getBlockX() && loc.getBlockZ()>=min && loc.getBlockZ()<=max;
        } else {
            int min = Math.min(origin.getBlockX(), origin.getBlockX() + (len-1));
            int max = Math.max(origin.getBlockX(), origin.getBlockX() + (len-1));
            return loc.getBlockZ()==origin.getBlockZ() && loc.getBlockX()>=min && loc.getBlockX()<=max;
        }
    }

    public boolean isInFarmRegion(Player p, Location loc){
        IslandData is = ownedIsland(p);
        if (is == null) return false;
        Location origin = getLoc(is.getId(), "farm");
        if (origin==null) return false;
        if (!Objects.equals(origin.getWorld(), loc.getWorld())) return false;
        // 3x3 square centered on origin at y+1 for crops, y for farmland
        int half = 1; // 3x3
        return abs(loc.getBlockX()-origin.getBlockX())<=half && abs(loc.getBlockZ()-origin.getBlockZ())<=half;
    }
    private int abs(int v){ return v<0?-v:v; }

    /* ====== Weighted tables ====== */
    private List<Material> parseWeighted(List<String> list){
        List<Material> bag = new ArrayList<>();
        if (list==null) return bag;
        for (String s : list){
            try {
                String[] sp = s.trim().split(":");
                Material m = Material.valueOf(sp[0].trim());
                int w = (sp.length>1? Integer.parseInt(sp[1].trim()) : 1);
                for (int i=0;i<Math.max(1,w);i++) bag.add(m);
            } catch(Throwable ignored) { }
        }
        return bag;
    }
    public Material pickOreFor(Player p){
        IslandData is = ownedIsland(p);
        if (is==null) return Material.COAL_ORE;
        int lv = getMineLevel(is.getId());
        List<Material> bag = parseWeighted(plugin.getConfig().getStringList("features.mine.ores."+lv));
        return bag.isEmpty()? Material.COAL_ORE : bag.get(rng.nextInt(bag.size()));
    }
    public Material pickCropFor(Player p, Material fallback){
        IslandData is = ownedIsland(p);
        if (is==null) return fallback;
        int lv = getFarmLevel(is.getId());
        List<Material> bag = parseWeighted(plugin.getConfig().getStringList("features.farm.crops."+lv));
        return bag.isEmpty()? fallback : bag.get(rng.nextInt(bag.size()));
    }

    /* ====== Install (one per island) ====== */
    public void installMine(Player p){
        if (!canOperateHere(p)) return;
        IslandData is = ownedIsland(p); if (is==null) return;
        if (hasMine(is.getId())){ p.sendMessage(Text.color("&c이미 설치된 광산이 있습니다. 먼저 제거하세요.")); return; }

        Location baseLoc = p.getLocation().getBlock().getLocation();
        // BentoBox/BSkyBlock: ensure placement inside own island
        if (!plugin.getBento().isInOwnIsland(p, baseLoc)){
            p.sendMessage(Text.color(plugin.getConfig().getString("features.messages.only-own-island","&c자신의 섬 내부에서만 설치할 수 있습니다.")));
            return;
        }

        // place starting one block in front, aligned to look axis at player's feet Y
        String axis = Math.abs(p.getLocation().getYaw())%180<45 || Math.abs(p.getLocation().getYaw())%180>135 ? "Z" : "X";
        // Better: snap to nearest cardinal
        float yaw = p.getLocation().getYaw();
        yaw = (yaw % 360 + 360) % 360;
        if (yaw>=45 && yaw<135) axis = "X";      // south -> +Z; use Z axis; but we store axis we iterate on
        else if (yaw>=135 && yaw<225) axis = "Z";
        else if (yaw>=225 && yaw<315) axis = "X";
        else axis = "Z";

        int len = Math.max(1, plugin.getConfig().getInt("features.mine.install.length", 4));
        World w = baseLoc.getWorld();
        Location start = baseLoc.clone().add(p.getLocation().getDirection().setY(0).normalize());
        start.setY(baseLoc.getBlockY());

        // Lay a row of ore nodes exactly along axis, 1x1xlen
        Material preview = Material.STONE;
        for (int i=0;i<len;i++) {
            Location t = start.clone();
            if ("Z".equals(axis)) t.add(0, 0, i);
            else t.add(i, 0, 0);
            Block b = w.getBlockAt(t);
            b.setType(preview, false);
        }
        // Persist origin (start) and axis
        setLoc(is.getId(),"mine", start);
        data.set(base(is.getId())+".mine.axis", axis);
        save();
        p.sendMessage(Text.color("&a광산이 설치되었습니다. &7방향: &f"+axis+" &7길이: &f"+len));
    }

    public 
void installFarm(Player p){
    if (!canOperateHere(p)) return;
    IslandData is = ownedIsland(p); if (is==null){ p.sendMessage(Text.color("&c섬을 소유한 플레이어만 설치할 수 있습니다.")); return; }
    if (hasFarm(is.getId())){ p.sendMessage(Text.color("&c이미 농장이 설치되어 있습니다.")); return; }
    
// determine a center 2 blocks in front of the player, snapped to block coords
org.bukkit.Location origin = p.getLocation().getBlock().getLocation();
org.bukkit.util.Vector dir = p.getLocation().getDirection();
int fx = Math.abs(dir.getX()) >= Math.abs(dir.getZ()) ? (dir.getX() >= 0 ? 1 : -1) : 0;
int fz = Math.abs(dir.getZ()) >  Math.abs(dir.getX()) ? (dir.getZ() >= 0 ? 1 : -1) : 0;
origin.add(fx*2, 0, fz*2);
origin.setY(origin.getWorld().getHighestBlockYAt(origin)+1);
// place on surface (safer on CatServer)
    setLoc(is.getId(), "farm", origin);
    int seedCost = plugin.getConfig().getInt("features.farm.install.seed-cost-per-plot", 1);
    boolean requireSeeds = plugin.getConfig().getBoolean("features.farm.install.require-seeds", true);
    String pref = plugin.getConfig().getString("features.farm.install.initial-crop", "AUTO").toUpperCase();
    java.util.List<Material> allowed = parseWeighted(plugin.getConfig().getStringList("features.farm.crops."+getFarmLevel(is.getId())));
    java.util.Set<Material> seen = new java.util.HashSet<Material>(allowed);
    // Map crop -> seed item
    java.util.Map<Material, Material> seedMap = new java.util.HashMap<Material, Material>();
    seedMap.put(Material.WHEAT, Material.WHEAT_SEEDS);
    seedMap.put(Material.CARROTS, Material.CARROT);
    seedMap.put(Material.POTATOES, Material.POTATO);
    // Choose target crop to plant
    Material targetCrop = Material.WHEAT;
    if ("AUTO".equals(pref)){
        // pick first allowed crop the player has enough seeds for
        for (Material c : new java.util.LinkedHashSet<Material>(seen)){
            Material seed = seedMap.get(c);
            if (seed == null) continue;
            if (!requireSeeds || countItem(p, seed) >= 9*seedCost){ targetCrop = c; break; }
        }
    } else {
        try { targetCrop = Material.valueOf(pref); } catch (Throwable ignored) {}
        if (!seedMap.containsKey(targetCrop)) targetCrop = Material.WHEAT;
    }
    Material seedItem = seedMap.get(targetCrop);
    if (requireSeeds && (seedItem == null || countItem(p, seedItem) < 9*seedCost)){
        p.sendMessage(Text.color("&c씨앗이 부족합니다! 필요: &f"+(9*seedCost)+"x "+(seedItem!=null? seedItem.name() : "SEED")));
        // rollback stored loc if we didn't actually place
        setLoc(is.getId(), "farm", null);
        return;
    }
    // Build 3x3: farmland at y, crop at y+1 facing player's look direction
    int[][] offsets = new int[][]{ {-1,-1},{0,-1},{1,-1},{-1,0},{0,0},{1,0},{-1,1},{0,1},{1,1} };
    for (int[] d : offsets){
        org.bukkit.Location fl = origin.clone().add(d[0], 0, d[1]);
        fl.getBlock().setType(Material.FARMLAND, false);
        org.bukkit.Location cl = origin.clone().add(d[0], 1, d[1]);
        cl.getBlock().setType(targetCrop, false);
        // consume seed per plot if required
        if (requireSeeds && seedItem != null){
            for (int i2=0;i2<seedCost;i2++) consumeOne(p, seedItem);
        }
    }
    save();
    p.sendMessage(Text.color("&a3x3 농장이 설치되었습니다. &7초기 작물: &f"+targetCrop.name()));
}


    /* ====== Upgrade (unchanged public surface) ====== */
    public boolean upgradeMine(Player p){
        if (!canOperateHere(p)) return false;
        IslandData is = ownedIsland(p); if (is==null) return false;
        int now = getMineLevel(is.getId());
        int max = plugin.getConfig().getInt("features.mine.max-level", 5);
        if (now>=max){ p.sendMessage(Text.color("&c광산 레벨이 최대입니다.")); return false; }
        int needLv = plugin.getConfig().getInt("features.mine.require.base", 3) + now*plugin.getConfig().getInt("features.mine.require.per-step", 1);
        if (is.getLevel() < needLv){ p.sendMessage(Text.color("&c요구 섬 레벨: &f"+needLv)); return false; }
        double cost = plugin.getConfig().getDouble("features.mine.cost.base", 5000.0) * Math.pow(plugin.getConfig().getDouble("features.mine.cost.multiplier", 1.25), now);
        if (!plugin.getVault().withdraw(p.getName(), cost)){ p.sendMessage(Text.color("&c잔액 부족. 필요: &f"+(long)cost)); return false; }
        setMineLevel(is.getId(), now+1);
        p.sendMessage(Text.color("&a광산 레벨 업! &7현재: &f"+(now+1)));
        return true;
    }

    public boolean upgradeFarm(Player p){
        if (!canOperateHere(p)) return false;
        IslandData is = ownedIsland(p); if (is==null) return false;
        int now = getFarmLevel(is.getId());
        int max = plugin.getConfig().getInt("features.farm.max-level", 5);
        if (now>=max){ p.sendMessage(Text.color("&c농장 레벨이 최대입니다.")); return false; }
        int needLv = plugin.getConfig().getInt("features.farm.require.base", 2) + now*plugin.getConfig().getInt("features.farm.require.per-step", 1);
        if (is.getLevel() < needLv){ p.sendMessage(Text.color("&c요구 섬 레벨: &f"+needLv)); return false; }
        double cost = plugin.getConfig().getDouble("features.farm.cost.base", 4000.0) * Math.pow(plugin.getConfig().getDouble("features.farm.cost.multiplier", 1.2), now);
        if (!plugin.getVault().withdraw(p.getName(), cost)){ p.sendMessage(Text.color("&c잔액 부족. 필요: &f"+(long)cost)); return false; }
        setFarmLevel(is.getId(), now+1);
        p.sendMessage(Text.color("&a농장 레벨 업! &7현재: &f"+(now+1)));
        return true;
    }

    /* ====== Remove ====== */
    public void removeMine(Player p){
        if (!canOperateHere(p)) return;
        IslandData is = ownedIsland(p); if (is==null) return;
        Location start = getLoc(is.getId(), "mine");
        if (start==null){ p.sendMessage(Text.color("&c설치된 광산이 없습니다.")); return; }
        int len = Math.max(1, plugin.getConfig().getInt("features.mine.install.length", 4));
        String axis = data.getString(base(is.getId())+".mine.axis","X");
        World w = start.getWorld();
        for (int i=0;i<len;i++) {
            Location t = start.clone();
            if ("Z".equals(axis)) t.add(0,0,i); else t.add(i,0,0);
            w.getBlockAt(t).setType(Material.AIR,false);
        }
        data.set(base(is.getId())+".mine", null);
        save();
        p.sendMessage(Text.color("&a광산이 제거되었습니다."));
    }

    public void removeFarm(Player p){
        if (!canOperateHere(p)) return;
        IslandData is = ownedIsland(p); if (is==null) return;
        Location c = getLoc(is.getId(), "farm");
        if (c==null){ p.sendMessage(Text.color("&c설치된 농장이 없습니다.")); return; }
        World w = c.getWorld();
        for (int dx=-1; dx<=1; dx++){ for (int dz=-1; dz<=1; dz++){ 
            Location s = c.clone().add(dx,0,dz);
            w.getBlockAt(s).setType(Material.AIR,false);
            w.getBlockAt(s.clone().add(0,1,0)).setType(Material.AIR,false);
        }}
        data.set(base(is.getId())+".farm", null);
        save();
        p.sendMessage(Text.color("&a농장이 제거되었습니다."));
    }

    /* ====== Levels (persist) ====== */
    public int getMineLevel(UUID id){ return data.getInt(base(id)+".mine.level", 0); }
    public int getFarmLevel(UUID id){ return data.getInt(base(id)+".farm.level", 0); }
    public void setMineLevel(UUID id, int v){ data.set(base(id)+".mine.level", v); save(); }
    public void setFarmLevel(UUID id, int v){ data.set(base(id)+".farm.level", v); save(); }

/** Check if a location is inside ANY island's farm region (3x3 around stored origin). */
public boolean isInAnyFarmRegion(org.bukkit.Location loc){
    if (loc == null || loc.getWorld() == null) return false;
    org.bukkit.configuration.ConfigurationSection sec = data.getConfigurationSection("islands");
    if (sec == null) return false;
    for (String key : sec.getKeys(false)){
        try {
            java.util.UUID id = java.util.UUID.fromString(key);
            org.bukkit.Location origin = getLoc(id, "farm");
            if (origin == null || origin.getWorld() == null) continue;
            if (!origin.getWorld().equals(loc.getWorld())) continue;
            int half = 1;
            if (Math.abs(loc.getBlockX() - origin.getBlockX()) <= half &&
                Math.abs(loc.getBlockZ() - origin.getBlockZ()) <= half){
                return true;
            }
        } catch (Throwable ignored){}
    }
    return false;
}
/** Get farm level by block location. Returns 0 if not in a known farm. */
public int getFarmLevelByLoc(org.bukkit.Location loc){
    if (loc == null || loc.getWorld() == null) return 0;
    org.bukkit.configuration.ConfigurationSection sec = data.getConfigurationSection("islands");
    if (sec == null) return 0;
    for (String key : sec.getKeys(false)){
        try {
            java.util.UUID id = java.util.UUID.fromString(key);
            org.bukkit.Location origin = getLoc(id, "farm");
            if (origin == null || origin.getWorld() == null) continue;
            if (!origin.getWorld().equals(loc.getWorld())) continue;
            int half = 1;
            if (Math.abs(loc.getBlockX() - origin.getBlockX()) <= half &&
                Math.abs(loc.getBlockZ() - origin.getBlockZ()) <= half){
                return getFarmLevel(id);
            }
        } catch (Throwable ignored){}
    }
    return 0;
}

private int countItem(Player p, Material mat){
    int n = 0;
    for (org.bukkit.inventory.ItemStack it : p.getInventory().getContents()){
        if (it != null && it.getType() == mat) n += it.getAmount();
    }
    return n;
}
private boolean consumeOne(Player p, Material mat){
    org.bukkit.inventory.ItemStack[] arr = p.getInventory().getContents();
    for (int i=0;i<arr.length;i++){
        org.bukkit.inventory.ItemStack it = arr[i];
        if (it != null && it.getType()==mat && it.getAmount()>0){
            it.setAmount(it.getAmount()-1);
            arr[i] = it.getAmount()>0 ? it : null;
            p.getInventory().setContents(arr);
            p.updateInventory();
            return true;
        }
    }
    return false;
}



public static boolean islandOwner(org.bukkit.entity.Player p) {
    try {
        return com.signition.samskybridge.compat.BentoCompat.isOwner(p);
    } catch (Throwable t) { return false; }
}
public static boolean islandMember(org.bukkit.entity.Player p) {
    try {
        return com.signition.samskybridge.compat.BentoCompat.isMember(p);
    } catch (Throwable t) { return false; }
}
public static boolean isIslandWorld(org.bukkit.World w) {
    try {
        return com.signition.samskybridge.compat.BentoCompat.isIslandWorld(w);
    } catch (Throwable t) { return true; }
}

}
