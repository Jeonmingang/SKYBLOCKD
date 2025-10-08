package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.upgrade.UpgradeService;
import com.signition.samskybridge.util.Text;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event;
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

        // hard block all move attempts on our gui
        e.setCancelled(true);
        e.setResult(Event.Result.DENY);

        int top = e.getView().getTopInventory().getSize();
        int raw = e.getRawSlot();
        if (raw < 0 || raw >= top) return; // clicks in player inv are blocked but ignored

        if (e.getWhoClicked() instanceof Player){
            Player p = (Player) e.getWhoClicked();
            try { upgrade.click(p, raw, e.isShiftClick()); } catch (Throwable t){
                plugin.getLogger().warning("Upgrade click error: " + t.getMessage());
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
