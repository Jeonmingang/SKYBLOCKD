package com.signition.samskybridge.level;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import org.bukkit.entity.Player;
import org.bukkit.Material;

import java.util.UUID;

public class LevelService {
    private final Main plugin;
    private final DataStore data;

    public LevelService(Main plugin, DataStore data){
        this.plugin = plugin;
        this.data = data;
    
    public int getXpFor(Material m){
        if (m == null) return 0;
        switch (m){
            case STONE: case COBBLESTONE: return 1;
            case COAL_ORE: return 3;
            case IRON_ORE: return 5;
            case GOLD_ORE: return 8;
            case DIAMOND_ORE: return 20;
            case EMERALD_ORE: return 25;
            case ANCIENT_DEBRIS: return 30;
            default: return 0;
        }
    }
}

    /** 다음 레벨까지 필요한 XP (간단 공식; 추후 config 연동 가능) */
    public int nextRequired(int level){
        // 예시: (level+1)*100, 최소 10
        int n = (level + 1) * 100;
        return (n < 10 ? 10 : n);
    }

    /** 과거 코드 호환: requiredXp(level) */
    public int requiredXp(int level){
        return nextRequired(level);
    }

    public void grantXp(Player p, int amount){
        UUID id = p.getUniqueId();
        IslandData d = data.getOrCreate(id);
        d.setXp(d.getXp() + amount);
        // level up
        while (d.getXp() >= nextRequired(d.getLevel())){
            d.setXp(d.getXp() - nextRequired(d.getLevel()));
            d.setLevel(d.getLevel() + 1);
        }
    }

    public int levelOf(Player p){ return data.getOrCreate(p.getUniqueId()).getLevel(); }
    public int xpOf(Player p){ return data.getOrCreate(p.getUniqueId()).getXp(); }
}
