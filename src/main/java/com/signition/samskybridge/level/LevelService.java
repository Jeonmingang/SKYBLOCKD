package com.signition.samskybridge.level;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.util.Text;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.UUID;

public class LevelService {
    private final Main plugin;
    private final DataStore store;

    private long baseXp;
    private long perLevel;
    private double growth;
    private int maxLevel;
    private boolean notifyOnLevelUp;
    private String levelUpMsg;

    public LevelService(Main plugin, DataStore store){
        this.plugin = plugin;
        this.store = store != null ? store : plugin.getDataStore();
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

    /** Multiplier percent for XP requirement (e.g. 120 means 1.2x). */
    private double reqMultiplier(){
        double pct = plugin.getConfig().getDouble("level.requirement-multiplier-percent", 100.0);
        if (pct <= 0) pct = 100.0;
        return pct / 100.0;
    }

    /** XP required to REACH the given level (i.e., next level requirement). */
    public long getNextXpRequirement(int level){
        if (level <= 1) return (long)Math.floor(baseXp * reqMultiplier());
        double req = baseXp;
        for (int i=2; i<=level; i++){
            req = (req + perLevel) * growth;
        }
        req = req * reqMultiplier();
        return (long) Math.max(baseXp, Math.floor(req));
    }

    /** Add XP and handle level up chain if needed. */
    public void addXp(Player p, long delta){
        if (delta <= 0) return;
        IslandData is = getIslandOf(p);
        if (is == null) return;
        long xp = Math.max(0L, is.getXp()) + delta;

        int level = Math.max(1, is.getLevel());
        boolean leveled = false;
        while (level < maxLevel){
            long need = getNextXpRequirement(level + 1);
            if (xp >= need){
                xp -= need;
                level++;
                leveled = true;
            } else break;
        }

        is.setXp(xp);
        is.setLevel(level);
        store.put(is);

        if (leveled && notifyOnLevelUp){
            String msg = levelUpMsg.replace("<level>", String.valueOf(level));
            p.sendMessage(Text.color(msg));
        }
    }

    /** Show status bar in chat for /섬 레벨 */
    public void show(Player p){
        IslandData is = getIslandOf(p);
        if (is == null){
            p.sendMessage(Text.color("&c섬 데이터를 찾지 못했습니다."));
            return;
        }
        int lv = Math.min(is.getLevel(), maxLevel);
        long cur = is.getXp();
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
