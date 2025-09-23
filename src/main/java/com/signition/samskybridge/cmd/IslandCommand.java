
package com.signition.samskybridge.cmd;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.rank.RankingService;
import com.signition.samskybridge.upgrade.UpgradeService;
import com.signition.samskybridge.util.ConfigUtil;
import com.signition.samskybridge.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class IslandCommand implements CommandExecutor {
    private boolean forwardToIs(Player p, String args){
        try{ p.performCommand("is "+args); return true; }catch(Throwable t){ return false; }
    }

    private final Main plugin;
    private final com.signition.samskybridge.data.DataStore store;
    private final LevelService level;
    private final UpgradeService upgrade;
    private final RankingService ranking;

    public IslandCommand(Main plugin, com.signition.samskybridge.data.DataStore store, LevelService level, UpgradeService upgrade, RankingService ranking) {
        this.plugin = plugin;
        this.store = store;
        this.level = level;
        this.upgrade = upgrade;
        this.ranking = ranking;
    }

    private 
void sendHelp(Player p, int page){
        int total = 2;
        if (page < 1) page = 1;
        if (page > total) page = total;
        p.sendMessage(Text.color("&a섬 명령어 &7(페이지 "+page+"/"+total+")"));
        if (page == 1){
            p.sendMessage(Text.color("&7/섬 레벨 &f: 섬 레벨 확인"));
            p.sendMessage(Text.color("&7/섬 랭킹 &f: 섬 랭킹 보기"));
            p.sendMessage(Text.color("&7/섬 업그레이드 &f: 업그레이드 GUI 열기"));
            p.sendMessage(Text.color("&7/섬 설치 <광산|농장> &f: 섬 내에서만 설치"));
            p.sendMessage(Text.color("&7/섬 제거 <광산|농장> &f: 설치 구조물 제거"));
        p.sendMessage(Text.color("&7/섬 매물 &f: 섬 매물 GUI 열기"));
        p.sendMessage(Text.color("&7/섬 매물 등록 <가격> &f: 섬장만 등록 가능"));
        p.sendMessage(Text.color("&7/섬 관리 승급 <닉> &f: 섬원 → 부섬장"));
        p.sendMessage(Text.color("&7/섬 관리 강등 <닉> &f: 부섬장 → 섬원"));
            p.sendMessage(Text.color("&7/섬 초대 <닉>/수락/거절 &f: 팀에 초대 (/is team invite)"));
            p.sendMessage(Text.color("&7/섬 강퇴 <닉> &f: 팀원 강퇴 (/is team kick)"));
            p.sendMessage(Text.color("&7/섬 홈 &f: (/is home 위임)"));
        } else if (page == 2){
            if (p.hasPermission("samsky.admin")){
                p.sendMessage(Text.color("&8---- 관리자 ----"));
                p.sendMessage(Text.color("&7/섬 설정 리로드 &f: 설정/ 리로드"));
                p.sendMessage(Text.color("&7/섬 설정 보기 &f: 주요 설정값 확인"));
                p.sendMessage(Text.color("&7/섬 설정 바리어 시간 <초> &f: 방벽 표시 시간 변경"));
                p.sendMessage(Text.color("&7/섬 설정 레벨 증가율 <퍼센트> &f: 필요 경험치 증가율(%)"));
                p.sendMessage(Text.color("&7/섬 설정 비용 size base|multiplier <값> &f: 섬 크기 비용/증가율"));
                p.sendMessage(Text.color("&7/섬 설정 비용 team base|multiplier <값> &f: 팀원 비용/증가율"));
                p.sendMessage(Text.color("&7/섬 설정 랭킹접두어 <형식> &f: [ 섬 랭킹 <rank>위 ] 포맷"));
                p.sendMessage(Text.color("&7/섬 설정 저장 &f: config.yml 저장"));
            } else {
                p.sendMessage(Text.color("&7/섬 도움말 &f: 이 도움말 보기"));
                p.sendMessage(Text.color("&7/섬 <기타명령> ... &f: BentoBox /is 로 위임"));
            }
        }
        p.sendMessage(Text.color("&7다음 페이지: &f/섬 페이지 "+(page<total?page+1:total)));
    }

    void sendHelp(Player p){

        p.sendMessage(Text.color("&7/섬 레벨 &f: 섬 레벨 확인"));
        p.sendMessage(Text.color("&7/섬 업그레이드 &f: 업그레이드 GUI 열기"));
            p.sendMessage(Text.color("&7/섬 설치 <광산|농장> &f: 섬 내에서만 설치"));
            p.sendMessage(Text.color("&7/섬 제거 <광산|농장> &f: 설치 구조물 제거"));
        p.sendMessage(Text.color("&7/섬 매물 &f: 섬 매물 GUI 열기"));
        p.sendMessage(Text.color("&7/섬 매물 등록 <가격> &f: 섬장만 등록 가능"));
        p.sendMessage(Text.color("&7/섬 관리 승급 <닉> &f: 섬원 → 부섬장"));
        p.sendMessage(Text.color("&7/섬 관리 강등 <닉> &f: 부섬장 → 섬원"));
        p.sendMessage(Text.color("&7/섬 랭킹 &f: 섬 랭킹 보기"));
        p.sendMessage(Text.color("&7/섬 초대 <닉>/수락/거절 &f: 팀에 초대 (/is team invite)"));
        p.sendMessage(Text.color("&7/섬 강퇴 <닉> &f: 팀원 강퇴 (/is team kick)"));
        if (p.hasPermission("samsky.admin")){
            p.sendMessage(Text.color("&8---- 관리자 ----"));
            p.sendMessage(Text.color("&7/섬 설정 리로드 &f: 설정/ 리로드"));
            p.sendMessage(Text.color("&7/섬 설정 보기 &f: 주요 설정값 확인"));
            p.sendMessage(Text.color("&7/섬 설정 바리어 시간 <초> &f: 방벽 표시 시간 변경"));
            p.sendMessage(Text.color("&7/섬 설정 레벨 증가율 <퍼센트> &f: 필요 경험치 증가율(%)"));
            p.sendMessage(Text.color("&7/섬 설정 비용 size base|multiplier <값> &f: 섬 크기 비용/증가율"));
            p.sendMessage(Text.color("&7/섬 설정 비용 team base|multiplier <값> &f: 팀원 비용/증가율"));
            p.sendMessage(Text.color("&7/섬 설정 랭킹접두어 <형식> &f: [ 섬 랭킹 <rank>위 ] 포맷"));
            p.sendMessage(Text.color("&7/섬 설정 저장 &f: config.yml 저장"));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)){
            sender.sendMessage(Text.color(plugin.getConfig().getString("messages.not-player","플레이어만 사용가능")));
            return true;
        }
        Player p = (Player) sender;
        if (args.length == 0){
            sendHelp(p, 1);
            return true;
        }
        switch (args[0]){
            case "페이지": {
                int pg = 1;
                try { if (args.length > 1) pg = Integer.parseInt(args[1]); } catch (Exception ignored){}
                sendHelp(p, pg);
                return true;
            }
            case "도움말": {
                sendHelp(p, 1);
                return true;
            }
            case "설치": {
                if (args.length < 2){ p.sendMessage(Text.color("&c사용법: /섬 설치 <광산|농장>")); return true; }
                if ("광산".equalsIgnoreCase(args[1])){ plugin.getFeatures().installMine(p); return true; }
                if ("농장".equalsIgnoreCase(args[1])){ plugin.getFeatures().installFarm(p); return true; }
                p.sendMessage(Text.color("&c알 수 없는 대상: " + args[1]));
                return true;
            }
            case "제거": {
                if (args.length < 2){ p.sendMessage(Text.color("&c사용법: /섬 제거 <광산|농장>")); return true; }
                if ("광산".equalsIgnoreCase(args[1])){ plugin.getFeatures().removeMine(p); return true; }
                if ("농장".equalsIgnoreCase(args[1])){ plugin.getFeatures().removeFarm(p); return true; }
                p.sendMessage(Text.color("&c알 수 없는 대상: " + args[1]));
                return true;
            }
        

            case "레벨": {
                IslandData is = level.getIslandOf(p);
                long need = level.requiredXpForLevel(is.getLevel());
                double percent = Math.min(100.0, (is.getXp() * 100.0) / need);
                String msg = plugin.getConfig().getString("messages.level.status","섬 레벨")
                        .replace("<level>", String.valueOf(is.getLevel()))
                        .replace("<xp>", String.valueOf(is.getXp()))
                        .replace("<need>", String.valueOf(need))
                        .replace("<percent>", String.format("%.1f", percent));
                p.sendMessage(Text.color(msg));
                
            }
    int _size = is.getSize();
    int _team = is.getTeamMax();
    int _mine = plugin.getFeatures().getMineLevel(is.getId());
    int _farm = plugin.getFeatures().getFarmLevel(is.getId());
    p.sendMessage(Text.color("&a[업그레이드] &7크기: &f"+_size+" &7인원: &f"+_team));
    p.sendMessage(Text.color("&a[특화] &7광산: &fLv."+_mine+" &7농장: &fLv."+_farm));
    return true;
}

            case "업그레이드": {
                upgrade.openGui(p);
                return true;
            }
            case "매물": {
                if (args.length >= 2 && args[1].equalsIgnoreCase("등록")){
                    if (args.length < 3){ p.sendMessage(Text.color("&c사용법: /섬 매물 등록 <가격>")); return true; }
                    double price;
                    try { price = Double.parseDouble(args[2]); }
                    catch (Exception e){ p.sendMessage(Text.color("&c가격은 숫자여야 합니다.")); return true; }
                    new com.signition.samskybridge.market.MarketService(plugin, plugin.getDataStore(), plugin.getVault(), plugin.getBento()).register(p, price);
                    return true;
                }
                int page = 1; try { if (args.length>1) page = Integer.parseInt(args[1]); } catch(Exception ignored){}
                new com.signition.samskybridge.market.MarketService(plugin, plugin.getDataStore(), plugin.getVault(), plugin.getBento()).openGui(p, page);
                return true;
            }
            case "관리": {
                if (args.length < 3){
                    p.sendMessage(Text.color("&c사용법: /섬 관리 <승급|강등> <닉>"));
                    return true;
                }
                String sub = args[1];
                String target = args[2];
                if (sub.equalsIgnoreCase("승급")){
                    p.performCommand("is team setrank " + target + " officer");
                    p.sendMessage(Text.color("&a"+target+"님을 부섬장으로 승급시켰습니다."));
                    return true;
                }
                if (sub.equalsIgnoreCase("강등")){
                    p.performCommand("is team setrank " + target + " member");
                    p.sendMessage(Text.color("&a"+target+"님을 멤버로 강등시켰습니다."));
                    return true;
                }
                p.sendMessage(Text.color("&c사용법: /섬 관리 <승급|강등> <닉>"));
                return true;
            }
        
            case "랭킹": {
                ranking.sendTop(p, 10);
                return true;
            }
            
            case "초대": {
                if (args.length < 2){ p.sendMessage(Text.color("&c사용법: /섬 초대 <닉네임>")); return true; }
                
// Allow "/섬 초대 수락" and "/섬 초대 거절"
if (args.length == 2){
    if ("수락".equalsIgnoreCase(args[1])){ forwardToIs(p, "team accept"); return true; }
    if ("거절".equalsIgnoreCase(args[1])){ forwardToIs(p, "team reject"); return true; }
}
forwardToIs(p, "team invite " + args[1]);
                return true;
            }
            case "강퇴": {
                if (args.length < 2){ p.sendMessage(Text.color("&c사용법: /섬 강퇴 <닉네임>")); return true; }
                forwardToIs(p, "team kick " + args[1]);
                return true;
            }

case "알바": {
    if (args.length < 2){ p.sendMessage(Text.color("&c사용법: /섬 알바 <닉네임>")); return true; }
    forwardToIs(p, "coop " + args[1]);
    return true;
}
case "설정": {
                if (!p.hasPermission("samsky.admin")){
                    p.sendMessage(Text.color("&c권한이 없습니다."));
                    return true;
                }
                if (args.length == 1){
                    sendHelp(p);
                    return true;
                }
                // /섬 설정 리로드
                if ("리로드".equals(args[1])){
                    plugin.reloadConfig();
                    level.reloadBlocks();
                    p.sendMessage(Text.color("&a설정이 리로드되었습니다."));
                    return true;
                }
                // /섬 설정 보기
                if ("보기".equals(args[1])){
                    p.sendMessage(Text.color("&7increase-percent: &f"+plugin.getConfig().getDouble("leveling.increase-percent",1.5)));
                    p.sendMessage(Text.color("&7barrier.show-seconds: &f"+plugin.getConfig().getInt("barrier.show-seconds",6)));
                    p.sendMessage(Text.color("&7economy.size: base &f"+plugin.getConfig().getDouble("economy.costs.size.base",10000.0)+" &7multiplier &f"+plugin.getConfig().getDouble("economy.costs.size.multiplier",1.25)));
                    p.sendMessage(Text.color("&7economy.team: base &f"+plugin.getConfig().getDouble("economy.costs.team.base",5000.0)+" &7multiplier &f"+plugin.getConfig().getDouble("economy.costs.team.multiplier",1.5)));
                    p.sendMessage(Text.color("&7scoreboard.prefix-format: &f"+plugin.getConfig().getString("scoreboard.prefix-format","&7[ &a섬 랭킹 &f<rank>위 &7] ")));
                    return true;
                }
                // /섬 설정 바리어 시간 <초>
                if ("바리어".equals(args[1]) && args.length>=4 && "시간".equals(args[2])){
                    try{
                        int v = Integer.parseInt(args[3]);
                        plugin.getConfig().set("barrier.show-seconds", v);
                        p.sendMessage(Text.color("&abarrier.show-seconds = "+v));
                    }catch (Exception ex){ p.sendMessage(Text.color("&c숫자를 입력하세요.")); }
                    return true;
                }
                // /섬 설정 레벨 증가율 <퍼센트>
                if ("레벨".equals(args[1]) && args.length>=4 && "증가율".equals(args[2])){
                    try{
                        double v = Double.parseDouble(args[3]);
                        plugin.getConfig().set("leveling.increase-percent", v);
                        p.sendMessage(Text.color("&aincrease-percent = "+v));
                    }catch (Exception ex){ p.sendMessage(Text.color("&c숫자를 입력하세요.")); }
                    return true;
                }
                // /섬 설정 비용 size|team base|multiplier <값>
                if ("비용".equals(args[1]) && args.length>=5){
                    String cat = args[2]; String key = args[3];
                    try{
                        double v = Double.parseDouble(args[4]);
                        if ("size".equals(cat) && ("base".equals(key) || "multiplier".equals(key))){
                            plugin.getConfig().set("economy.costs.size."+key, v);
                            p.sendMessage(Text.color("&aeconomy.costs.size."+key+" = "+v));
                        } else if ("team".equals(cat) && ("base".equals(key) || "multiplier".equals(key))){
                            plugin.getConfig().set("economy.costs.team."+key, v);
                            p.sendMessage(Text.color("&aeconomy.costs.team."+key+" = "+v));
                        } else {
                            p.sendMessage(Text.color("&c사용법: /섬 설정 비용 size|team base|multiplier <값>"));
                        }
                    }catch (Exception ex){ p.sendMessage(Text.color("&c숫자를 입력하세요.")); }
                    return true;
                }
                // /섬 설정 랭킹접두어 <형식>
                if ("랭킹접두어".equals(args[1]) && args.length>=3){
                    StringBuilder sb = new StringBuilder();
                    for (int i=2;i<args.length;i++){
                        if (i>2) sb.append(" ");
                        sb.append(args[i]);
                    }
                    String fmt = sb.toString();
                    plugin.getConfig().set("tab_prefix.format_dynamic", fmt);
                    plugin.saveConfig();
                    p.sendMessage(Text.color("&a랭킹 접두어 형식이 변경되었습니다: &r" + fmt));
                    return true;
                }
            }
            default:
                // 기타 서브명령은 BentoBox /is로 위임
                String joined = String.join(" ", args);
                if (!joined.isEmpty()) { forwardToIs(p, joined); return true; }
                sendHelp(p);
                return true;
        }
    }
