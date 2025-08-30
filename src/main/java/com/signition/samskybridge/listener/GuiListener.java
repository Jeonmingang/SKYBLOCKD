
package com.signition.samskybridge.listener;
import com.signition.samskybridge.Main;
import com.signition.samskybridge.upgrade.UpgradeService;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler; import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent; import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.entity.Player;
public class GuiListener implements Listener {
  private final Main plugin; private final UpgradeService upgrade;
  public GuiListener(Main plugin, com.signition.samskybridge.data.DataStore store){ this.plugin = plugin; this.upgrade = new UpgradeService(plugin, store); }
  @EventHandler public void onClick(InventoryClickEvent e){
    Inventory inv = e.getInventory();
    String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("gui.title-upgrade","섬 업그레이드"));
    if (inv==null || e.getView()==null || e.getCurrentItem()==null) return;
    if (!e.getView().getTitle().equals(title)) return;
    e.setCancelled(true);
    if (e.getClick()==ClickType.RIGHT || e.getClick()==ClickType.SHIFT_RIGHT) return;
    int slot = e.getRawSlot(); upgrade.click((Player)e.getWhoClicked(), slot);
  }
}
