
package com.signition.samskybridge.feature;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.util.Text;
import org.bukkit.*;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.UUID;

/**
 * Lightweight per-island features: Mine & Farm
 * - Levels persisted in features.yml (keeps DataStore untouched)
 * - Install commands place a small structure near player's location
 * - Upgrades only adjust internal level values; structure content can be regenerated via /섬 설치 again
 */
public class FeatureService {
    private final Main plugin;
    private final DataStore store;
    private final File file;
    private final FileConfiguration data;

    
private void setLoc(java.util.UUID id, String key, org.bukkit.Location loc){
    if (loc==null || loc.getWorld()==null) return;
    String b = "islands."+id.toString()+"."+key+".loc";
    data.set(b+".world", loc.getWorld().getName());
    data.set(b+".x", loc.getBlockX());
    data.set(b+".y", loc.getBlockY());
    data.set(b+".z", loc.getBlockZ());
    save();
}
private org.bukkit.Location getLoc(java.util.UUID id, String key){
    String b = "islands."+id.toString()+"."+key+".loc";
    String w = data.getString(b+".world", null);
    if (w == null) return null;
    org.bukkit.World world = plugin.getServer().getWorld(w);
    if (world == null) return null;
    int x = data.getInt(b+".x", 0);
    int y = data.getInt(b+".y", 0);
    int z = data.getInt(b+".z", 0);
    return new org.bukkit.Location(world, x, y, z);
}

public FeatureService(Main plugin, DataStore store){
        this.plugin = plugin;
        this.store = store;
        this.file = new File(plugin.getDataFolder(), "features.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    public void save(){
        try { data.save(file);} catch(Exception ignored){}
    }

    private String base(UUID id){ return "islands."+id.toString(); }
    public int getMineLevel(UUID id){ return data.getInt(base(id)+".mine.level", 0); }
    public int getFarmLevel(UUID id){ return data.getInt(base(id)+".farm.level", 0); }
    public void setMineLevel(UUID id, int v){ data.set(base(id)+".mine.level", v); save(); }
    public void setFarmLevel(UUID id, int v){ data.set(base(id)+".farm.level", v); save(); }

    /* ---------------- Installation ---------------- */

    public void installMine(Player p){
        Location loc = p.getLocation().getBlock().getLocation();
        World w = loc.getWorld();
        if (w == null) return;
        int size = Math.max(3, plugin.getConfig().getInt("features.mine.install.size", 5)); // odd
        int half = size/2;
        Material[] ores = new Material[] { 
        com.signition.samskybridge.data.IslandData is = ownedIsland(p);
Material.COAL_ORE, Material.IRON_ORE, Material.REDSTONE_ORE, Material.GOLD_ORE };
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
        p.sendMessage(Text.color("&a섬 광산이 설치되었습니다. &7(간단 구조)"));
    }

    public void installFarm(Player p){
        Location loc = p.getLocation().getBlock().getLocation();
        World w = loc.getWorld();
        if (w == null) return;
        int size = Math.max(5, plugin.getConfig().getInt("features.farm.install.size", 7)); // odd
        int half = size/2;
        for (int x=-half;x<=half;x++){
            
        com.signition.samskybridge.data.IslandData is = ownedIsland(p);
for (int z=-half;z<=half;z++){
                Location s = loc.clone().add(x,0,z);
                w.getBlockAt(s).setType(Material.DIRT,false);
                w.getBlockAt(s.clone().add(0,1,0)).setType(Material.AIR,false);
                w.getBlockAt(s).setType(Material.FARMLAND,false);
                Material crop = (Math.abs(x+z)%2==0)? Material.WHEAT : Material.CARROTS;
                w.getBlockAt(s.clone().add(0,1,0)).setType(crop,false);
            }
        }
        p.sendMessage(Text.color("&a섬 농장이 설치되었습니다. &7(간단 구조)"));
    }

    /* ---------------- Upgrade APIs (called from GUI) ---------------- */
    public boolean upgradeMine(Player p){
        
        int now = getMineLevel(is.getId());
        int max = plugin.getConfig().getInt("features.mine.max-level", 5);
        if (now >= max){ p.sendMessage(Text.color("&c광산 레벨이 최대입니다.")); return false; }
        int needLv = plugin.getConfig().getInt("features.mine.require.base", 3) + now * plugin.getConfig().getInt("features.mine.require.per-step", 1);
        if (is.getLevel() < needLv){ p.sendMessage(Text.color("&c요구 섬 레벨: &f"+needLv)); return false; }
        double cost = plugin.getConfig().getDouble("features.mine.cost.base", 5000.0) * Math.pow(plugin.getConfig().getDouble("features.mine.cost.multiplier", 1.25), now);
        if (!plugin.getVault().withdraw(p.getName(), cost)){ p.sendMessage(Text.color("&c잔액 부족. 필요: &f"+(long)cost)); return false; }
        setMineLevel(is.getId(), now+1);
        p.sendMessage(Text.color("&a광산 레벨 업! &7현재: &f"+(now+1)));
        com.signition.samskybridge.data.
        if (plugin.getConfig().getBoolean("features.require.owner-only", true) && is == null){ p.sendMessage(com.signition.samskybridge.util.Text.color(plugin.getConfig().getString("features.messages.owner-only","&c섬장만 사용할 수 있습니다."))); return false; }
        return true;
    }

    public boolean upgradeFarm(Player p){
        
        int now = getFarmLevel(is.getId());
        int max = plugin.getConfig().getInt("features.farm.max-level", 5);
        if (now >= max){ p.sendMessage(Text.color("&c농장 레벨이 최대입니다.")); return false; }
        int needLv = plugin.getConfig().getInt("features.farm.require.base", 2) + now * plugin.getConfig().getInt("features.farm.require.per-step", 1);
        if (is.getLevel() < needLv){ p.sendMessage(Text.color("&c요구 섬 레벨: &f"+needLv)); return false; }
        double cost = plugin.getConfig().getDouble("features.farm.cost.base", 4000.0) * Math.pow(plugin.getConfig().getDouble("features.farm.cost.multiplier", 1.2), now);
        if (!plugin.getVault().withdraw(p.getName(), cost)){ p.sendMessage(Text.color("&c잔액 부족. 필요: &f"+(long)cost)); return false; }
        setFarmLevel(is.getId(), now+1);
        p.sendMessage(Text.color("&a농장 레벨 업! &7현재: &f"+(now+1)));
        com.signition.samskybridge.data.
        if (plugin.getConfig().getBoolean("features.require.owner-only", true) && is == null){ p.sendMessage(com.signition.samskybridge.util.Text.color(plugin.getConfig().getString("features.messages.owner-only","&c섬장만 사용할 수 있습니다."))); return false; }
        return true;
    }


public void removeMine(org.bukkit.entity.Player p){
    if (!canOperateHere(p)) return;
    com.signition.samskybridge.data.
    if (is == null){ 
        com.signition.samskybridge.data.IslandData is = ownedIsland(p);
p.sendMessage(com.signition.samskybridge.util.Text.color("&c섬장만 사용할 수 있습니다.")); return; }
    org.bukkit.Location loc = getLoc(is.getId(), "mine");
    if (loc == null){ p.sendMessage(com.signition.samskybridge.util.Text.color("&c설치된 광산 위치가 없습니다.")); return; }
    org.bukkit.World w = loc.getWorld();
    int size = Math.max(3, plugin.getConfig().getInt("features.mine.install.size", 5));
    int half = size/2;
    for (int x=-half;x<=half;x++){
        for (int y=0;y<=2;y++){
            for (int z=-half;z<=half;z++){
                w.getBlockAt(loc.clone().add(x,y,z)).setType(org.bukkit.Material.AIR, false);
            }
        }
    }
    p.sendMessage(com.signition.samskybridge.util.Text.color("&a광산이 제거되었습니다."));
}

public void removeFarm(org.bukkit.entity.Player p){
    if (!canOperateHere(p)) return;
    com.signition.samskybridge.data.
    if (is == null){ 
        com.signition.samskybridge.data.IslandData is = ownedIsland(p);
p.sendMessage(com.signition.samskybridge.util.Text.color("&c섬장만 사용할 수 있습니다.")); return; }
    org.bukkit.Location loc = getLoc(is.getId(), "farm");
    if (loc == null){ p.sendMessage(com.signition.samskybridge.util.Text.color("&c설치된 농장 위치가 없습니다.")); return; }
    org.bukkit.World w = loc.getWorld();
    int size = Math.max(5, plugin.getConfig().getInt("features.farm.install.size", 7));
    int half = size/2;
    for (int x=-half;x<=half;x++){
        for (int z=-half;z<=half;z++){
            org.bukkit.Location s = loc.clone().add(x,0,z);
            w.getBlockAt(s).setType(org.bukkit.Material.AIR,false);
            w.getBlockAt(s.clone().add(0,1,0)).setType(org.bukkit.Material.AIR,false);
        }
    }
    p.sendMessage(com.signition.samskybridge.util.Text.color("&a농장이 제거되었습니다."));
}

}
