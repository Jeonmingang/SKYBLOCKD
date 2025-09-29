
package kr.minkang.samskybridge;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Periodically refreshes the player's tab prefix to show island info.
 * Supports both <rank> and <rankDisplay>. When the island is unranked,
 * <rank> and <rankDisplay> will both be replaced with the configured
 * unranked-label AND any trailing "위" will be removed automatically.
 */
public class TabRefresher {

    private final Main plugin;
    private final Storage storage;
    private BukkitTask task;

    public TabRefresher(Main plugin, Storage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public void start() {
        stop();
        int ticks = Math.max(2, plugin.getConfig().getInt("tab_prefix.refresh_ticks", 20));
        boolean force = plugin.getConfig().getBoolean("tab_prefix.force", true);
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> tick(force), 40L, ticks);
    }

    public void stop() {
        if (task != null) try { task.cancel(); } catch (Throwable ignored) {}
        task = null;
    }

    private void tick(boolean force) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            IslandData data = storage.getIslandByPlayer(p.getUniqueId());
            if (data == null) data = BentoBridge.resolveFromBento(plugin, p); // import once if possible
            String prefix = buildPrefix(p, data);
            if (force || prefix != null) {
                try {
                    p.setPlayerListName(color(prefix + ChatColor.RESET + p.getName()));
                } catch (Throwable ignored) {
                    // some skins/plugins may cap/set display name length; ignore safely
                }
            }
        }
    }

    private String buildPrefix(Player p, IslandData d) {
        String ownerFmt = plugin.getConfig().getString("tab_prefix.owner",
                "&7[ &a섬 랭킹 &f<rankDisplay> &7| &blv.<level> &7| 크기 <size> &7| 인원 <team> &7] &r");
        String memberFmt = plugin.getConfig().getString("tab_prefix.member",
                "&7[ &a섬원 섬장의섬랭크 &f<rankDisplay> &7| 섬장의 &blv.<level> &7| 크기 <size> &7| 인원 <team> &7] &r");

        if (d == null) {
            // unranked, no island known
            String un = plugin.getConfig().getString("ranking.unranked-label", "등록안됨");
            String fmt = ownerFmt; // default shape
            String out = fmt.replace("<rankDisplay>", un).replace("<rank>", un);
            out = stripRedundantUiSuffix(out, un);
            out = out.replace("<level>", "0").replace("<size>", "0").replace("<team>", "0");
            return out;
        }

        boolean isOwner = d.owner != null && d.owner.equals(p.getUniqueId());
        String fmt = isOwner ? ownerFmt : memberFmt;

        int rank = computeRank(d);
        String un = plugin.getConfig().getString("ranking.unranked-label", "등록안됨");
        String rankDisplay = (rank <= 0 || rank >= Integer.MAX_VALUE) ? un : (rank + "위");

        String out = fmt
                .replace("<rankDisplay>", rankDisplay)
                .replace("<rank>", (rank <= 0 || rank >= Integer.MAX_VALUE) ? un : String.valueOf(rank))
                .replace("<level>", String.valueOf(d.level))
                .replace("<size>", String.valueOf(d.sizeRadius))
                .replace("<team>", String.valueOf(d.teamMax));
        if (rank <= 0 || rank >= Integer.MAX_VALUE) {
            out = stripRedundantUiSuffix(out, un);
        }
        return out;
    }

    private String stripRedundantUiSuffix(String text, String unrankedLabel) {
        // fixes templates like "<rank>위" when <rank> is replaced by "등록안됨"
        return text.replace(unrankedLabel + "위", unrankedLabel);
    }

    private int computeRank(IslandData d) {
        List<IslandData> all = new ArrayList<>(storage.getAllIslands());
        // highest level first, then xp desc
        all.sort(Comparator.comparingInt((IslandData x) -> x.level).reversed()
                .thenComparingInt((IslandData x) -> x.xp).reversed());
        int i = 1;
        for (IslandData x : all) {
            if (x.owner != null && x.owner.equals(d.owner)) return i;
            i++;
        }
        return Integer.MAX_VALUE; // unranked
    }

    private String color(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
