package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.upgrade.UpgradeService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class GuiListener implements Listener {

    private final Main plugin;
    private final UpgradeService upgrade;

    public GuiListener(Main plugin, UpgradeService upgrade){
        this.plugin = plugin;
        this.upgrade = upgrade;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        Inventory inv = e.getInventory();
        if (inv == null || e.getView() == null || e.getCurrentItem() == null) return;

        String raw = plugin.getConfig().getString("gui.title-upgrade", "섬 업그레이드");
        String title = com.signition.samskybridge.util.Text.color(raw);
        boolean strict = plugin.getConfig().getBoolean("gui.strict-title-match", true);
        String vt = e.getView().getTitle();
        if (strict ? !vt.equals(title) : !org.bukkit.ChatColor.stripColor(vt).equals(org.bukkit.ChatColor.stripColor(title))) return;

        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (e.getWhoClicked() instanceof org.bukkit.entity.Player){
            upgrade.click((org.bukkit.entity.Player)e.getWhoClicked(), slot, e.isShiftClick());
        }
    }
}
