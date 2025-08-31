
package com.signition.samskybridge.upgrade;
import com.signition.samskybridge.Main; import com.signition.samskybridge.data.DataStore; import com.signition.samskybridge.data.IslandData; import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit; import org.bukkit.Material; import org.bukkit.entity.Player; import org.bukkit.inventory.Inventory; import org.bukkit.inventory.ItemStack; import org.bukkit.inventory.meta.ItemMeta;
public class UpgradeService {
  private final Main plugin; private final DataStore store; public UpgradeService(Main plugin, DataStore store){ this.plugin=plugin; this.store=store; }

  private static String money(long v){ String s=String.valueOf(v); StringBuilder out=new StringBuilder(); int c=0; for (int i=s.length()-1;i>=0;i--){ out.append(s.charAt(i)); c++; if (c==3 && i>0){ out.append(','); c=0; } } return out.reverse().toString(); }
  private int stepByDelta(int current, int delta){ if (delta<=0) return current; return current / Math.max(1, delta); }

  private int costLevel(String keyBase, int step){
    int base = plugin.getConfig().getInt("upgrade."+keyBase+".level-cost-base", 5);
    int mul  = plugin.getConfig().getInt("upgrade."+keyBase+".level-cost-mul", 2);
    return base + step * mul;
  }
  private long costMoney(String keyBase, int step){
    double base = plugin.getConfig().getDouble("upgrade."+keyBase+".money-cost-base", 1000.0);
    double mul  = plugin.getConfig().getDouble("upgrade."+keyBase+".money-cost-mul", 1.2);
    return (long)Math.floor(base * Math.pow(mul, step));
  }

  public void open(Player p){
    IslandData is = store.findByMember(p.getUniqueId()).orElseGet(new java.util.function.Supplier<IslandData>(){ public IslandData get(){ return store.getOrCreate(p.getUniqueId()); }});
    int sizeDelta=plugin.getConfig().getInt("upgrade.size.delta",5);
    int teamDelta=plugin.getConfig().getInt("upgrade.team.delta",1);

    int sizeStep = stepByDelta(is.getSizeLevel(), sizeDelta);
    int teamStep = stepByDelta(is.getTeamLevel(), teamDelta);

    int sizeCostLevel = costLevel("size", sizeStep);
    long sizeCostMoney = costMoney("size", sizeStep);
    int teamCostLevel = costLevel("team", teamStep);
    long teamCostMoney = costMoney("team", teamStep);

    Inventory inv=Bukkit.createInventory(p,27, Text.color(plugin.getConfig().getString("gui.title-upgrade","섬 업그레이드")));

    inv.setItem(11, item(Material.PLAYER_HEAD,
      plugin.getConfig().getString("gui.upgrade.size.name","&a섬 크기 업그레이드 &7(Lv.<step>)")
        .replace("<step>", String.valueOf(sizeStep)),
      new String[]{
        line("gui.upgrade.size.lore",0,"&7현재: &f<current> &7→ 다음: &f+<delta> &7(= &f<next>)")
          .replace("<current>", String.valueOf(is.getSizeLevel()))
          .replace("<delta>", String.valueOf(sizeDelta))
          .replace("<next>", String.valueOf(is.getSizeLevel()+sizeDelta)),
        line("gui.upgrade.size.lore",1,"&7필요 레벨: &f<cost_level>").replace("<cost_level>", String.valueOf(sizeCostLevel)),
        line("gui.upgrade.size.lore",2,"&7필요 돈: &f<cost_money>").replace("<cost_money>", money(sizeCostMoney)),
        line("gui.upgrade.size.lore",3,"&8좌클릭: 업그레이드 진행")
      }
    ));

    inv.setItem(15, item(Material.WHITE_STAINED_GLASS,
      plugin.getConfig().getString("gui.upgrade.team.name","&a팀원 업그레이드 &7(Lv.<step>)")
        .replace("<step>", String.valueOf(teamStep)),
      new String[]{
        line("gui.upgrade.team.lore",0,"&7현재: &f<current> &7→ 다음: &f+<delta> &7(= &f<next>)")
          .replace("<current>", String.valueOf(is.getTeamLevel()))
          .replace("<delta>", String.valueOf(teamDelta))
          .replace("<next>", String.valueOf(is.getTeamLevel()+teamDelta)),
        line("gui.upgrade.team.lore",1,"&7필요 레벨: &f<cost_level>").replace("<cost_level>", String.valueOf(teamCostLevel)),
        line("gui.upgrade.team.lore",2,"&7필요 돈: &f<cost_money>").replace("<cost_money>", money(teamCostMoney)),
        line("gui.upgrade.team.lore",3,"&8좌클릭: 업그레이드 진행")
      }
    ));

    p.openInventory(inv);
  }

