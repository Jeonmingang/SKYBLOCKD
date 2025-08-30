
package com.signition.samskybridge.cmd;
import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.gui.ManagementGui;
import com.signition.samskybridge.gui.MarketGui;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.rank.RankingService;
import com.signition.samskybridge.upgrade.UpgradeService;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit; import org.bukkit.OfflinePlayer;
import org.bukkit.command.*; import org.bukkit.entity.Player;
import java.util.*;
public class IslandCommand implements CommandExecutor, TabCompleter {
  private final Main plugin; private final DataStore store; private final LevelService level; private final RankingService rank;
  public IslandCommand(Main plugin, DataStore store, LevelService level, RankingService rank){ this.plugin=plugin; this.store=store; this.level=level; this.rank=rank; }
  @Override public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
    if (!(sender instanceof Player)){ sender.sendMessage("플레이어만 사용 가능합니다."); return true; }
    Player p=(Player)sender;
    if (args.length==0){ sendHelp(p); return true; }
    String sub = args[0];
    if ("업그레이드".equals(sub)){ new UpgradeService(plugin, store).open(p); return true; }
    if ("레벨".equals(sub)){
      IslandData is = store.findByMember(p.getUniqueId()).orElseGet(new java.util.function.Supplier<IslandData>(){ public IslandData get(){ return store.getOrCreate(p.getUniqueId()); }});
      long need = level.requiredXp(is.getLevel());
      p.sendMessage(Text.color("&a섬 레벨: &f"+is.getLevel()+" &7경험치 &f"+is.getXp()+"&7/&f"+need));
      return true;
    }
    if ("랭킹".equals(sub)){ rank.showTop(p, 10); return true; }
    if ("알바".equals(sub)){
      if (args.length<2){ p.sendMessage("/섬 알바 <플레이어>"); return true; }
      OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);
      if (op==null || op.getUniqueId()==null){ p.sendMessage("플레이어를 찾을 수 없습니다."); return true; }
      IslandData is = store.findByMember(p.getUniqueId()).orElseGet(new java.util.function.Supplier<IslandData>(){ public IslandData get(){ return store.getOrCreate(p.getUniqueId()); }});
      if (!is.getOwner().equals(p.getUniqueId())){ p.sendMessage("섬장만 설정 가능합니다."); return true; }
      is.toggleWorker(op.getUniqueId());
      p.sendMessage(Text.color("&a알바 권한: &f"+args[1]+" &7=> "+(is.isWorker(op.getUniqueId())?"&a허용":"&c해제")));
      store.save(); return true;
    }
    if ("관리".equals(sub)){ new ManagementGui(plugin, store).open(p); return true; }
    if ("매물".equals(sub)){ new MarketGui(plugin, store).open(p); return true; }
    if ("판매".equals(sub)){
      if (args.length<2){ p.sendMessage("/섬 판매 <금액>"); return true; }
      IslandData is = store.findByMember(p.getUniqueId()).orElseGet(new java.util.function.Supplier<IslandData>(){ public IslandData get(){ return store.getOrCreate(p.getUniqueId()); }});
      if (!is.getOwner().equals(p.getUniqueId())){ p.sendMessage("섬장만 매물 등록 가능"); return true; }
      try{ double price = Double.parseDouble(args[1]); is.setForSale(true); is.setPrice(price); store.save(); p.sendMessage(Text.color("&a매물 등록 완료: &f"+(long)price)); }catch(Exception ex){ p.sendMessage("숫자를 입력하세요."); }
      return true;
    }
    if ("채팅".equals(sub)){
      if (args.length<2){ p.sendMessage("/섬 채팅 켜기|끄기"); return true; }
      if ("켜기".equals(args[1])){
        if (!store.isChatOn(p.getUniqueId())) store.toggleChat(p.getUniqueId());
        p.sendMessage(Text.color("&a섬 채팅: 켜짐"));
      } else if ("끄기".equals(args[1])){
        if (store.isChatOn(p.getUniqueId())) store.toggleChat(p.getUniqueId());
        p.sendMessage(Text.color("&c섬 채팅: 꺼짐"));
      } else { p.sendMessage("/섬 채팅 켜기|끄기"); }
      store.save(); return true;
    }
    sendHelp(p); return true;
  }
  private void sendHelp(Player p){
    p.sendMessage(Text.color("&7/섬 업그레이드 &f: 업그레이드 GUI"));
    p.sendMessage(Text.color("&7/섬 레벨 &f: 현재 섬 레벨/경험치"));
    p.sendMessage(Text.color("&7/섬 랭킹 &f: 랭킹 보기"));
    p.sendMessage(Text.color("&7/섬 알바 <플레이어> &f: 블럭 설치/파괴 허용 토글"));
    p.sendMessage(Text.color("&7/섬 관리 &f: 섬 관리 GUI (승급/강등/추방)"));
    p.sendMessage(Text.color("&7/섬 매물 &f: 섬 매물 GUI"));
    p.sendMessage(Text.color("&7/섬 판매 <금액> &f: 매물 등록/갱신"));
    p.sendMessage(Text.color("&7/섬 채팅 켜기|끄기 &f: 섬 채팅 토글"));
  }
  @Override public java.util.List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){
    if (args.length==1) return java.util.Arrays.asList("업그레이드","레벨","랭킹","알바","관리","매물","판매","채팅");
    if (args.length==2 && "채팅".equals(args[0])) return java.util.Arrays.asList("켜기","끄기");
    return java.util.Collections.emptyList();
  }
}
