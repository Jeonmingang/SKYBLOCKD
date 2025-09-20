
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

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Text.color(plugin.getConfig().getString("messages.not-player","&c플레이어만 사용할 수 있습니다.")));
            return true;
        }
        Player p = (Player) sender;
        if (args.length == 0) {
            sendHelp(p, 1);
            return true;
        }
        String sub = args[0];
        switch (sub) {
            case "도움말": {
                int page = 1;
                if (args.length >= 2) try { page = Integer.parseInt(args[1]); } catch (Exception ignored){}
                sendHelp(p, page);
                return true;
            }
            case "레벨": {
                IslandData is = level.getIslandOf(p);
                long need = 0L;
                try { need = level.requiredXpForLevel(is.getLevel()); } catch (Throwable t){ need = level.requiredXp(is.getLevel()); }
                double percent = need > 0 ? Math.min(100.0, (is.getXp() * 100.0) / need) : 100.0;
                String msg = plugin.getConfig().getString("messages.level.status","&a섬 레벨: &f<level> &7(&f<xp>&7/&f<need>&7, &f<percent>%&7)");
                msg = msg.replace("<level>", String.valueOf(is.getLevel()))
                         .replace("<xp>", String.valueOf(is.getXp()))
                         .replace("<need>", String.valueOf(need))
                         .replace("<percent>", String.format("%.1f", percent));
                p.sendMessage(Text.color(msg));
                return true;
            }
            case "업그레이드": {
                try { upgrade.openGui(p); } catch (Throwable t){ p.sendMessage(Text.color("&c업그레이드 GUI를 여는 중 오류가 발생했습니다.")); }
                return true;
            }
            case "랭킹": {
                int n = 10;
                if (args.length >= 2) { try { n = Math.max(1, Integer.parseInt(args[1])); } catch (Exception ignored) {} }
                try { ranking.sendTop(p, n); } catch (Throwable t){ p.sendMessage(Text.color("&c랭킹 표시 중 오류가 발생했습니다.")); }
                return true;
            }
            case "설정": {
                if (!p.hasPermission("samsky.admin")) {
                    p.sendMessage(Text.color("&c권한이 없습니다."));
                    return true;
                }
                if (args.length >= 2 && "리로드".equals(args[1])){
                    plugin.reloadConfig();
                    p.sendMessage(Text.color("&a설정이 리로드되었습니다."));
                    return true;
                }
                if (args.length >= 2 && "보기".equals(args[1])){
                    double base = plugin.getConfig().getDouble("leveling.base-required-xp", 1000.0);
                    double inc = plugin.getConfig().getDouble("leveling.increase-percent", 1.5);
                    p.sendMessage(Text.color("&7base-required-xp: &f"+base));
                    p.sendMessage(Text.color("&7increase-percent: &f"+inc));
                    return true;
                }
                if (args.length >= 2 && "저장".equals(args[1])){
                    try { store.save(); } catch (Exception ignored){}
                    p.sendMessage(Text.color("&a저장되었습니다."));
                    return true;
                }
                sendHelp(p, 2);
                return true;
            }
            default: {
                sendHelp(p, 1);
                return true;
            }
        }
    }

    private void sendHelp(Player p, int page){
        if (page < 1) page = 1;
        if (page == 1){
            p.sendMessage(Text.color("&a섬 명령어"));
            p.sendMessage(Text.color("&7/섬 레벨 &f: 섬 레벨 확인"));
            p.sendMessage(Text.color("&7/섬 랭킹 [수] &f: 섬 랭킹 보기"));
            p.sendMessage(Text.color("&7/섬 업그레이드 &f: 업그레이드 GUI 열기"));
        } else {
            if (p.hasPermission("samsky.admin")){
                p.sendMessage(Text.color("&8---- 관리자 ----"));
                p.sendMessage(Text.color("&7/섬 설정 리로드 &f: 설정 리로드"));
                p.sendMessage(Text.color("&7/섬 설정 보기 &f: 주요 설정값 확인"));
                p.sendMessage(Text.color("&7/섬 설정 저장 &f: 데이터 저장"));
            }
        }
    }
}