  private String line(String path, int idx, String def){ java.util.List<String> l=plugin.getConfig().getStringList(path); if (l!=null && l.size()>idx) return Text.color(l.get(idx)); return Text.color(def); }

  private ItemStack item(Material m, String name, String[] loreArr){ ItemStack it=new ItemStack(m); ItemMeta im=it.getItemMeta(); if (im!=null){ im.setDisplayName(Text.color(name)); java.util.List<String> lore=new java.util.ArrayList<String>(); for (String s:loreArr) lore.add(Text.color(s)); im.setLore(lore); it.setItemMeta(im);} return it; }

  public void click(Player p, int slot){
    if (slot!=11 && slot!=15) return;
    IslandData is = store.findByMember(p.getUniqueId()).orElseGet(new java.util.function.Supplier<IslandData>(){ public IslandData get(){ return store.getOrCreate(p.getUniqueId()); }});
    if (!is.getOwner().equals(p.getUniqueId())){ p.sendMessage(Text.color("&c섬장만 업그레이드 가능합니다.")); return; }
    boolean both=plugin.getConfig().getBoolean("upgrade.costs.require-both", true);
    int sizeDelta=plugin.getConfig().getInt("upgrade.size.delta",5); int teamDelta=plugin.getConfig().getInt("upgrade.team.delta",1);

    if (slot==11){
      int step = stepByDelta(is.getSizeLevel(), sizeDelta);
      int reqLv = costLevel("size", step); long reqMo = costMoney("size", step);
      if (!pay(p, is, both?reqLv:0, both?reqMo:0)) return;
      int before=is.getSizeLevel(); is.setSizeLevel(is.getSizeLevel()+sizeDelta);
      String msg=plugin.getConfig().getString("messages.upgrade.done","&a섬 업그레이드 완료! &7크기 +<size>, 인원 +<team>");
      p.sendMessage(Text.color(msg.replace("<size>", String.valueOf(is.getSizeLevel()-before)).replace("<team>", "0")));
    } else {
      int step = stepByDelta(is.getTeamLevel(), teamDelta);
      int reqLv = costLevel("team", step); long reqMo = costMoney("team", step);
      if (!pay(p, is, both?reqLv:0, both?reqMo:0)) return;
      int before=is.getTeamLevel(); is.setTeamLevel(is.getTeamLevel()+teamDelta);
      String msg=plugin.getConfig().getString("messages.upgrade.done","&a섬 업그레이드 완료! &7크기 +<size>, 인원 +<team>");
      p.sendMessage(Text.color(msg.replace("<size>", "0").replace("<team>", String.valueOf(is.getTeamLevel()-before))));
    }
    store.save(); open(p);
  }

  private boolean pay(Player p, IslandData is, int reqLevel, long reqMoney){
    if (reqLevel>0 && is.getLevel() < reqLevel){ p.sendMessage(Text.color("&c레벨 부족 &7(요구: "+reqLevel+")")); return false; }
    if (reqMoney>0){
      org.bukkit.plugin.Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
      if (vault==null){ p.sendMessage(Text.color("&cVault(경제) 플러그인이 없습니다. 업그레이드 불가.")); return false; }
      try{
        net.milkbowl.vault.economy.Economy econ = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class).getProvider();
        if (!econ.has(p, reqMoney)){ p.sendMessage(Text.color("&c돈 부족 &7(요구: "+money(reqMoney)+")")); return false; }
        econ.withdrawPlayer(p, reqMoney);
      }catch(Exception ex){ p.sendMessage(Text.color("&c경제 연동 오류: "+ex.getMessage())); return false; }
    }
    if (reqLevel>0) is.setLevel(is.getLevel()-reqLevel);
    return true;
  }
}
