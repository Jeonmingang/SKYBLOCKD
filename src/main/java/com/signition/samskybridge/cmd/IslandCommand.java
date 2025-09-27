package com.signition.samskybridge.cmd;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IslandCommand implements CommandExecutor {

    private final Main plugin;

    public IslandCommand(Main plugin){ this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if (!(sender instanceof Player)){ sender.sendMessage("Players only."); return true; }
        Player p = (Player) sender;

        if (args.length == 0){ sendHelp(p); return true; }

        switch (args[0]){
            case "설치": {
                if (args.length < 2){ p.sendMessage(Text.color("&c사용법: /섬 설치 <광산|농장>")); return true; }
                if ("광산".equalsIgnoreCase(args[1])){ plugin.getFeatureService().installMine(p); return true; }
                if ("농장".equalsIgnoreCase(args[1])){ plugin.getFeatureService().installFarm(p); return true; }
                p.sendMessage(Text.color("&c알 수 없는 대상: " + args[1])); return true;
            }
            case "제거": {
                if (args.length < 2){ p.sendMessage(Text.color("&c사용법: /섬 제거 <광산|농장>")); return true; }
                if ("광산".equalsIgnoreCase(args[1])){ plugin.getFeatureService().removeMine(p); return true; }
                if ("농장".equalsIgnoreCase(args[1])){ p.sendMessage(Text.color("&c농장 제거는 수동 파괴로 처리됩니다.")); return true; }
                p.sendMessage(Text.color("&c알 수 없는 대상: " + args[1])); return true;
            }
            case "업그레이드": { plugin.getUpgradeService().openGUI(p); return true; }
            case "랭킹": {
                java.util.List<java.util.UUID> top = plugin.getRankingService().top(10);
                String rh = plugin.getConfig().getString("messages.plugin.getRankingService().header", "&a섬 랭킹 TOP <n>");
                p.sendMessage(com.signition.samskybridge.util.Text.color(rh.replace("<n>", String.valueOf(top.size()))));
                int i=1; for (java.util.UUID id: top){
                    String name = org.bukkit.Bukkit.getOfflinePlayer(id).getName();
                    long xp = plugin.getDataStore().getXP(id);
                    int lvShown = plugin.getLevelService().getLevel(p); // placeholder
                    String rl = plugin.getConfig().getString("messages.plugin.getRankingService().line", "&f<rank>. &a<name> &7- &f레벨 <level> &7(경험치 <xp>)");
                    String r = rl.replace("<rank>", String.valueOf(i++))
                                 .replace("<name>", (name==null?"Unknown":name))
                                 .replace("<level>", String.valueOf(lvShown))
                                 .replace("<xp>", String.valueOf(xp));
                    p.sendMessage(com.signition.samskybridge.util.Text.color(r));
                }
                
                int myRank = plugin.getRankingService().rankOf(p.getUniqueId());
                if (myRank > 0) {
                    String yr = plugin.getConfig().getString("messages.plugin.getRankingService().your-rank", "&a당신의 순위: &f<rank>위");
                    p.sendMessage(com.signition.samskybridge.util.Text.color(yr.replace("<rank>", String.valueOf(myRank))));
                } 
return true;
            }
            case "채팅": {
                boolean on = plugin.getChatService().toggle(p);
                p.sendMessage(on ? "§a섬 채팅 ON" : "§c섬 채팅 OFF");
                return true;
            }
            case "매물": {
                p.sendMessage(Text.color("&b매물 목록"));
                for (java.util.Map.Entry<java.util.UUID, Long> e : plugin.getMarketService().all().entrySet()){
                    String name = org.bukkit.Bukkit.getOfflinePlayer(e.getKey()).getName();
                    p.sendMessage(Text.color("&7- &f" + name + " &7: &a" + e.getValue()));
                }
                return true;
            }
            case "매물등록":
            case "매물_등록":
            case "매물-등록":
                if (args.length >= 2){
                    try {
                        long price = Long.parseLong(args[1]);
                        plugin.getMarketService().list(p, price);
                        p.sendMessage(Text.color("&a섬을 &f" + price + " &a에 등록했습니다."));
                    } catch (NumberFormatException ex){
                        p.sendMessage(Text.color("&c가격은 숫자여야 합니다."));
                    }
                    return true;
                }
                break;
            case "설정":
                if (args.length >= 2 && "리로드".equals(args[1])){
                    plugin.reloadConfig();
                    p.sendMessage(Text.color("&a리로드 완료"));
                    return true;
                }
                break;
            
            case "레벨": {
                long xp = plugin.getDataStore().getXP(p.getUniqueId());
                int lv = plugin.getLevelService().getLevel(p.getUniqueId());
                long need = plugin.getLevelService().needFor(lv+1);
                long cur = xp;
                long remain = Math.max(0, need - cur);
                double percent = Math.min(100.0, (cur * 100.0) / Math.max(1.0, need));
                String tpl = plugin.getConfig().getString("messages.level.status", "&a섬 레벨: &f<level> &7(&f<xp>&7/&f<need>&7, &f<percent>%&7)");
                String out = tpl.replace("<level>", String.valueOf(lv)).replace("<xp>", String.valueOf(cur))
                               .replace("<need>", String.valueOf(need)).replace("<percent>", String.format("%.1f", percent));
                p.sendMessage(com.signition.samskybridge.util.Text.color(out));
                return true;
            }
    
            default: {
                // passthrough to BentoBox's /is command
                StringBuilder sb = new StringBuilder("is");
                for (String a : args) sb.append(" ").append(a);
                p.performCommand(sb.toString());
                return true;
            }
        }
        sendHelp(p);
        return true;
    }

    private void sendHelp(Player p){
        p.sendMessage(Text.color("&b&lSamSkyBridge 도움말"));
        p.sendMessage(Text.color("&7/섬 설치 <광산|농장>"));
        p.sendMessage(Text.color("&7/섬 제거 <광산|농장>"));
        p.sendMessage(Text.color("&7/섬 업그레이드"));
        p.sendMessage(Text.color("&7/섬 랭킹"));
        p.sendMessage(Text.color("&7/섬 매물  |  /섬 매물등록 <가격>"));
        p.sendMessage(Text.color("&7/섬 채팅"));
        p.sendMessage(Text.color("&7/섬 설정 리로드"));
    }
}
