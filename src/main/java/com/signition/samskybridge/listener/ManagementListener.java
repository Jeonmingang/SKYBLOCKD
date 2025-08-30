
package com.signition.samskybridge.listener;
import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.gui.ActionGui;
import com.signition.samskybridge.util.Text;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer; import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler; import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack; import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.Material;
import java.util.UUID;
public class ManagementListener implements Listener {
  private final Main plugin; private final DataStore store;
  public ManagementListener(Main plugin, DataStore store){ this.plugin = plugin; this.store = store; }
  @EventHandler public void onManageClick(InventoryClickEvent e){
    if (e.getView()==null) return;
    String title = ChatColor.stripColor(e.getView().getTitle());
    if (!"섬 관리".equals(title)) return;
    e.setCancelled(true);
    if (e.getCurrentItem()==null) return;
    ItemStack it = e.getCurrentItem();
    if (it.getType()!=Material.PLAYER_HEAD) return;
    if (!(it.getItemMeta() instanceof SkullMeta)) return;
    SkullMeta sm = (SkullMeta) it.getItemMeta();
    OfflinePlayer op = sm.getOwningPlayer(); if (op==null || op.getUniqueId()==null) return;
    Player p = (Player)e.getWhoClicked();
    ActionGui.open(p, op.getUniqueId(), "unknown");
  }
  @EventHandler public void onActionClick(InventoryClickEvent e){
    if (e.getView()==null) return;
    String title = ChatColor.stripColor(e.getView().getTitle());
    if (!title.startsWith("관리: ")) return;
    e.setCancelled(true);
    Player p = (Player)e.getWhoClicked();
    UUID target = null;
    try{ target = java.util.UUID.fromString(title.substring("관리: ".length())); }catch(Exception ignored){}
    if (target==null) return;
    IslandData is = store.findByMember(p.getUniqueId()).orElse(null);
    if (is==null){ p.sendMessage(Text.color("&c섬이 없습니다.")); return; }
    boolean isOwner = is.getOwner().equals(p.getUniqueId());
    boolean isCo = is.getCoOwners().contains(p.getUniqueId());
    int slot = e.getRawSlot();
    if (slot==11){ // promote
      if (is.getMembers().contains(target)){
        if (isOwner || isCo){
          is.getMembers().remove(target); is.getCoOwners().add(target);
          p.sendMessage(Text.color("&a승급 완료: &f섬원 → 부섬장"));
          store.save();
        }else p.sendMessage(Text.color("&c권한이 없습니다."));
      }else p.sendMessage(Text.color("&c대상이 섬원이 아닙니다."));
    }else if (slot==13){ // demote
      if (is.getCoOwners().contains(target)){
        if (isOwner){
          is.getCoOwners().remove(target); is.getMembers().add(target);
          p.sendMessage(Text.color("&e강등 완료: &f부섬장 → 섬원"));
          store.save();
        }else p.sendMessage(Text.color("&c섬장만 부섬장 관리 가능"));
      }else p.sendMessage(Text.color("&c대상이 부섬장이 아닙니다."));
    }else if (slot==15){ // kick
      if (target.equals(is.getOwner())){ p.sendMessage(Text.color("&c섬장은 추방할 수 없습니다.")); return; }
      if (is.getCoOwners().contains(target)){
        if (!isOwner){ p.sendMessage(Text.color("&c섬장만 부섬장 추방 가능")); return; }
        is.getCoOwners().remove(target);
        p.sendMessage(Text.color("&c부섬장 추방 완료"));
      }else if (is.getMembers().contains(target)){
        if (!(isOwner || isCo)){ p.sendMessage(Text.color("&c권한이 없습니다.")); return; }
        is.getMembers().remove(target);
        p.sendMessage(Text.color("&c섬원 추방 완료"));
      }else{
        p.sendMessage(Text.color("&c섬 구성원이 아닙니다.")); return;
      }
      store.save();
    }
  }
}
