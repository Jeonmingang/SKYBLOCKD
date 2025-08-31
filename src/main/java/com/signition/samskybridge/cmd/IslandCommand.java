
package com.signition.samskybridge.cmd;
import com.signition.samskybridge.Main; import com.signition.samskybridge.data.DataStore; import com.signition.samskybridge.data.IslandData; import com.signition.samskybridge.gui.ManagementGui; import com.signition.samskybridge.gui.MarketGui; import com.signition.samskybridge.level.LevelService; import com.signition.samskybridge.rank.RankingService; import com.signition.samskybridge.upgrade.UpgradeService; import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit; import org.bukkit.command.*; import org.bukkit.entity.Player;
public class IslandCommand implements CommandExecutor, TabCompleter {
  private final Main plugin; private final DataStore store; private final LevelService level; private final RankingService rank;
  public IslandCommand(Main plugin, DataStore store, LevelService level, RankingService rank){ this.plugin=plugin; this.store=store; this.level=level; this.rank=rank; }
  @Override public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
    if (!(sender instanceof Player)){ sender.sendMessage("플레이어만 사용 가능합니다."); return true; }
    Player p=(Player)sender; if (args.length==0){ sendHelp(p, 1); return true; } String sub=args[0];
    if ("초대".equals(sub) && args.length>=2){ if ("수락".equals(args[1])) return forward(p,"is team accept"); if ("거절".equals(args[1])) return forward(p,"is team reject"); return forward(p,"is team invite "+args[1]); }
    if ("탈퇴".equals(sub)) return forward(p,"is team leave");
    if ("알바".equals(sub) && args.length>=2) return forward(p,"is team coop "+args[1]);
    if ("알바해제".equals(sub) && args.length>=2) return forward(p,"is team uncoop "+args[1]);
    if ("승급".equals(sub) && args.length>=2) return forward(p,"is team promote "+args[1]);
    if ("강등".equals(sub) && args.length>=2) return forward(p,"is team demote "+args[1]);
    if ("팀추방".equals(sub) && args.length>=2) return forward(p,"is team kick "+args[1]);
    if ("섬장위임".equals(sub) && args.length>=2) return forward(p,"is team setowner "+args[1]);
    if ("신뢰".equals(sub) && args.length>=2) return forward(p,"is team trust "+args[1]);
    if ("신뢰해제".equals(sub) && args.length>=2) return forward(p,"is team untrust "+args[1]);
    if ("이동".equals(sub)) return forward(p,"is go "+joinTail(args,1));
    if ("스폰".equals(sub)) return forward(p,"is spawn");
    if ("생성".equals(sub) || "만들기".equals(sub)) return forward(p,"is create "+joinTail(args,1));
    if ("초기화".equals(sub)) return forward(p,"is reset "+joinTail(args,1));
    if ("정보".equals(sub)) return forward(p,"is info "+(args.length>=2?args[1]:""));
    if ("설정".equals(sub)) return forward(p,"is settings");
    if ("이름".equals(sub) && args.length>=2) return forward(p,"is setname "+joinTail(args,1));
    if ("이름초기화".equals(sub)) return forward(p,"is resetname");
    if ("언어".equals(sub)) return forward(p,"is language "+joinTail(args,1));
    if ("차단".equals(sub) && args.length>=2) return forward(p,"is ban "+args[1]);
    if ("차단해제".equals(sub) && args.length>=2) return forward(p,"is unban "+args[1]);
    if ("차단목록".equals(sub)) return forward(p,"is banlist");
    if ("추방".equals(sub) && args.length>=2) return forward(p,"is expel "+args[1]);
    if ("업그레이드".equals(sub)){ new UpgradeService(plugin,store).open(p); return true; }
    if ("레벨".equals(sub)){ IslandData is=store.findByMember(p.getUniqueId()).orElseGet(new java.util.function.Supplier<com.signition.samskybridge.data.IslandData>(){ public IslandData get(){ return store.getOrCreate(p.getUniqueId()); }}); long need=level.requiredXp(is.getLevel()); p.sendMessage(Text.color("&a섬 레벨: &f"+is.getLevel()+" &7경험치 &f"+is.getXp()+"&7/&f"+need)); return true; }
    if ("랭킹".equals(sub)){ rank.showTop(p,10); return true; }
    if ("관리".equals(sub)){ new ManagementGui(plugin,store).open(p); return true; }
    if ("매물".equals(sub)){ new MarketGui(plugin,store).open(p); return true; }
    if ("판매".equals(sub)){
      if (args.length<2){ p.sendMessage("/섬 판매 <금액>"); return true; }
      IslandData is=store.findByMember(p.getUniqueId()).orElseGet(new java.util.function.Supplier<com.signition.samskybridge.data.IslandData>(){ public IslandData get(){ return store.getOrCreate(p.getUniqueId()); }});
      if (!is.getOwner().equals(p.getUniqueId())){ p.sendMessage("섬장만 매물 등록 가능"); return true; }
      try{ double price=Double.parseDouble(args[1]); is.setForSale(true); is.setPrice(price); store.save(); p.sendMessage(Text.color("&a매물 등록 완료: &f"+(long)price)); }catch(Exception ex){ p.sendMessage("숫자를 입력하세요."); }
      return true;
    }
    if ("채팅".equals(sub)){
      if (args.length<2){ p.sendMessage("/섬 채팅 켜기|끄기"); return true; }
      if ("켜기".equals(args[1])){ if (!store.isChatOn(p.getUniqueId())) store.toggleChat(p.getUniqueId()); p.sendMessage(Text.color("&a섬 채팅: 켜짐")); }
      else if ("끄기".equals(args[1])){ if (store.isChatOn(p.getUniqueId())) store.toggleChat(p.getUniqueId()); p.sendMessage(Text.color("&c섬 채팅: 꺼짐")); }
      else p.sendMessage("/섬 채팅 켜기|끄기"); store.save(); return true;
    }
    if ("도움말".equals(sub)){ sendHelp(p, 1); return true; }
    sendHelp(p, 1); return true;
  }
  private boolean forward(Player p, String raw){ return p.performCommand(raw); }
  private String joinTail(String[] arr, int start){ if (arr.length<=start) return ""; StringBuilder sb=new StringBuilder(); for (int i=start;i<arr.length;i++){ if (i>start) sb.append(' '); sb.append(arr[i]); } return sb.toString(); }
  
