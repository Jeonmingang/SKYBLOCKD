package com.signition.samskybridge.listener;

import com.signition.samskybridge.gui.UpgradeGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;

public class UpgradeGuiListener implements Listener {

    private final JavaPlugin plugin;
    private final UpgradeGUI gui;

    public UpgradeGuiListener(JavaPlugin plugin, UpgradeGUI gui){
        this.plugin = plugin;
        this.gui = gui;
    }

    private String color(String s){
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }

    private boolean isUpgradeGui(InventoryView v){
        if (v == null) return false;
        String viewTitle = v.getTitle();
        String cfg = plugin.getConfig().getString("upgrade.gui.title", "&f섬 업그레이드");
        String configured = color(cfg);
        return configured.equals(viewTitle)
                || ChatColor.stripColor(configured).equals(ChatColor.stripColor(viewTitle));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e){
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (!isUpgradeGui(e.getView())) return;

        // hard-block any move from/to top inventory
        e.setCancelled(true);
        e.setResult(Event.Result.DENY);

        // Block common bypass actions explicitly
        InventoryAction action = e.getAction();
        switch (action){
            case MOVE_TO_OTHER_INVENTORY:
            case HOTBAR_MOVE_AND_READD:
            case HOTBAR_SWAP:
            case COLLECT_TO_CURSOR:
            case UNKNOWN:
                e.setCancelled(true);
                break;
            default:
                // fall-through — still cancelled above
                break;
        }

        int raw = e.getRawSlot();
        int top = e.getView().getTopInventory().getSize();
        if (raw < 0 || raw >= top) return; // clicks in bottom inventory ignored

        Player p = (Player) e.getWhoClicked();

        // resolve slot→key from config (UpgradeGUI writes this when building the inv)
        String key = plugin.getConfig().getString("upgrade.gui._slotmap."+raw, null);
        if (key == null){
            return;
        }
        String cmd = plugin.getConfig().getString("upgrade.gui._commands."+key, null);
        if (cmd == null || cmd.isEmpty()){
            return;
        }

        // Run as player (so permissions/economy hooks fire the same as typing it)
        Bukkit.dispatchCommand(p, cmd);

        // Refresh or close according to config
        boolean refresh = plugin.getConfig().getBoolean("upgrade.gui.refresh-after-click", true);
        if (refresh){
            Inventory rebuilt = gui.build(p);
            p.openInventory(rebuilt);
        } else {
            p.closeInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e){
        if (!isUpgradeGui(e.getView())) return;
        int top = e.getView().getTopInventory().getSize();
        // any drag affecting top inventory is denied
        if (e.getRawSlots().stream().anyMatch(s -> s < top)){
            e.setCancelled(true);
            e.setResult(Event.Result.DENY);
        }
    }
}