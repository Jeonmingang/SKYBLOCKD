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

import java.util.UUID;

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
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (!(sender instanceof Player)){
            sender.sendMessage("Only players.");
            return true;
        }
        Player p = (Player) sender;
        if (args.length == 0){
            p.sendMessage(Text.color("&a/섬 레벨 &7- 섬 레벨/경험치 보기"));
            p.sendMessage(Text.color("&a/섬 업그레이드 &7- 섬 크기/인원 업그레이드"));
            return true;
        }
        String sub = args[0];
        if (sub.equalsIgnoreCase("레벨")){
            showLevelInfo(p);
            return true;
        }
        if (sub.equalsIgnoreCase("업그레이드")){
            upgrade.open(p);
            return true;
        }
        return false;
    }

    private void showLevelInfo(Player p){
        UUID owner = p.getUniqueId();
        int lv = level.getLevel(owner);
        long cur = level.getCurrentXp(owner);
        long need = level.requiredXpForLevel(lv + 1);
        double pct = need <= 0 ? 1.0 : Math.min(1.0, (double)cur / (double)need);

        int bars = 20;
        int filled = (int)Math.round(pct * bars);
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < bars; i++) bar.append(i < filled ? "§a▮" : "§7▮");

        IslandData is = level.getIslandOf(p);
        int size = is != null ? is.getSize() : 0;
        int cap  = is != null ? is.getTeamMax() : 0;

        p.sendMessage(Text.color("&b[섬 레벨] &fLv.&e" + lv));
        p.sendMessage(Text.color("&7경험치: &f" + String.format("%,d", cur) + " &7/ &f" + String.format("%,d", need) + " &8(" + (int)(pct*100) + "%)"));
        p.sendMessage(bar.toString());
        p.sendMessage(Text.color("&7섬 크기: &f" + size + " 블럭  &8|  &7인원수: &f" + cap + " 명"));
    }
}
