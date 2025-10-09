package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.util.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;

/**
 * Backward-compat listener kept for older builds that referenced UpgradeGuiListener.
 * Functionally identical to GuiListener: protects the Upgrade GUI from item extraction
 * and routes clicks to UpgradeService.
 */
public class UpgradeGuiListener implements Listener {
    private final Main plugin;

    public UpgradeGuiListener(Main plugin){
        this.plugin = plugin;
    }

    private boolean isUpgradeGui(InventoryView view){
        if (view == null) return false;
        String raw = plugin.getConfig().getString("upgrade.gui.title-upgrade",
                plugin.getConfig().getString("gui.title-upgrade", "섬 업그레이드"));
        String colored = Text.color(raw);
        String title = view.getTitle();
        return title != null && (title.equals(colored) || title.equals(Text.stripColor(colored)) || title.contains("섬 업그레이드"));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e){
        if (!isUpgradeGui(e.getView())) return;
        int top = e.getView().getTopInventory().getSize();
        if (e.getRawSlot() < top){
            e.setCancelled(true);
            e.setResult(Event.Result.DENY);
            if (e.getWhoClicked() instanceof Player){
                // Delegate to current UpgradeService
                plugin.getUpgradeService().click((Player)e.getWhoClicked(), e.getRawSlot(), e.isShiftClick());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e){
        if (!isUpgradeGui(e.getView())) return;
        int top = e.getView().getTopInventory().getSize();
        for (Integer s : e.getRawSlots()){
            if (s != null && s < top){
                e.setCancelled(true);
                e.setResult(Event.Result.DENY);
                return;
            }
        }
    }
}
