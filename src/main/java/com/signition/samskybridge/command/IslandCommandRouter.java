
package com.signition.samskybridge.command;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.rank.RankingService;
import com.signition.samskybridge.service.ChatService;
import com.signition.samskybridge.service.LevelService;
import com.signition.samskybridge.service.InfoService;
import com.signition.samskybridge.service.RankingUiService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class IslandCommandRouter implements CommandExecutor, TabCompleter {
    private final Main plugin;
    private final ChatService chat; private final LevelService lvl; private final InfoService info; private final RankingUiService rankUi;

    public IslandCommandRouter(Main plugin){ this.plugin = plugin; this.chat = plugin.getChatService(); this.lvl = plugin.getLevelService(); this.info = plugin.getInfoService(); this.rankUi = plugin.getRankingUiService(); } catch (Throwable ignore) {}
        try { r = (RankingService) Main.class.getMethod("getRankingService").invoke(plugin); } catch (Throwable ignore) {}
        this.upgrade = u;
        this.ranking = r;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("플레이어만 사용할 수 있습니다.");
            return true;
        }
        Player p = (Player) sender;
        if (args.length == 0) {
            help(p);
            return true;
        }
        String sub = args[0];

        if (eq(sub, "채팅","chat")) { chat.toggle(p); return true; }
            return true;
        }
        if (eq(sub, "업그레이드","upgrade","upg")) { plugin.getUpgradeService().openUpgradeGui(p); return true; }
                catch (Throwable t) { Bukkit.dispatchCommand(p, "is"); }
            } else { Bukkit.dispatchCommand(p, "is"); }
            return true;
        }
        if (eq(sub, "레벨","level","lvl")) { lvl.show(p); return true; }
            return true;
        }
        if (eq(sub, "정보","info")) { info.show(p); return true; }
            return true;
        }
        if (eq(sub, "랭킹","ranking","rank","top")) { rankUi.openOrRefresh(p); return true; } catch (Throwable t) {
                    if (!Bukkit.dispatchCommand(p, "is top")) { p.sendMessage("§7랭킹 새로고침 완료."); }
                }
            } else {
                if (!Bukkit.dispatchCommand(p, "is top")) { p.sendMessage("§7랭킹 정보를 확인했습니다."); }
            }
            return true;
        }

        p.sendMessage("§c알 수 없는 하위 명령입니다. §7/섬 §f로 도움말을 확인하세요."); return true;
    }

    private void help(Player p){
        p.sendMessage("§6[섬] §f사용법:");
        p.sendMessage(" §e/섬 채팅 §7- 섬 채팅 토글");
        p.sendMessage(" §e/섬 업그레이드 §7- 섬 크기/인원 업글 GUI");
        p.sendMessage(" §e/섬 레벨 §7- 섬 레벨/XP 정보");
        p.sendMessage(" §e/섬 정보 §7- 섬 정보 보기");
        p.sendMessage(" §e/섬 랭킹 §7- 랭킹/탭 새로고침");
    }

    private boolean eq(String s, String... keys){
        String x = s.toLowerCase(Locale.ROOT);
        for (String k: keys) if (x.equals(k.toLowerCase(Locale.ROOT))) return true;
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> base = Arrays.asList("채팅","업그레이드","레벨","정보","랭킹");
        if (args.length == 1) {
            String p = args[0];
            List<String> out = new ArrayList<>();
            for (String s : base) if (s.startsWith(p)) out.add(s);
            return out;
        }
        return Collections.emptyList();
    }
}
