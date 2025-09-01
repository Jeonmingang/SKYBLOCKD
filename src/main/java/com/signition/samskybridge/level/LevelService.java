
package com.signition.samskybridge.level;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.integration.IslandSync;
import com.signition.samskybridge.util.Hud;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public class LevelService {
  private final Main plugin;
  private final DataStore store;
  public LevelService(Main plugin, DataStore store){ this.plugin=plugin; this.store=store; }

  public long requiredXp(int level){
    // explicit override
    String key = "level.required." + level;
    if (plugin.getConfig().isInt(key)) return plugin.getConfig().getInt(key);
    int base = plugin.getConfig().getInt("level.base", 100);
    double pct = plugin.getConfig().getDouble("level.percent-increase", 10.0) / 100.0;
    long cap = plugin.getConfig().getLong("level.cap", Long.MAX_VALUE);
    double need = base * Math.pow(1.0 + pct, Math.max(0, level));
    long v = (long)Math.ceil(need);
    return Math.min(v, cap);
  }

  private int xpFor(Block b){
    String namespaced = b.getType().getKey().toString().toLowerCase(Locale.ROOT);  // minecraft:stone
    return plugin.getConfig().getInt("xp.blocks." + namespaced, 0);
  }

  public void onPlace(BlockPlaceEvent e){
    Player p = e.getPlayer();
    List<String> worlds = plugin.getConfig().getStringList("xp.allowed-worlds");
    if (!worlds.isEmpty() && !worlds.contains(e.getBlock().getWorld().getName())) return;

    // If player has BentoBox island but not in our store, sync
    IslandSync.ensureSyncedFromBento(plugin, store, p);

    int give = xpFor(e.getBlockPlaced());
    if (give <= 0) return;

    IslandData is = store.findByMember(p.getUniqueId()).orElseGet(() -> store.getOrCreate(p.getUniqueId()));
    long cur = is.getXp() + give;
    long need = requiredXp(is.getLevel());
    if (cur >= need){
      is.setLevel(is.getLevel() + 1);
      is.setXp(cur - need);
      Hud.levelUp(plugin, is, is.getLevel(), requiredXp(is.getLevel()));
    } else {
      is.setXp(cur);
    }
    store.saveAsync();
  }
}
