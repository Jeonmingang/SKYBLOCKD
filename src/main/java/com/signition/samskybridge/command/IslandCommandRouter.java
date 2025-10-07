
package com.signition.samskybridge.command;

import com.signition.samskybridge.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.*;

public class IslandCommandRouter implements CommandExecutor, TabCompleter {
    private final Main plugin;
    public IslandCommandRouter(Main plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) { sender.sendMessage("플레이어만 사용할 수 있습니다."); return true; }
        Player p=(Player)sender;
        if (args.length==0) { help(p); return true; }
        String sub=args[0];

        if (eq(sub,"채팅","chat")) {
            if (!callService("getChatService","toggle", new Class[]{Player.class}, new Object[]{p})) {
                p.sendMessage("§6[섬] §f섬 채팅 토글");
            }
            return true;
        }
        if (eq(sub,"업그레이드","upgrade","upg")) {
            if (!callService("getUpgradeService","openUpgradeGui", new Class[]{Player.class}, new Object[]{p})) {
                p.sendMessage("§6[섬] §f업그레이드 GUI");
            }
            return true;
        }
        if (eq(sub,"레벨","level","lvl")) {
            if (!callService("getLevelService","show", new Class[]{Player.class}, new Object[]{p})) {
                p.sendMessage("§6[섬] §fLv.1 (예시)");
            }
            return true;
        }
        if (eq(sub,"정보","info")) { if (!callService("getInfoService","show", new Class[]{org.bukkit.entity.Player.class}, new Object[]{p})) { p.sendMessage("§6[섬] §f섬 정보 (예시)"); } return true; }
        if (eq(sub,"랭킹","ranking","rank","top")) { if (!callService("getRankingUiService","openOrRefresh", new Class[]{org.bukkit.entity.Player.class}, new Object[]{p})) { p.sendMessage("§e[섬] 랭킹 UI를 열 수 없습니다."); } return true; }

        p.sendMessage("§c알 수 없는 하위 명령입니다. §7/섬 §f로 도움말을 확인하세요."); 
        return true;
    }

    private boolean callService(String getterName, String methodName, Class<?>[] sig, Object[] args) {
        try {
            Method getter = plugin.getClass().getMethod(getterName);
            Object svc = getter.invoke(plugin);
            if (svc == null) return false;
            Method m = svc.getClass().getMethod(methodName, sig);
            m.invoke(svc, args);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private void help(Player p) {
        p.sendMessage("§6[섬] §f사용법:");
        p.sendMessage(" §e/섬 채팅 §7- 섬 채팅 토글");
        p.sendMessage(" §e/섬 업그레이드 §7- 섬 크기/인원 업글 GUI");
        p.sendMessage(" §e/섬 레벨 §7- 섬 레벨/XP 정보");
        p.sendMessage(" §e/섬 정보 §7- 섬 정보 보기");
        p.sendMessage(" §e/섬 랭킹 §7- 랭킹/탭 새로고침");
    }

    private boolean eq(String s, String... keys) {
        String x=s.toLowerCase(Locale.ROOT);
        for (String k: keys) if (x.equals(k.toLowerCase(Locale.ROOT))) return true;
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> base=Arrays.asList("채팅","업그레이드","레벨","정보","랭킹");
        if (args.length==1) {
            String p=args[0]; List<String> out=new ArrayList<>();
            for (String s: base) if (s.startsWith(p)) out.add(s);
            return out;
        }
        return Collections.emptyList();
    }
}
