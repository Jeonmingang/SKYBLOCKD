
package com.signition.samskybridge.feature;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.util.Text;
import org.bukkit.*;
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
        Material[] ores = new Material[] { Material.COAL_ORE, Material.IRON_ORE, Material.COPPER_ORE, Material.GOLD_ORE };
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
            for (int z=-half;z<=half;z++){
                Location s = loc.clone().add(x,0,z);
                w.getBlockAt(s).setType(Material.DIRT,false);
                w.getBlockAt(s.clone().add(0,1,0)).setType(Material.AIR,false);
                w.getBlockAt(s).setType(Material.FARMLAND,false);
                Material crop = (Math.abs(x+z)%2==0)? Material.WHEAT_SEEDS : Material.CARROTS;
                w.getBlockAt(s.clone().add(0,1,0)).setType(crop,false);
            }
        }
        p.sendMessage(Text.color("&a섬 농장이 설치되었습니다. &7(간단 구조)"));
    }

    /* ---------------- Upgrade APIs (called from GUI) ---------------- */
    public boolean upgradeMine(Player p){
        IslandData is = store.getOrCreate(p.getUniqueId(), p.getName());
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
        IslandData is = store.getOrCreate(p.getUniqueId(), p.getName());
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
}
