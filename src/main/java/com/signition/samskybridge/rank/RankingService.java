
package com.signition.samskybridge.rank;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class RankingService {
    private final Main plugin;
    private final DataStore store;
    public RankingService(Main plugin, DataStore store){ this.plugin=plugin; this.store=store; }

    public void showTop(Player p, int n){
        List<IslandData> all = store.getAll();
        Collections.sort(all, new Comparator<IslandData>(){
            public int compare(IslandData a, IslandData b){
                if (a.getLevel()!=b.getLevel()) return Integer.compare(b.getLevel(), a.getLevel());
                return Long.compare(b.getXp(), a.getXp());
            }
        });
        n = Math.min(n, all.size());
        p.sendMessage(Text.color(plugin.getConfig().getString("messages.ranking.title","&a섬 랭킹 TOP <n>").replace("<n>", String.valueOf(n))));
        for (int i=0;i<n;i++){
            IslandData is = all.get(i);
            long need = plugin.getLevelService().requiredXp(is.getLevel());
            String ownerName = Optional.ofNullable(Bukkit.getOfflinePlayer(is.getOwner()).getName()).orElse(is.getOwner().toString());
            String line = plugin.getConfig().getString("messages.ranking.line","&f<rank>. &a<name> &7- &f레벨 <level> &7(경험치 <xp>)")
                    .replace("<rank>", String.valueOf(i+1))
                    .replace("<name>", ownerName)
                    .replace("<level>", String.valueOf(is.getLevel()))
                    .replace("<xp>", is.getXp()+" / "+need);
            p.sendMessage(Text.color(line));
        }
    }
}
