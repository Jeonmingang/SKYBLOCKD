package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.level.LevelService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class DropTagListener implements Listener {
    private final Main plugin;
    private final LevelService level;
    public DropTagListener(Main plugin, LevelService level){ this.plugin=plugin; this.level=level; }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(BlockDropItemEvent e){
        if (e.getItems()==null) return;
        for (org.bukkit.entity.Item ent : e.getItems()){
            try{
                ItemStack st = ent.getItemStack(); if (st==null) continue;
                ItemMeta m = st.getItemMeta(); if (m==null) continue;
                m.getPersistentDataContainer().set(level.getMinedKey(), PersistentDataType.BYTE, (byte)1);
                st.setItemMeta(m);
                ent.setItemStack(st);
            }catch(Throwable ignored){}
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent e){
        ItemStack st = e.getItem().getItemStack();
        if (st==null) return;
        ItemMeta m = st.getItemMeta();
        if (m==null) return;
        if (!m.getPersistentDataContainer().has(level.getMinedKey(), PersistentDataType.BYTE)){
            // keep no-op; if already tagged, it will remain and prevent XP later.
        }
    }
}
