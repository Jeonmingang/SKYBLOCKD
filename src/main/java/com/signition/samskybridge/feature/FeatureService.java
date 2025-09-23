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
import java.util.UUID;

public class FeatureService {
    private final Main plugin;
    private final DataStore store;
    private final File file;
    private final FileConfiguration data;

    public FeatureService(Main plugin, DataStore store){
        this.plugin = plugin;
        this.store = store;
        this.file = new File(plugin.getDataFolder(), "features.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    public void save(){
        try { data.save(file);} catch (IOException ignored){}
    }

    /* ---------------- Owner / Guards ---------------- */
    private IslandData ownedIsland(Player p){
        return store.findByOwnerName(p.getName());
    }

    private boolean canOperateHere(Player p){
        java.util.List<String> worlds = plugin.getConfig().getStringList("features.require.island-worlds");
        if (worlds != null && !worlds.isEmpty()){
            String wn = p.getWorld()!=null ? p.getWorld().getName() : "";
            if (!worlds.contains(wn)){
                p.sendMessage(Text.color(plugin.getConfig().getString("features.messages.only-on-island-world","&c이 명령은 스카이블록 월드에서만 사용 가능합니다.")));
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

    /* ---------------- Location persistence ---------------- */
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

    /* ---------------- Install ---------------- */
    public void installMine(Player p){
        if (!canOperateHere(p)) return;
        IslandData is = ownedIsland(p);
        if (is == null){ return; }
        Location loc = p.getLocation().getBlock().getLocation();
        World w = loc.getWorld();
        int size = Math.max(3, plugin.getConfig().getInt("features.mine.install.size", 5));
        int half = size/2;
        Material[] ores = new Material[] { Material.COAL_ORE, Material.IRON_ORE, Material.REDSTONE_ORE, Material.GOLD_ORE };
        int o = 0;
        for (int x=-half;x<=half;x++){
            for (int y=0;y<=2;y++){
                for (int z=-half;z<=half;z++){
                    Location t = loc.clone().add(x,y,z);
                    if (y==0){
                        w.getBlockAt(t).setType(Material.BEDROCK,false);
                    } else if (y==1){
                        w.getBlockAt(t).setType(ores[o%ores.length], false);
                        o++;
                    } else {
                        w.getBlockAt(t).setType(Material.STONE, false);
                    }
                }
            }
        }
        setLoc(is.getId(), "mine", loc);
        p.sendMessage(Text.color("&a섬 광산이 설치되었습니다."));
    }

    public void installFarm(Player p){
        if (!canOperateHere(p)) return;
        IslandData is = ownedIsland(p);
        if (is == null){ return; }
        Location loc = p.getLocation().getBlock().getLocation();
        World w = loc.getWorld();
        int size = Math.max(5, plugin.getConfig().getInt("features.farm.install.size", 7));
        int half = size/2;
        for (int x=-half;x<=half;x++){
            for (int z=-half;z<=half;z++){
                Location s = loc.clone().add(x,0,z);
                w.getBlockAt(s).setType(Material.DIRT,false);
                w.getBlockAt(s).setType(Material.FARMLAND,false);
                w.getBlockAt(s.clone().add(0,1,0)).setType(Material.WHEAT,false);
            }
        }
        setLoc(is.getId(), "farm", loc);
        p.sendMessage(Text.color("&a섬 농장이 설치되었습니다."));
    }

    /* ---------------- Upgrade ---------------- */
    public boolean upgradeMine(Player p){
        if (!canOperateHere(p)) return false;
        IslandData is = ownedIsland(p);
        if (is == null){ return false; }
        int now = getMineLevel(is.getId());
        int max = plugin.getConfig().getInt("features.mine.max-level", 5);
        if (now >= max){ p.sendMessage(Text.color("&c광산 레벨이 최대입니다.")); return false; }
        int needLv = plugin.getConfig().getInt("features.mine.require.base", 3) + now * plugin.getConfig().getInt("features.mine.require.per-step", 1);
        if (is.getLevel() < needLv){ p.sendMessage(Text.color("&c요구 섬 레벨: &f"+needLv)); return false; }
        double cost = plugin.getConfig().getDouble("features.mine.cost.base", 5000.0) * Math.pow(plugin.getConfig().getDouble("features.mine.cost.multiplier", 1.25), now);
        if (!plugin.getVault().withdraw(p.getName(), cost)){ p.sendMessage(Text.color("&c잔액 부족. 필요: &f"+(long)cost)); return false; }
        setMineLevel(is.getId(), now+1);
        p.sendMessage(Text.color("&a광산 레벨 업! &7현재: &f"+(now+1)));
        return true;
    }

    public boolean upgradeFarm(Player p){
        if (!canOperateHere(p)) return false;
        IslandData is = ownedIsland(p);
        if (is == null){ return false; }
        int now = getFarmLevel(is.getId());
        int max = plugin.getConfig().getInt("features.farm.max-level", 5);
        if (now >= max){ p.sendMessage(Text.color("&c농장 레벨이 최대입니다.")); return false; }
        int needLv = plugin.getConfig().getInt("features.farm.require.base", 2) + now * plugin.getConfig().getInt("features.farm.require.per-step", 1);
        if (is.getLevel() < needLv){ p.sendMessage(Text.color("&c요구 섬 레벨: &f"+needLv)); return false; }
        double cost = plugin.getConfig().getDouble("features.farm.cost.base", 4000.0) * Math.pow(plugin.getConfig().getDouble("features.farm.cost.multiplier", 1.2), now);
        if (!plugin.getVault().withdraw(p.getName(), cost)){ p.sendMessage(Text.color("&c잔액 부족. 필요: &f"+(long)cost)); return false; }
        setFarmLevel(is.getId(), now+1);
        p.sendMessage(Text.color("&a농장 레벨 업! &7현재: &f"+(now+1)));
        return true;
    }

    /* ---------------- Remove ---------------- */
    public void removeMine(Player p){
        if (!canOperateHere(p)) return;
        IslandData is = ownedIsland(p);
        if (is == null){ return; }
        Location loc = getLoc(is.getId(), "mine");
        if (loc == null){ p.sendMessage(Text.color("&c설치된 광산 위치가 없습니다.")); return; }
        World w = loc.getWorld();
        int size = Math.max(3, plugin.getConfig().getInt("features.mine.install.size", 5));
        int half = size/2;
        for (int x=-half;x<=half;x++){
            for (int y=0;y<=2;y++){
                for (int z=-half;z<=half;z++){
                    w.getBlockAt(loc.clone().add(x,y,z)).setType(Material.AIR, false);
                }
            }
        }
        p.sendMessage(Text.color("&a광산이 제거되었습니다."));
    }

    public void removeFarm(Player p){
        if (!canOperateHere(p)) return;
        IslandData is = ownedIsland(p);
        if (is == null){ return; }
        Location loc = getLoc(is.getId(), "farm");
        if (loc == null){ p.sendMessage(Text.color("&c설치된 농장 위치가 없습니다.")); return; }
        World w = loc.getWorld();
        int size = Math.max(5, plugin.getConfig().getInt("features.farm.install.size", 7));
        int half = size/2;
        for (int x=-half;x<=half;x++){
            for (int z=-half;z<=half;z++){
                Location s = loc.clone().add(x,0,z);
                w.getBlockAt(s).setType(Material.AIR,false);
                w.getBlockAt(s.clone().add(0,1,0)).setType(Material.AIR,false);
            }
        }
        p.sendMessage(Text.color("&a농장이 제거되었습니다."));
    }

    /* ---------------- Levels (YAML) ---------------- */
    public int getMineLevel(UUID id){ return data.getInt(base(id)+".mine.level", 0); }
    public int getFarmLevel(UUID id){ return data.getInt(base(id)+".farm.level", 0); }
    public void setMineLevel(UUID id, int v){ data.set(base(id)+".mine.level", v); save(); }
    public void setFarmLevel(UUID id, int v){ data.set(base(id)+".farm.level", v); save(); }

    // ----- Region helpers -----
    public boolean isInMineRegion(org.bukkit.entity.Player p, org.bukkit.Location loc){
        com.signition.samskybridge.data.IslandData is = ownedIsland(p);
        if (is == null) return false;
        org.bukkit.Location c = getLoc(is.getId(), "mine");
        if (c == null || loc.getWorld()==null || c.getWorld()==null) return false;
        if (!c.getWorld().getName().equals(loc.getWorld().getName())) return false;
        int size = Math.max(3, plugin.getConfig().getInt("features.mine.install.size", 5));
        int half = size/2;
        return Math.abs(loc.getBlockX()-c.getBlockX())<=half && Math.abs(loc.getBlockY()-c.getBlockY())<=2 && Math.abs(loc.getBlockZ()-c.getBlockZ())<=half;
    }
    public boolean isInFarmRegion(org.bukkit.entity.Player p, org.bukkit.Location loc){
        com.signition.samskybridge.data.IslandData is = ownedIsland(p);
        if (is == null) return false;
        org.bukkit.Location c = getLoc(is.getId(), "farm");
        if (c == null || loc.getWorld()==null || c.getWorld()==null) return false;
        if (!c.getWorld().getName().equals(loc.getWorld().getName())) return false;
        int size = Math.max(5, plugin.getConfig().getInt("features.farm.install.size", 7));
        int half = size/2;
        return Math.abs(loc.getBlockX()-c.getBlockX())<=half && Math.abs(loc.getBlockZ()-c.getBlockZ())<=half;
    }

    // ----- Weighted picks -----
    private java.util.List<org.bukkit.Material> parseWeighted(java.util.List<String> list){
        java.util.List<org.bukkit.Material> bag = new java.util.ArrayList<>();
        if (list == null) return bag;
        for (String s : list){
            try{
                String[] sp = s.trim().split(":");
                org.bukkit.Material m = org.bukkit.Material.valueOf(sp[0].trim());
                int w = (sp.length>1? Integer.parseInt(sp[1].trim()) : 1);
                for (int i=0;i<Math.max(1,w);i++) bag.add(m);
            }catch(Throwable ignored){}
        }
        return bag;
    }
    public org.bukkit.Material pickOreFor(org.bukkit.entity.Player p){
        com.signition.samskybridge.data.IslandData is = ownedIsland(p);
        if (is == null) return null;
        int lv = getMineLevel(is.getId());
        java.util.List<String> table = plugin.getConfig().getStringList("features.mine.ores."+lv);
        java.util.List<org.bukkit.Material> bag = parseWeighted(table);
        if (bag.isEmpty()) return org.bukkit.Material.COAL_ORE;
        return bag.get(new java.util.Random().nextInt(bag.size()));
    }
    public org.bukkit.Material pickCropFor(org.bukkit.entity.Player p, org.bukkit.Material fallback){
        com.signition.samskybridge.data.IslandData is = ownedIsland(p);
        if (is == null) return fallback;
        int lv = getFarmLevel(is.getId());
        java.util.List<String> table = plugin.getConfig().getStringList("features.farm.crops."+lv);
        java.util.List<org.bukkit.Material> bag = parseWeighted(table);
        if (bag.isEmpty()) return fallback;
        return bag.get(new java.util.Random().nextInt(bag.size()));
    }
    
}