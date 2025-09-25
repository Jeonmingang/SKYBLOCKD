package com.signition.samskybridge.cmd;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.rank.RankingService;
import com.signition.samskybridge.upgrade.UpgradeService;
import com.signition.samskybridge.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IslandCommand implements CommandExecutor {

    private final Main plugin;
    private final DataStore store;
    private final LevelService level;
    private final UpgradeService upgrade;
    private final RankingService ranking;

    public IslandCommand(Main plugin, DataStore store, LevelService level, UpgradeService upgrade, RankingService ranking){
        this.plugin = plugin;
        this.store = store;
        this.level = level;
        this.upgrade = upgrade;
        this.ranking = ranking;
    }

    private boolean forwardToIs(Player p, String args){
        try { p.performCommand("is " + args); return true; } catch(Throwable t) { return false; }
    }

    private void sendHelp(Player p){ sendHelp(p, 1); }

    private void sendHelp(Player p, int page){
        p.sendMessage(Text.color("&a[ 섬 명령 도움말 ]"));
        if (page <= 1){
            p.sendMessage(Text.color("&7/섬 도움말 &f: 이 도움말"));
            p.sendMessage(Text.color("&7/섬 업그레이드 &f: 업그레이드 GUI 열기"));
            p.sendMessage(Text.color("&7/섬 레벨 &f: 섬 레벨/XP 보기"));
            p.sendMessage(Text.color("&7/섬 설치 <광산|농장> &f: 설치"));
            p.sendMessage(Text.color("&7/섬 제거 <광산|농장> &f: 제거(레벨 유지)"));
            p.sendMessage(Text.color("&7/섬 매물 &f: 매물 GUI / &7/섬 매물 등록 <가격>"));
            p.sendMessage(Text.color("&7/섬 채팅 &f: 섬 채팅 토글"));
            p.sendMessage(Text.color("&7/섬 설정 리로드 &f: 설정 리로드"));
        } else {
            p.sendMessage(Text.color("&7/섬 초대 <닉> &f: 팀 초대"));
            p.sendMessage(Text.color("&7/섬 강퇴 <닉> &f: 팀 강퇴"));
            p.sendMessage(Text.color("&7/섬 관리 <승급|강등> <닉> &f: 등급 관리"));
            p.sendMessage(Text.color("&7/섬 탈퇴 &f: 팀 탈퇴"));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)){
            sender.sendMessage(Text.color(plugin.getConfig().getString("messages.not-player","플레이어만 사용가능")));
            return true;
        }
        Player p = (Player) sender;
        if (args.length == 0){ sendHelp(p, 1); return true; }

        switch (args[0]) {
            case "페이지": {
                int pg = 1;
                try { if (args.length > 1) pg = Integer.parseInt(args[1]); } catch(Exception ignore){}
                sendHelp(p, pg);
                return true;
            }
            case "도움말": { sendHelp(p, 1); return true; }
            case "탈퇴": { p.performCommand("is team leave"); return true; }

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
                double percent = Math.min(100.0, (is.getXp() * 100.0) / Math.max(1, need));
                String msg = plugin.getConfig().getString("messages.level.status","섬 레벨 <level> / XP <xp>/<need> (<percent>%)")
                        .replace("<level>", String.valueOf(is.getLevel()))
                        .replace("<xp>", String.valueOf(is.getXp()))
                        .replace("<need>", String.valueOf(need))
                        .replace("<percent>", String.format("%.1f", percent));
                p.sendMessage(Text.color(msg));
                int _size = is.getSize();
                int _team = is.getTeamMax();
                int _mine = plugin.getFeatures().getMineLevel(is.getId());
                int _farm = plugin.getFeatures().getFarmLevel(is.getId());
                p.sendMessage(Text.color("&a[업그레이드] &7크기: &f"+_size+" &7인원: &f"+_team));
                p.sendMessage(Text.color("&a[특화] &7광산: &fLv."+_mine+" &7농장: &fLv."+_farm));
                return true;
            }

            case "업그레이드": { upgrade.openGui(p); return true; }

            case "매물": {
                if (args.length >= 2 && args[1].equalsIgnoreCase("등록")){
                    if (args.length < 3){ p.sendMessage(Text.color("&c사용법: /섬 매물 등록 <가격>")); return true; }
                    double price;
                    try { price = Double.parseDouble(args[2]); }
                    catch (Exception e){ p.sendMessage(Text.color("&c가격은 숫자여야 합니다.")); return true; }
                    new com.signition.samskybridge.market.MarketService(plugin, store, plugin.getVault(), plugin.getBento()).register(p, price);
                    return true;
                }
                int page = 1; try { if (args.length>1) page = Integer.parseInt(args[1]); } catch(Exception ignore){}
                new com.signition.samskybridge.market.MarketService(plugin, store, plugin.getVault(), plugin.getBento()).openGui(p, page);
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

            case "초대": {
                if (args.length < 2){ p.sendMessage(Text.color("&c사용법: /섬 초대 <닉네임>")); return true; }
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

            case "설정": {
                if (!p.hasPermission("samsky.admin")){
                    p.sendMessage(Text.color("&c권한이 없습니다."));
                    return true;
                }
                if (args.length == 1){ sendHelp(p); return true; }

                if ("리로드".equals(args[1]) || "reload".equalsIgnoreCase(args[1])){
                    plugin.reloadConfig();
                    try { level.reloadBlocks(); } catch(Throwable ignore){}
                    try { if (plugin.getFeatureService()!=null) plugin.getFeatureService().reload(); } catch(Throwable ignore){}
                    try { if (plugin.getChatService()!=null) plugin.getChatService().reload(); } catch(Throwable ignore){}
                    try { if (plugin.getTablistService()!=null) plugin.getTablistService().reload(); } catch(Throwable ignore){}
                    p.sendMessage(Text.color("&a설정이 리로드되었습니다."));
                    return true;
                }

                if ("보기".equals(args[1])){
                    p.sendMessage(Text.color("&7increase-percent: &f"+plugin.getConfig().getDouble("leveling.increase-percent",20.0)));
                    p.sendMessage(Text.color("&7barrier.show-seconds: &f"+plugin.getConfig().getInt("barrier.show-seconds",6)));
                    p.sendMessage(Text.color("&7economy.size.multiplier: &f"+plugin.getConfig().getDouble("economy.costs.size.multiplier",1.25)));
                    p.sendMessage(Text.color("&7economy.team.multiplier: &f"+plugin.getConfig().getDouble("economy.costs.team.multiplier",1.5)));
                    p.sendMessage(Text.color("&7scoreboard.prefix-format: &f"+plugin.getConfig().getString("scoreboard.prefix-format","&7[ &a섬 랭킹 &f<rank>위 &7] ")));
                    return true;
                }

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

                return true;
            }


            case "랭킹":
            case "ranking":
            case "rank":
            case "top": {
                int page = 1;
                if (args.length >= 2) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ex) {
                        p.sendMessage(Text.color("&c숫자를 입력하세요."));
                        return true;
                    }
                }
                plugin.getRankingService().sendTop(p, page);
                return true;
            }
            default: {
                String joined = String.join(" ", args);
                if (!joined.isEmpty()) { forwardToIs(p, joined); return true; }
                sendHelp(p);
                return true;
            }
        }
    }
}
