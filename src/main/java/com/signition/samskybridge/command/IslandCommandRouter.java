package com.signition.samskybridge.command;

import com.signition.samskybridge.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IslandCommandRouter implements CommandExecutor, TabCompleter {
    private final Main plugin;
    public IslandCommandRouter(Main plugin){ this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)){
            sender.sendMessage("플레이어만 사용 가능합니다.");
            return true;
        }
        Player p = (Player) sender;
        if (args.length == 0){
            p.sendMessage("§6[섬 도움말]");
            p.sendMessage(" §e/섬 업그레이드 §7- 섬 크기/인원/경험치 GUI");
            p.sendMessage(" §e/섬 레벨 §7- 섬 레벨/XP 정보");
            p.sendMessage(" §e/섬 정보 §7- 섬 정보 보기");
            p.sendMessage(" §e/섬 랭킹 §7- 랭킹 보기");
            p.sendMessage(" §e/섬 채팅 §7- 섬 채팅 토글");
            return true;
        }
        String sub = args[0];
        if ("리로드".equalsIgnoreCase(sub) || "reload".equalsIgnoreCase(sub)){
            if (p.isOp() || p.hasPermission("samsky.admin.reload")){
                plugin.reloadConfig();
                p.sendMessage("§a컨피그를 다시 불러왔습니다.");
            } else {
                p.sendMessage("§c권한이 없습니다.");
            }
            return true;
        }

        if ("업그레이드".equalsIgnoreCase(sub) || "upgrade".equalsIgnoreCase(sub) || "upg".equalsIgnoreCase(sub)){
            plugin.getUpgradeService().open(p);
            return true;
        }
        if ("레벨".equalsIgnoreCase(sub) || "level".equalsIgnoreCase(sub) || "lvl".equalsIgnoreCase(sub)){
            plugin.getLevelService().show(p);
            return true;
        }
        if ("정보".equalsIgnoreCase(sub) || "info".equalsIgnoreCase(sub)){
            plugin.getInfoService().show(p);
            return true;
        }
        if ("랭킹".equalsIgnoreCase(sub) || "rank".equalsIgnoreCase(sub)){
            plugin.getRankingUiService().openOrRefresh(p);
            return true;
        }
        if ("채팅".equalsIgnoreCase(sub) || "chat".equalsIgnoreCase(sub)){
            plugin.getChat().toggle(p);
            return true;
        }
        p.sendMessage("§c알 수 없는 하위 명령입니다. §7/섬");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1){
            List<String> base = Arrays.asList("업그레이드","레벨","정보","랭킹","채팅","리로드");
            String p = args[0];
            List<String> out = new ArrayList<>();
            for (String s : base) if (s.startsWith(p)) out.add(s);
            return out;
        }
        return new ArrayList<>();
    }
}
