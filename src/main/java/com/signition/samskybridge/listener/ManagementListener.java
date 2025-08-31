
package com.signition.samskybridge.listener;
import com.signition.samskybridge.Main; import com.signition.samskybridge.data.DataStore; import com.signition.samskybridge.data.IslandData; import com.signition.samskybridge.gui.ActionGui; import com.signition.samskybridge.util.Text;
import org.bukkit.ChatColor; import org.bukkit.OfflinePlayer; import org.bukkit.entity.Player; import org.bukkit.event.EventHandler; import org.bukkit.event.Listener; import org.bukkit.event.inventory.InventoryClickEvent; import org.bukkit.inventory.ItemStack; import org.bukkit.inventory.meta.SkullMeta; import org.bukkit.Material;
import java.util.UUID;
public class ManagementListener implements Listener {
  private final Main plugin; private final DataStore store; public ManagementListener(Main plugin, DataStore store){ this.plugin=plugin; this.store=store; }
  @EventHandler public void onManageClick(InventoryClickEvent e){ if (e.getView()==null) return; String title=ChatColor.stripColor(e.getView().getTitle()); if (!"섬 관리".equals(title)) return; e.setCancelled(true); if (e.getCurrentItem()==null) return; ItemStack it=e.getCurrentItem(); if (it.getType()!=Material.PLAYER_HEAD) return; if (!(it.getItemMeta() instanceof SkullMeta)) return; SkullMeta sm=(SkullMeta)it.getItemMeta(); OfflinePlayer op=sm.getOwningPlayer(); if (op==null||op.getUniqueId()==null) return; Player p=(Player)e.getWhoClicked(); ActionGui.open(p, op.getUniqueId(), "unknown"); }
}
