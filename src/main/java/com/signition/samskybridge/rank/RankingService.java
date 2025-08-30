
package com.signition.samskybridge.rank;
import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.util.Text;
import org.bukkit.entity.Player;
import java.util.*;
public class RankingService {
  private final Main plugin; private final DataStore store;
  public RankingService(Main plugin, DataStore store){ this.plugin = plugin; this.store = store; }
  public void showTop(Player viewer, int n){
    List<IslandData> all = store.getAll();
    Collections.sort(all, new Comparator<IslandData>(){
      public int compare(IslandData a, IslandData b){
        if (a.getLevel()!=b.getLevel()) return b.getLevel()-a.getLevel();
        return (int)Math.signum(b.getXp()-a.getXp());
      }
    });
    viewer.sendMessage(Text.color(plugin.getConfig().getString("messages.ranking.title","&a섬 랭킹 TOP <n>").replace("<n>", String.valueOf(n))));
    for (int i=0;i<Math.min(n, all.size()); i++){
      IslandData is = all.get(i);
      String line = plugin.getConfig().getString("messages.ranking.line","&f<rank>. &a<name> &7- &f레벨 <level> &7(경험치 <xp>)");
      viewer.sendMessage(Text.color(line.replace("<rank>", String.valueOf(i+1))
        .replace("<name>", is.getName())
        .replace("<level>", String.valueOf(is.getLevel()))
        .replace("<xp>", String.valueOf(is.getXp()))));
    }
  }
}
