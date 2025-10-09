
package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.upgrade.UpgradeService;
import com.signition.samskybridge.util.Text;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;

public class GuiListener implements Listener {
  private final Main plugin;
  private final UpgradeService service;
  public GuiListener(Main plugin, UpgradeService service){
    this.plugin = plugin; this.service = service;
  }

  private boolean isUpgradeGui(InventoryView view){
    String title = Text.color(plugin.getConfig().getString("gui.title-upgrade", "&f섬 업그레이드"));
    String vt = view.getTitle();
    return vt.equals(title) || ChatColor.stripColor(vt).equals(ChatColor.stripColor(title));
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onClick(InventoryClickEvent e){
    if (!isUpgradeGui(e.getView())) return;
    // Block global actions that can suck from top even when clicking bottom
    InventoryAction act = e.getAction();
    if (act == InventoryAction.COLLECT_TO_CURSOR ||
        act == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
        act == InventoryAction.HOTBAR_SWAP ||
        act == InventoryAction.HOTBAR_MOVE_AND_READD ||
        act == InventoryAction.SWAP_WITH_CURSOR){
      e.setCancelled(true);
      e.setResult(Event.Result.DENY);
    }
    if (e.getView() == null) return;
    if (!isUpgradeGui(e.getView())) return;
    int top = e.getView().getTopInventory().getSize();
    if (e.getRawSlot() < top){
      e.setCancelled(true);
      e.setResult(Event.Result.DENY);

      int sSize = plugin.getConfig().getInt("upgrade.gui.slots.size", 12);
      int sTeam = plugin.getConfig().getInt("upgrade.gui.slots.team", 14);
      int sXp   = plugin.getConfig().getInt("upgrade.gui.slots.xp", 22);
      if (e.getRawSlot() == sSize) service.tryBuySize((org.bukkit.entity.Player)e.getWhoClicked());
      else if (e.getRawSlot() == sTeam) service.tryBuyTeam((org.bukkit.entity.Player)e.getWhoClicked());
      else if (e.getRawSlot() == sXp) service.buyXp((org.bukkit.entity.Player)e.getWhoClicked());
    } else {
      if (e.isShiftClick()){
        e.setCancelled(true);
        e.setResult(Event.Result.DENY);
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
