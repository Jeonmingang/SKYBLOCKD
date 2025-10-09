package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.upgrade.UpgradeService;
import com.signition.samskybridge.util.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event;
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
        if (e.getView() == null) return;
        if (!isUpgradeGui(e.getView())) return;

        // Deny any shift-click, number key swap or hotbar pickup to/from the top inventory
        if (e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT
                || e.getClick() == ClickType.NUMBER_KEY || e.getAction() == InventoryAction.HOTBAR_SWAP
                || e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY){
            e.setCancelled(true);
            e.setResult(Event.Result.DENY);
            return;
        }

        int raw = e.getRawSlot();
        int topSize = e.getView().getTopInventory().getSize();

        // Block taking/placing any items in the GUI area
        if (raw < topSize){
            e.setCancelled(true);
            e.setResult(Event.Result.DENY);
            if (!(e.getWhoClicked() instanceof Player)) return;
            Player p = (Player)e.getWhoClicked();

            // Route clicks to UpgradeService only when the configured slots are clicked
            int sizeSlot = plugin.getConfig().getInt("upgrade.gui.slots.size", 12);
            int teamSlot = plugin.getConfig().getInt("upgrade.gui.slots.team", 14);
            int xpSlot   = plugin.getConfig().getInt("upgrade.gui.slots.xp",   22);
            boolean shift = e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT;

            if (raw == sizeSlot || raw == teamSlot || raw == xpSlot){
                try {
                    upgrade.click(p, raw, shift);
                } catch (Throwable t){
                    p.sendMessage("§c업그레이드 처리 중 오류: §7" + t.getMessage());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e){
        if (e.getView() == null) return;
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
