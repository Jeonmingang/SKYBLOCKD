package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.upgrade.UpgradeService;
import com.signition.samskybridge.util.Text;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.Event;

import org.bukkit.inventory.InventoryView;

/** Listens for GUI interactions and routes clicks to UpgradeService while preventing item extraction. */
public class GuiListener implements Listener {

    private final Main plugin;
    private final UpgradeService upgrade;

    public GuiListener(Main plugin, UpgradeService upgrade){
        this.plugin = plugin;
        this.upgrade = upgrade;
    }

    private boolean isUpgradeGui(InventoryView view){
        String raw = plugin.getConfig().getString("upgrade.gui.title-upgrade", "섬 업그레이드");
        String colored = Text.color(raw);
        boolean strict = plugin.getConfig().getBoolean("upgrade.gui.strict-title-match", true);
        String vt = view.getTitle();
        if (strict) return vt.equals(colored);
        return ChatColor.stripColor(vt).equals(ChatColor.stripColor(colored));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e){
        if (e.getWhoClicked() == null || e.getView() == null) return;
        if (!isUpgradeGui(e.getView())) return;

        // Always deny moving items in our GUI
        e.setCancelled(true);
        e.setResult(Event.Result.DENY);

        // Only accept clicks in the top inventory area
        int top = e.getView().getTopInventory().getSize();
        int rawSlot = e.getRawSlot();
        if (rawSlot < 0) return;
        if (rawSlot >= top) {
            // Click was in player inventory: still cancel to prevent shift-click into GUI
            return;
        }

        if (e.getWhoClicked() instanceof Player){
            Player p = (Player) e.getWhoClicked();
            try {
                upgrade.click(p, rawSlot, e.isShiftClick());
            } catch (Throwable t){
                plugin.getLogger().warning("Upgrade GUI click handling error: " + t.getMessage());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e){
        if (e.getView() == null) return;
        if (!isUpgradeGui(e.getView())) return;

        int top = e.getView().getTopInventory().getSize();
        // If any dragged slot touches the top inventory, cancel
        for (Integer s : e.getRawSlots()){
            if (s != null && s < top){
                e.setCancelled(true);
                e.setResult(Event.Result.DENY);
                return;
            }
        }
    }
}
