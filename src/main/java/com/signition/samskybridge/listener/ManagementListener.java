package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.gui.ActionGui;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class ManagementListener implements Listener {
    private final Main plugin;
    private final DataStore store;

    public ManagementListener(Main plugin, DataStore store){
        this.plugin = plugin;
        this.store = store;
    }

    @EventHandler
    public void onManageClick(InventoryClickEvent e){
        Inventory inv = e.getInventory();
        String title = e.getView().getTitle();
        ItemStack it = e.getCurrentItem();
        HumanEntity who = e.getWhoClicked();

        // Top-level GUI
        if (ChatColor.stripColor(title).equals("섬 관리")){
            e.setCancelled(true);
            if (it == null || it.getType() != Material.PLAYER_HEAD) return;
            if (!(it.getItemMeta() instanceof SkullMeta)) return;
            SkullMeta sm = (SkullMeta) it.getItemMeta();
            OfflinePlayer op = sm.getOwningPlayer();
            if (op == null) return;
            String name = op.getName()==null? op.getUniqueId().toString(): op.getName();
            ActionGui.open((Player)who, op.getUniqueId(), name);
            return;
        }

        // Action GUI
        if (ChatColor.stripColor(title).startsWith("관리:")){
            e.setCancelled(true);
            if (it == null) return;
            java.util.UUID target = com.signition.samskybridge.gui.ActionGui.getTarget(it);
            String tname = com.signition.samskybridge.gui.ActionGui.getTargetName(it);
            if (tname == null && target != null){
                OfflinePlayer op = Bukkit.getOfflinePlayer(target);
                tname = op.getName()==null? target.toString(): op.getName();
            }
            if (tname == null) return;
            Player p = (Player) who;
            Material type = it.getType();
            if (type == Material.LIME_DYE){ p.performCommand("is team promote " + tname); p.closeInventory(); return; }
            if (type == Material.ORANGE_DYE){ p.performCommand("is team demote " + tname); p.closeInventory(); return; }
            if (type == Material.BARRIER){ p.performCommand("is team kick " + tname); p.closeInventory(); return; }
        }
    }
}
