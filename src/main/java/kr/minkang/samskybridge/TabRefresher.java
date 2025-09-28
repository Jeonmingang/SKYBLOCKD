
package kr.minkang.samskybridge;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Comparator;
import java.util.List;

public class TabRefresher {

    private final Main plugin;
    private final Storage storage;
    private BukkitTask task;

    public TabRefresher(Main plugin, Storage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public void start() {
        if (!plugin.getConfig().getBoolean("tab_prefix.force", true)) return;
        long ticks = plugin.getConfig().getLong("tab_prefix.refresh_ticks", 4L);
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                refresh(p);
            }
        }, ticks, ticks);
    }

    public void stop() {
        if (task != null) task.cancel();
    }

    private void refresh(Player p) {
        IslandData myIsland = storage.getIslandByPlayer(p.getUniqueId());
        String prefix;
        if (myIsland == null) {
            prefix = plugin.getConfig().getString("ranking.tab-prefix")
                .replace("<rank>", plugin.getConfig().getString("ranking.unranked-label"))
                .replace("<level>", "0");
        } else if (myIsland.owner.equals(p.getUniqueId())) {
            int rank = computeRank(myIsland);
            prefix = plugin.getConfig().getString("tab_prefix.owner")
                    .replace("<rank>", String.valueOf(rank))
                    .replace("<level>", String.valueOf(myIsland.level))
                    .replace("<size>", String.valueOf(myIsland.sizeRadius))
                    .replace("<team>", String.valueOf(myIsland.teamMax));
        } else {
            int rank = computeRank(storage.getIslandByOwner(myIsland.owner));
            prefix = plugin.getConfig().getString("tab_prefix.member")
                    .replace("<rank>", String.valueOf(rank))
                    .replace("<level>", String.valueOf(myIsland.level))
                    .replace("<size>", String.valueOf(myIsland.sizeRadius))
                    .replace("<team>", String.valueOf(myIsland.teamMax));
        }
        String listName = org.bukkit.ChatColor.translateAlternateColorCodes('&', prefix) + p.getName();
        try {
            p.setPlayerListName(listName);
        } catch (Throwable t) {
            // Fallback: ignore length errors
        }
    }

    private int computeRank(IslandData d) {
        List<IslandData> all = storage.getAllIslands();
        all.sort(Comparator.comparingInt(IslandData::getLevel).reversed().thenComparingInt(IslandData::getXp).reversed());
        int i = 1;
        for (IslandData x : all) {
            if (x.owner.equals(d.owner)) return i;
            i++;
        }
        return i;
    }
}
