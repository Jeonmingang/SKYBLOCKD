
package com.signition.samskybridge.cmd;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.rank.RankingService;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class IslandCommand implements CommandExecutor, TabCompleter {
  private final Main plugin;
  private final DataStore store;
  private final LevelService level;
  private final RankingService rank;

  private static final Set<String> LOCAL = new HashSet<>(Arrays.asList(
      "레벨","채팅","랭킹","업그레이드","매물","관리","매물등록","매물취소","페이지"
  ));

  public IslandCommand(Main plugin, DataStore store, LevelService level, RankingService rank){
    this.plugin=plugin; this.store=store; this.level=level; this.rank=rank;
  }

  // ===== Help Pages =====
  private static class HelpItem { String cmd; String desc;
    HelpItem(String c, String d){ cmd=c; desc=d; } }
  private final List<HelpItem> HELP = Arrays.asList(
      new HelpItem("/섬 레벨", "섬 레벨/경험치 진행도 표시"),
      new HelpItem("/섬 채팅", "섬 채팅 켜기/끄기 (섬원 전용)"),
      new HelpItem("/섬 랭킹", "섬 랭킹 확인"),
      new HelpItem("/섬 업그레이드", "섬 크기/인원 업그레이드 GUI"),
      new HelpItem("/섬 매물", "섬 매물 시장 열기"),
      new HelpItem("/섬 매물등록 <가격>", "내 섬을 매물로 등록"),
      new HelpItem("/섬 매물취소", "내 섬 매물 취소"),
      new HelpItem("/섬 관리", "초대/부섬장/추방 등 관리 GUI"),
      // forwarded to /is
      new HelpItem("/섬 go [home]", "섬으로 이동 (/is go …)"),
      new HelpItem("/섬 spawn", "섬월드 스폰으로 이동 (/is spawn)"),
      new HelpItem("/섬 create <설계도>", "섬 생성 (/is create)"),
      new HelpItem("/섬 reset <설계도>", "섬 초기화 (/is reset)"),
      new HelpItem("/섬 info [플레이어]", "섬 정보 (/is info)"),
      new HelpItem("/섬 settings", "섬 설정 (/is settings)"),
      new HelpItem("/섬 setname <이름>", "섬 이름 설정 (/is setname)"),
      new HelpItem("/섬 team", "섬 팀 관리 (/is team …)"),
      new HelpItem("/섬 near", "주변 섬 보기 (/is near)"),
      new HelpItem("/섬 sethome [n]", "집 위치 지정 (/is sethome)"),
      new HelpItem("/섬 deletehome [n]", "집 삭제 (/is deletehome)"),
      new HelpItem("/섬 renamehome [n]", "집 이름 변경 (/is renamehome)"),
      new HelpItem("/섬 about", "라이선스/정보 (/is about)")
  );

  private void showHelp(Player p, int page){
    int per = 8;
    int total = (HELP.size() + per - 1)/per;
    if (page < 1) page = 1; if (page > total) page = total;
    p.sendMessage(Text.color("&a&l섬 명령어 &7(페이지 &f"+page+"&7/&f"+total+"&7)"));
    int from = (page-1)*per;
    int to = Math.min(HELP.size(), from+per);
    for (int i=from;i<to;i++){
      HelpItem h = HELP.get(i);
      p.sendMessage(Text.color("&7 - &b"+h.cmd+" &7: &f"+h.desc));
    }
    p.sendMessage(Text.color("&8다음 페이지: &7/섬 페이지 "+Math.min(total, page+1)));
  }

  @Override
  public boolean onCommand(CommandSender s, Command c, String l, String[] args){
    if (!(s instanceof Player)){ s.sendMessage("플레이어만 사용 가능합니다."); return true; }
    Player p = (Player)s;
    // ensure island data is synced from BentoBox/BSkyBlock
    com.signition.samskybridge.integration.IslandSync.ensureSyncedFromBento(plugin, store, p);

    if (args.length == 0){
      showHelp(p, 1);
      return true;
    }

    String sub = args[0];

    // help paging
    if ("페이지".equals(sub)){
      int page = 1;
      if (args.length >= 2) try{ page = Integer.parseInt(args[1]); }catch(Exception ignored){}
      showHelp(p, page);
      return true;
    }

    // local handled commands
    if ("업그레이드".equals(sub)){
      plugin.getGuiListener().getUpgrade().open(p); return true;
    }
    if ("매물".equals(sub)){
      plugin.getGuiListener().getMarket().open(p, 0); return true;
    }
    if ("관리".equals(sub)){
      plugin.getGuiListener().getMgmt().open(p); return true;
    }
    if ("매물등록".equals(sub) && args.length>=2){
      try{
        double price = Double.parseDouble(args[1]);
        com.signition.samskybridge.data.DataStore.IslandSale s0 =
          new com.signition.samskybridge.data.DataStore.IslandSale(p.getUniqueId(), price);
        store.addSale(s0);
        p.sendMessage(Text.color("&a매물 등록: &f"+String.format("%,.0f", price)));
      }catch(Exception ex){ p.sendMessage(Text.color("&c가격을 숫자로 입력!")); }
      return true;
    }
    if ("매물취소".equals(sub)){
      java.util.Iterator<com.signition.samskybridge.data.DataStore.IslandSale> it = store.getMarket().iterator();
      boolean removed=false;
      while(it.hasNext()){ com.signition.samskybridge.data.DataStore.IslandSale s0 = it.next(); if (s0.owner.equals(p.getUniqueId())){ it.remove(); removed=true; } }
      if (removed){ store.saveAsync(); p.sendMessage(Text.color("&e매물 취소 완료.")); } else { p.sendMessage(Text.color("&7등록된 매물이 없습니다.")); }
      return true;
    }
    if ("레벨".equals(sub)){
      // show simple level HUD line
      store.findByMember(p.getUniqueId()).ifPresent(is -> {
        long need = level.requiredXp(is.getLevel());
        p.sendMessage(Text.color("&a섬 레벨 &fLv."+is.getLevel()+" &7("+is.getXp()+" / "+need+")"));
      });
      return true;
    }
    if ("채팅".equals(sub)){
      boolean next = !store.isChatOn(p.getUniqueId());
      store.setChatOn(p.getUniqueId(), next);
      p.sendMessage(Text.color(next? "&a섬 채팅: 켜짐" : "&c섬 채팅: 꺼짐"));
      return true;
    }
    if ("랭킹".equals(sub)){
      java.util.Optional<com.signition.samskybridge.data.IslandData> opt = store.findByMember(p.getUniqueId());
      if (!opt.isPresent()){ p.sendMessage(com.signition.samskybridge.util.Text.color("&7섬이 없습니다.")); return true; }
      com.signition.samskybridge.data.IslandData is = opt.get();
      int r = rank.getRank(is.getOwner());
      long need = level.requiredXp(is.getLevel());
      String playerName;
      try { playerName = org.bukkit.Bukkit.getOfflinePlayer(is.getOwner()).getName(); } catch (Exception ex){ playerName = p.getName(); }
      String fmt = plugin.getConfig().getString("ranking.format", "&a{rank}위 | {player} | Lv.{level} ({xp}/{nextXp})");
      String line = fmt.replace("{rank}", (r<0? "집계중" : String.valueOf(r)))
                       .replace("{player}", playerName==null? p.getName() : playerName)
                       .replace("{level}", String.valueOf(is.getLevel()))
                       .replace("{xp}", String.valueOf(is.getXp()))
                       .replace("{nextXp}", String.valueOf(need));
      p.sendMessage(com.signition.samskybridge.util.Text.color(line));
      return true;
    }{
      store.findByMember(p.getUniqueId()).ifPresent(is -> {
        int r = rank.getRank(is.getOwner());
        p.sendMessage(Text.color("&b섬 랭킹: &f"+(r<0? "집계중" : r+"위")));
      });
      return true;
    }

    // unknown -> forward to /is (bSkyBlock)
    String raw = String.join(" ", args);
    Bukkit.dispatchCommand(p, "is " + raw);
    return true;
  }

  @Override
  public java.util.List<String> onTabComplete(CommandSender s, Command c, String l, String[] args){
    if (args.length <= 1) return new java.util.ArrayList<>(LOCAL);
    return java.util.Collections.emptyList();
  }
}
