package com.signition.samskybridge.level;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.util.Text;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;

/**
 * Level / XP service (clean implementation).
 */
public class LevelService {
    private final Main plugin;
    private final DataStore store;

    // Tuning
    private long baseXp;     // XP for level 1 -> 2
    private long perLevel;   // linear increment per next level
    private double growth;   // multiplicative growth per level
    private int maxLevel;
    private boolean notifyOnLevelUp;
    private String levelUpMsg;

    public LevelService(Main plugin, DataStore store){
        this.plugin = plugin;
        this.store = store;
        reloadConfig();
    }

    public final void reloadConfig(){
        FileConfiguration c = plugin.getConfig();
        this.baseXp = c.getLong("level.base-xp", 500L);
        this.perLevel = c.getLong("level.per-level", 250L);
        this.growth = Math.max(1.0, c.getDouble("level.growth", 1.10));
        this.maxLevel = Math.max(1, c.getInt("level.max", 100));
        this.notifyOnLevelUp = c.getBoolean("level.notify-on-levelup", true);
        this.levelUpMsg = c.getString("level.levelup-message", "&a섬 레벨이 올라갔습니다! &fLv.<level>");
    }

    public IslandData getIslandOf(Player p){
        return store.getOrCreate(p.getUniqueId(), p.getName());
    }

    public int getLevel(UUID id){
        IslandData is = store.getIsland(id);
        return is != null ? is.getLevel() : 1;
    }

    public long getCurrentXp(UUID id){
        IslandData is = store.getIsland(id);
        return is != null ? is.getXp() : 0L;
    }

    /** XP required to REACH the given level (i.e., next level requirement). */
    public long getNextXpRequirement(int level){
        if (level <= 1) return baseXp;
        double req = baseXp;
        for (int i=2; i<=level; i++){
            req = (req + perLevel) * growth;
        }
        return (long) Math.max(baseXp, Math.floor(req));
    }

    /** Show status bar in chat for /섬 레벨 */
    public void show(Player p){
        UUID id = p.getUniqueId();
        int lv = Math.min(getLevel(id), maxLevel);
        long cur = getCurrentXp(id);
        long need = getNextXpRequirement(lv + 1);

        int barLen = Math.max(5, plugin.getConfig().getInt("level.gauge.length", 20));
        String full = plugin.getConfig().getString("level.gauge.full", "█");
        String empty = plugin.getConfig().getString("level.gauge.empty", "░");
        double ratio = need > 0 ? Math.min(1.0, Math.max(0.0, (cur * 1.0) / need)) : 0.0;
        int fill = (int)Math.floor(ratio * barLen);
        StringBuilder sb = new StringBuilder();
        for (int k=0;k<fill;k++) sb.append(full);
        for (int k=fill;k<barLen;k++) sb.append(empty);
        String gauge = sb.toString();

        String msg = plugin.getConfig().getString("level.status",
                "&a섬 레벨: &f<level> &7(&f<xp>&7/&f<need>&7) &8| &a<gauge> &7<percent>%");
        msg = msg.replace("<level>", String.valueOf(lv))
                 .replace("<xp>", String.valueOf(cur))
                 .replace("<need>", String.valueOf(need))
                 .replace("<gauge>", gauge)
                 .replace("<percent>", String.valueOf((int)Math.floor(ratio*100)));
        p.sendMessage(Text.color(msg));
    }
}
