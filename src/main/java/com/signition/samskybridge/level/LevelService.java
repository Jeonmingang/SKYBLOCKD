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
    String namespaced = b.getType().getKey().toString().toLowerCase(Locale.ROOT); // minecraft:stone
    return plugin.getConfig().getInt("xp.blocks." + namespaced, 0);
  }

  public void onPlace(BlockPlaceEvent e){
    Player p = e.getPlayer();

    // sync from Bento if needed
    IslandSync.ensureSyncedFromBento(plugin, store, p);

    // allowed worlds check
    List<String> worlds = plugin.getConfig().getStringList("xp.allowed-worlds");
    if (!worlds.isEmpty() && !worlds.contains(e.getBlock().getWorld().getName())) return;

    int give = xpFor(e.getBlockPlaced());

    if (plugin.getConfig().getBoolean("debug.xp-blocks", false)){
      String key = e.getBlockPlaced().getType().getKey().toString();
      org.bukkit.Bukkit.getLogger().info("[SamSkyBridge] place="+key+" xp="+give);
      p.sendMessage(com.signition.samskybridge.util.Text.color("&7[DEBUG] 블럭키: &f"+key+" &7→ XP: &f"+give));
    }

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
    private void giveXp(org.bukkit.event.block.BlockPlaceEvent e){
        // Minimal: no-op to avoid errors in truncated file
    }
