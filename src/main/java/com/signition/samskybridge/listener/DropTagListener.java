
package com.signition.samskybridge.listener;
import com.signition.samskybridge.Main;
import com.signition.samskybridge.level.LevelService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
public class DropTagListener implements Listener {
  private final Main plugin; private final LevelService level;
  public DropTagListener(Main plugin, LevelService level){ this.plugin=plugin; this.level=level; }
  @EventHandler(ignoreCancelled = true)
  public void onDrop(BlockDropItemEvent e){
    if (e.getItems()==null) return;
    for (org.bukkit.entity.Item it : e.getItems()){
      try{
        ItemStack st = it.getItemStack();
        if (st==null) continue;
        ItemMeta meta = st.getItemMeta(); if (meta==null) continue;
        meta.getPersistentDataContainer().set(level.getMinedKey(), PersistentDataType.BYTE, (byte)1);
        st.setItemMeta(meta);
        it.setItemStack(st);
      }catch(Throwable ignored){}
    }
  }
}