private void sendHelp(Player p, int page){
  java.util.List<String> page1 = new java.util.ArrayList<String>();
  page1.add("&a/섬 이동 [번호] &7- 내 섬/서브 홈으로 이동");
  page1.add("&a/섬 스폰 &7- 섬 월드 스폰으로 이동");
  page1.add("&a/섬 생성 <설계도> &7- 섬 만들기");
  page1.add("&a/섬 초기화 <설계도> &7- 섬 리셋(주의)");
  page1.add("&a/섬 정보 [플레이어] &7- 섬 정보 보기");
  page1.add("&a/섬 설정 &7- 섬 설정 열기");
  page1.add("&a/섬 이름 <이름> &7- 섬 이름 변경");
  page1.add("&a/섬 이름초기화 &7- 섬 이름 초기화");
  page1.add("&a/섬 언어 [언어] &7- 언어 선택");
  page1.add("&a/섬 레벨 &7- 현재 레벨/경험치 확인");
  page1.add("&a/섬 채팅 켜기|끄기 &7- 섬 전용 채팅 토글");
  page1.add("&a/섬 업그레이드 &7- 업그레이드 GUI");
  page1.add("&a/섬 관리 &7- 섬원 관리 GUI");
  page1.add("&a/섬 매물 &7- 섬 매물 GUI");
  page1.add("&a/섬 판매 <금액> &7- 내 섬을 매물로 등록");
  java.util.List<String> page2 = new java.util.ArrayList<String>();
  page2.add("&a/섬 초대 <플레이어> &7- 섬 초대");
  page2.add("&a/섬 초대 수락 &7- 초대 수락");
  page2.add("&a/섬 초대 거절 &7- 초대 거절");
  page2.add("&a/섬 탈퇴 &7- 섬 떠나기");
  page2.add("&a/섬 섬장위임 <플레이어> &7- 섬장 권한 넘기기");
  page2.add("&a/섬 팀추방 <플레이어> &7- 섬원 추방");
  page2.add("&a/섬 알바 <플레이어> &7- 알바 등록");
  page2.add("&a/섬 알바해제 <플레이어> &7- 알바 해제");
  page2.add("&a/섬 신뢰 <플레이어> &7- 신뢰 등록");
  page2.add("&a/섬 신뢰해제 <플레이어> &7- 신뢰 해제");
  page2.add("&a/섬 승급 <플레이어> &7- 등급 승급");
  page2.add("&a/섬 강등 <플레이어> &7- 등급 강등");
  page2.add("&a/섬 추방 <플레이어> &7- 방문자 추방");
  page2.add("&a/섬 차단 <플레이어> &7- 차단");
  page2.add("&a/섬 차단해제 <플레이어> &7- 차단 해제");
  page2.add("&a/섬 차단목록 &7- 차단 목록");
  int max = 2; if (page<1) page=1; if (page>max) page=max;
  p.sendMessage(com.signition.samskybridge.util.Text.color("&b================= &f섬 명령어(한글) &b[페이지 "+page+"/"+max+"]"));
  java.util.List<String> list = page==1 ? page1 : page2;
  for (String s : list) p.sendMessage(com.signition.samskybridge.util.Text.color(s));
  p.sendMessage(com.signition.samskybridge.util.Text.color("&b==============================================="));
  if (page==1) p.sendMessage(com.signition.samskybridge.util.Text.color("&7다음 페이지: &a/섬 도움말 2"));
}
@Override public java.util.List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){
    java.util.List<String> first=java.util.Arrays.asList("도움말","이동","스폰","생성","초기화","정보","설정","이름","이름초기화","언어","차단","차단해제","차단목록","추방","초대","탈퇴","섬장위임","팀추방","알바","알바해제","신뢰","신뢰해제","승급","강등","업그레이드","레벨","랭킹","관리","매물","판매","채팅");
    if (args.length==1){ java.util.List<String> out=new java.util.ArrayList<String>(); for (String k:first) if (k.startsWith(args[0])) out.add(k); return out; }
    if (args.length==2){
      String s1=args[0];
      java.util.List<String> needPlayer=java.util.Arrays.asList("초대","섬장위임","팀추방","알바","알바해제","신뢰","신뢰해제","승급","강등","추방","차단","차단해제","정보");
      if (needPlayer.contains(s1)){ java.util.List<String> names=new java.util.ArrayList<String>(); for (org.bukkit.entity.Player op: Bukkit.getOnlinePlayers()) names.add(op.getName()); java.util.List<String> out=new java.util.ArrayList<String>(); for (String n:names) if (n.toLowerCase().startsWith(args[1].toLowerCase())) out.add(n); return out; }
      if ("채팅".equals(s1)) return java.util.Arrays.asList("켜기","끄기");
      if ("초대".equals(s1)) return java.util.Arrays.asList("수락","거절");
    }
    return java.util.Collections.emptyList();
  }

private void sendHelp(Player p){ sendHelp(p, 1); }
}
