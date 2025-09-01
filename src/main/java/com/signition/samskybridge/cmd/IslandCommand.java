
package com.signition.samskybridge.cmd;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.rank.RankingService;
import com.signition.samskybridge.util.Text;
import com.signition.samskybridge.listener.GuiListener;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;

public class IslandCommand implements CommandExecutor, TabCompleter {
  private final Main plugin; private final DataStore store; private final LevelService level; private final RankingService rank;
  public IslandCommand(Main plugin, DataStore store, LevelService level, RankingService rank){
    this.plugin=plugin; this.store=store; this.level=level; this.rank=rank;
  }

  @Override public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
    if (!(sender instanceof Player)){ sender.sendMessage("플레이어만 사용 가능합니다."); return true; }
    Player p = (Player)sender;
    String sub = args.length>0? args[0] : "도움말";
    if ("레벨".equals(sub)){
      Optional<IslandData> opt = store.findByMember(p.getUniqueId());
      if (!opt.isPresent()){ p.sendMessage(Text.color("&c섬이 없습니다.")); return true; }
      IslandData is = opt.get();
      long need = level.requiredXp(is.getLevel());
      p.sendMessage(Text.color("&a섬 레벨: &f"+is.getLevel()));
      p.sendMessage(level.progressBar(is.getXp(), need));
      return true;
    }
    if ("채팅".equals(sub)){
      boolean on = !store.isChatOn(p.getUniqueId());
      store.setChatOn(p.getUniqueId(), on);
      p.sendMessage(Text.color(on? "&a섬 채팅 &f켜짐" : "&c섬 채팅 &f꺼짐"));
      return true;
    }
    
    if ("매물등록".equals(sub) && args.length>=2){
      try{
        double price = Double.parseDouble(args[1]);
        com.signition.samskybridge.data.DataStore.IslandSale s = new com.signition.samskybridge.data.DataStore.IslandSale(p.getUniqueId(), price);
        store.addSale(s);
        p.sendMessage(com.signition.samskybridge.util.Text.color("&a매물로 등록했습니다: &f"+String.format("%,.0f", price)));
      }catch(Exception ex){ p.sendMessage(com.signition.samskybridge.util.Text.color("&c가격을 숫자로 입력!")); }
      return true;
    }
    if ("매물취소".equals(sub)){
      java.util.Iterator<com.signition.samskybridge.data.DataStore.IslandSale> it = store.getMarket().iterator();
      boolean removed=false;
      while(it.hasNext()){ com.signition.samskybridge.data.DataStore.IslandSale s = it.next(); if (s.owner.equals(p.getUniqueId())){ it.remove(); removed=true; } }
      if (removed){ store.saveAsync(); p.sendMessage(com.signition.samskybridge.util.Text.color("&e매물 취소 완료.")); } else { p.sendMessage(com.signition.samskybridge.util.Text.color("&7등록된 매물이 없습니다.")); }
      return true;
    }

    if ("업그레이드".equals(sub)){
      plugin.getGuiListener().getUpgrade().open(p);
      return true;
    }
    if ("매물".equals(sub)){
      plugin.getGuiListener().getMarket().open(p, 0);
      return true;
    }
    if ("관리".equals(sub)){
      plugin.getGuiListener().getMgmt().open(p);
      return true;
    }
    if ("랭킹".equals(sub)){
      rank.refresh();
      p.sendMessage(Text.color("&a랭킹이 갱신되었습니다."));
      return true;
    }
    p.sendMessage(Text.color("&a/섬 레벨, /섬 채팅, /섬 랭킹, /섬 업그레이드, /섬 매물, /섬 매물등록 <가격>, /섬 매물취소, /섬 관리"));
    return true;
  }

  @Override public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args){
    if (args.length==1) return Arrays.asList("레벨","채팅","랭킹");
    return java.util.Collections.emptyList();
  }
}
