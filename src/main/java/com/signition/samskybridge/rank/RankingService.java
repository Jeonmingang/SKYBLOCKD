
package com.signition.samskybridge.rank;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import java.util.*;

public class RankingService {
  private final Main plugin;
  private final DataStore store;
  private final Map<UUID,Integer> rankByOwner = new HashMap<>();

  public RankingService(Main plugin, DataStore store){
    this.plugin = plugin; this.store = store;
  }

  public void refresh(){
    List<IslandData> all = new ArrayList<>();
    for (UUID u : store.getAllOwners()){
      store.findByMember(u).ifPresent(all::add);
    }
    // sort by level desc, then xp desc
    Collections.sort(all, new Comparator<IslandData>(){
      public int compare(IslandData a, IslandData b){
        int d = Integer.compare(b.getLevel(), a.getLevel());
        if (d != 0) return d;
        return Long.compare(b.getXp(), a.getXp());
      }
    });
    rankByOwner.clear();
    int i=1; for (IslandData is : all){ rankByOwner.put(is.getOwner(), i++); }

    // apply to all online
    for (Player p : Bukkit.getOnlinePlayers()) applyTo(p);
  }

  public int getRank(UUID owner){
    Integer r = rankByOwner.get(owner); return r==null? -1 : r;
  }

  public void applyTo(Player p){
    store.findByMember(p.getUniqueId()).ifPresent(is -> {
      int r = getRank(is.getOwner());
      if (r < 0) r = 999;
      String fmt = plugin.getConfig().getString("ranking.tab-prefix", "&7[ &a섬 랭킹 &f<rank>위 &7| &bLv.<level> &7] &r");
      String prefix = Text.color(fmt.replace("<rank>", String.valueOf(r)).replace("<level>", String.valueOf(is.getLevel())));
      Scoreboard sb = p.getScoreboard(); if (sb == null) sb = Bukkit.getScoreboardManager().getMainScoreboard();
      String teamName = ("is_"+p.getUniqueId().toString().substring(0, 12)).replace("-", "");
      Team t = sb.getTeam(teamName);
      if (t == null) t = sb.registerNewTeam(teamName);
      t.setPrefix(prefix);
      if (!t.hasEntry(p.getName())) t.addEntry(p.getName());
      p.setPlayerListName(prefix + p.getName());
    });
  }
}
