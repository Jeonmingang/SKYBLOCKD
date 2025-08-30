
package com.signition.samskybridge.upgrade;
import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
public class UpgradeService {
  private final Main plugin; private final DataStore store;
  public UpgradeService(Main plugin, DataStore store){ this.plugin = plugin; this.store = store; }
  public void open(Player p){
    Inventory inv = Bukkit.createInventory(p, 27, Text.color(plugin.getConfig().getString("gui.title-upgrade","섬 업그레이드")));
    inv.setItem(11, item(Material.PLAYER_HEAD,
      plugin.getConfig().getString("gui.upgrade.size.name","&a섬 크기 업그레이드"),
      new String[]{"&7좌클릭: 비용 지불(레벨+돈, 둘 다 필요)","&7우클릭: 비활성화됨"}));
    inv.setItem(15, item(Material.WHITE_STAINED_GLASS,
      plugin.getConfig().getString("gui.upgrade.team.name","&a팀원 업그레이드"),
      new String[]{"&7좌클릭: 비용 지불(레벨+돈, 둘 다 필요)","&7우클릭: 비활성화됨"}));
    p.openInventory(inv);
  }
  private ItemStack item(Material m, String name, String[] loreArr){
    ItemStack it = new ItemStack(m);
    ItemMeta im = it.getItemMeta();
    if (im!=null){
      im.setDisplayName(Text.color(name));
      java.util.List<String> lore = new java.util.ArrayList<String>();
      for (String s: loreArr) lore.add(Text.color(s));
      im.setLore(lore); it.setItemMeta(im);
    }
    return it;
  }
  public void click(Player p, int slot){
    if (slot!=11 && slot!=15) return;
    IslandData is = store.findByMember(p.getUniqueId()).orElseGet(new java.util.function.Supplier<IslandData>(){ public IslandData get(){ return store.getOrCreate(p.getUniqueId()); }});
    if (!is.getOwner().equals(p.getUniqueId())){ p.sendMessage(Text.color("&c섬장만 업그레이드 가능합니다.")); return; }
    boolean both = plugin.getConfig().getBoolean("upgrade.costs.require-both", true);
    int sizeDelta = plugin.getConfig().getInt("upgrade.size.delta", 5);
    int teamDelta = plugin.getConfig().getInt("upgrade.team.delta", 1);
    int beforeSize = is.getSizeLevel(); int beforeTeam = is.getTeamLevel();
    int sizeCostLevel = plugin.getConfig().getInt("upgrade.size.level-cost-base",5) + is.getSizeLevel()*plugin.getConfig().getInt("upgrade.size.level-cost-mul",2);
    double sizeCostMoney = plugin.getConfig().getDouble("upgrade.size.money-cost-base",1000.0) * Math.pow(plugin.getConfig().getDouble("upgrade.size.money-cost-mul",1.2), is.getSizeLevel());
    int teamCostLevel = plugin.getConfig().getInt("upgrade.team.level-cost-base",3) + is.getTeamLevel()*plugin.getConfig().getInt("upgrade.team.level-cost-mul",2);
    double teamCostMoney = plugin.getConfig().getDouble("upgrade.team.money-cost-base",1500.0) * Math.pow(plugin.getConfig().getDouble("upgrade.team.money-cost-mul",1.25), is.getTeamLevel());
    if (slot==11){
      if (!pay(p, is, both? sizeCostLevel:0, both? sizeCostMoney:0.0)) return;
      is.setSizeLevel(is.getSizeLevel()+sizeDelta);
    }else if (slot==15){
      if (!pay(p, is, both? teamCostLevel:0, both? teamCostMoney:0.0)) return;
      is.setTeamLevel(is.getTeamLevel()+teamDelta);
    }
    String msg = plugin.getConfig().getString("messages.upgrade.done","&a섬 업그레이드 완료! &7크기 +<size>, 인원 +<team>");
    p.sendMessage(Text.color(msg.replace("<size>", String.valueOf(is.getSizeLevel()-beforeSize)).replace("<team>", String.valueOf(is.getTeamLevel()-beforeTeam))));
    store.save();
    open(p);
  }
  private boolean pay(Player p, IslandData is, int reqLevel, double reqMoney){
    if (is.getLevel() < reqLevel){ p.sendMessage(Text.color("&c레벨 부족 &7(요구: "+reqLevel+")")); return false; }
    if (reqMoney > 0.0){
      org.bukkit.plugin.Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
      if (vault == null){ p.sendMessage(Text.color("&cVault(경제) 플러그인이 없습니다. 업그레이드 불가.")); return false; }
      try{
        net.milkbowl.vault.economy.Economy econ = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class).getProvider();
        if (!econ.has(p, reqMoney)){ p.sendMessage(Text.color("&c돈 부족 &7(요구: "+(long)reqMoney+")")); return false; }
        econ.withdrawPlayer(p, reqMoney);
      }catch (Exception ex){ p.sendMessage(Text.color("&c경제 연동 오류: "+ex.getMessage())); return false; }
    }
    is.setLevel(is.getLevel()-reqLevel);
    return true;
  }
}
