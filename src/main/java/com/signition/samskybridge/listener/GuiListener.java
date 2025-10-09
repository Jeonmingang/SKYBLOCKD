package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.upgrade.UpgradeService;
import com.signition.samskybridge.util.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;

public class GuiListener implements Listener {
    private final Main plugin;
    private final UpgradeService upgrade;

    public GuiListener(Main plugin, UpgradeService upgrade){
        this.plugin = plugin;
        this.upgrade = upgrade;
    }

    private boolean isUpgrade(InventoryView v){
        if (v == null) return false;
        String raw = plugin.getConfig().getString("upgrade.gui.title", plugin.getConfig().getString("upgrade.gui.title-upgrade", "섬 업그레이드"));
        String title = v.getTitle();
        String colored = Text.color(raw);
        return title != null && (title.equals(colored) || title.equals(Text.stripColor(colored)) || title.contains("섬 업그레이드"));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e){
        if (!isUpgrade(e.getView())) return;

        // Deny quick moves & hotbar swaps inside GUI
        if (e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT
                || e.getClick() == ClickType.NUMBER_KEY || e.getAction() == InventoryAction.HOTBAR_SWAP
                || e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY){
            e.setCancelled(true);
            e.setResult(Event.Result.DENY);
            return;
        }

        int raw = e.getRawSlot();
        int top = e.getView().getTopInventory().getSize();

        if (raw < top){
            e.setCancelled(true);
            e.setResult(Event.Result.DENY);
            Player p = (Player)e.getWhoClicked();
            boolean shift = e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT;
            upgrade.click(p, raw, shift);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e){
        if (!isUpgrade(e.getView())) return;
        int top = e.getView().getTopInventory().getSize();
        for (Integer s : e.getRawSlots()){
            if (s != null && s < top){
                e.setCancelled(true);
                e.setResult(Event.Result.DENY);
                break;
            }
        }
    }
}
