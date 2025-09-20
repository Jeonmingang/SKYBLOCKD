package com.signition.samskybridge.level;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class LevelService {
    private final Main plugin;
    private final DataStore store;

    public LevelService(Main plugin, DataStore store){
        this.plugin = plugin;
        this.store = store;
    }

    public IslandData getIslandOf(Player p){
        return store.getOrCreate(p.getUniqueId(), p.getName());
    }

    /** Block-based XP: removed (no-op) */
    public void onPlace(Material mat, Location loc, Player p){}

    public long requiredXp(int level){
        if (level <= 1) {
            try { return plugin.getConfig().getLong("leveling.base-required-xp", 1000L); } catch (Throwable t){ return 1000L; }
        }
        try {
            long base = plugin.getConfig().getLong("leveling.base-required-xp", 1000L);
            long inc  = plugin.getConfig().getLong("leveling.increase-percent", 50L);
            double mult = 1.0 + (inc / 100.0);
            return (long)Math.floor(base * Math.pow(mult, Math.max(0, level - 1)));
        } catch (Throwable t){
            return 1000L * level;
        }
    }

    public long requiredXpForLevel(int level){
        try{
            FileConfiguration cfg = plugin.getConfig();
            if (cfg.isConfigurationSection("leveling.table")){
                String key = "leveling.table." + level;
                if (cfg.isSet(key)){
                    long v = cfg.getLong(key, -1L);
                    if (v >= 0) return v;
                }
            }
        }catch (Throwable ignored){}
        return requiredXp(level);
    }

    public void tryLevelUp(IslandData is){
        while (is.getXp() >= requiredXpForLevel(is.getLevel())){
            is.setLevel(is.getLevel() + 1);
            Player p = Bukkit.getPlayer(is.getId());
            if (p != null){
                String msg = plugin.getConfig().getString("messages.level.leveled-up","&a레벨업! 현재 레벨: &f<level>");
                p.sendMessage(Text.color(msg.replace("<level>", String.valueOf(is.getLevel()))));
            }
        }
    }

    public void applyXpPurchase(IslandData is, long add){
        is.addXp(add);
        tryLevelUp(is);
    }
}
