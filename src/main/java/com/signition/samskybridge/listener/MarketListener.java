package com.signition.samskybridge.listener;
import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.integration.BentoSync;
import com.signition.samskybridge.util.Text;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler; import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack; import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.Material;
public class MarketListener implements Listener {
  private final Main plugin; private final DataStore store;
  public MarketListener(Main plugin, DataStore store){ this.plugin = plugin; this.store = store; }
  @EventHandler
  public void onClick(InventoryClickEvent e){
    if (e.getView()==null) return;
    String title = ChatColor.stripColor(e.getView().getTitle());
    if (!"섬 매물".equals(title)) return;
    e.setCancelled(true);
    ItemStack it = e.getCurrentItem(); if (it==null) return;
    if (it.getType()!=Material.PLAYER_HEAD) return;
    if (!(it.getItemMeta() instanceof SkullMeta sm)) return;
    OfflinePlayer op = sm.getOwningPlayer(); if (op==null || op.getUniqueId()==null) return;
    Player p = (Player)e.getWhoClicked();
    // 구경 이동: Bento 있으면 워프 시도, 없으면 안내만
    boolean ok = BentoSync.visitIsland(p, op.getUniqueId());
    if (!ok) p.sendMessage(Text.color("&7구경 이동은 BentoBox 설치 시 동작합니다."));
  }
}