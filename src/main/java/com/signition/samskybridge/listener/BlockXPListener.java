
package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.level.LevelService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockXPListener implements Listener {
  private final Main plugin; private final DataStore store; private final LevelService level;
  public BlockXPListener(Main plugin, DataStore store, LevelService level){ this.plugin=plugin; this.store=store; this.level=level; }

  @EventHandler(ignoreCancelled = true)
  public void onPlace(BlockPlaceEvent e){ level.onPlace(e); }
}
