
package com.signition.samskybridge.info;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.util.Text;
import org.bukkit.entity.Player;

public class InfoService {
    private final Main plugin;
    private final LevelService level;
    private final DataStore store;

    public InfoService(Main plugin, LevelService level, DataStore store){
        this.plugin = plugin;
        this.level = level;
        this.store = store;
    }

    /** Opens a simple text-based island info. Can be extended to GUI later. */
    public void show(Player p){
        IslandData is = level.getIslandOf(p);
        if (is == null){
            p.sendMessage(Text.color("&c섬 정보를 찾을 수 없습니다."));
            return;
        }
        int lv = is.getLevel();
        long cur = is.getXp();
        long need = level.getNextXpRequirement(lv + 1);
        int size = is.getSize();
        int cap  = is.getTeamMax();

        p.sendMessage(Text.color("&6[섬 정보] &f소유자: &e" + p.getName()));
        p.sendMessage(Text.color("&7레벨: &f" + lv + " &8| &7경험치: &f" + cur + "&7/&f" + need));
        p.sendMessage(Text.color("&7섬 크기: &f" + size + " 블럭 &8| &7인원수: &f" + cap + " 명"));

        int barLen = Math.max(5, plugin.getConfig().getInt("level.gauge.length", 20));
        String full = plugin.getConfig().getString("level.gauge.full", "█");
        String empty = plugin.getConfig().getString("level.gauge.empty", "░");
        double ratio = need > 0 ? Math.min(1.0, Math.max(0.0, (cur * 1.0) / need)) : 0.0;
        int fill = (int)Math.floor(ratio * barLen);
        StringBuilder sb = new StringBuilder();
        for (int k=0;k<fill;k++) sb.append(full);
        for (int k=fill;k<barLen;k++) sb.append(empty);
        String gauge = sb.toString();
        p.sendMessage(Text.color("&8[&a" + gauge + "&8] &7" + (int)Math.floor(ratio*100) + "%"));
    }
        int lv = is.getLevel();
        long cur = is.getXp();
        long need = level.getNextXpRequirement(lv + 1);
        int size = is.getSize();
        int cap  = is.getTeamMax();

        p.sendMessage(Text.color("&6[섬 정보] &f소유자: &e" + p.getName()));
        p.sendMessage(Text.color("&7레벨: &f" + lv + " &8| &7경험치: &f" + cur + "&7/&f" + need));
        p.sendMessage(Text.color("&7섬 크기: &f" + size + " 블럭 &8| &7인원수: &f" + cap + " 명"));
    }
}
