package com.signition.samskybridge.rank;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class RankingService {
    private final Main plugin;
    private final DataStore store;

    private final Map<UUID, Integer> rankCache = new HashMap<>();

    public RankingService(Main plugin, DataStore store) {
        this.plugin = plugin;
        this.store = store;
    }

    /** 외부에서 주기적으로 호출 가능 */
    public void refreshRanking() {
        rankCache.clear();
        Map<UUID, IslandData> all = fetchAllIslands();
        List<UUID> sorted = all.values().stream()
                .sorted(Comparator.<IslandData>comparingInt(IslandData::getLevel).reversed()
                        .thenComparingLong(IslandData::getXp).reversed())
                .map(IslandData::getOwner)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        int idx = 1;
        for (UUID u : sorted) rankCache.put(u, idx++);
    }

    public Integer getRank(UUID owner) {
        if (!rankCache.containsKey(owner)) refreshRanking();
        return rankCache.getOrDefault(owner, -1);
    }

    public void sendTop(Player p, int count) {
        if (count <= 0) count = 10;
        refreshRanking();

        Map<UUID, IslandData> map = fetchAllIslands();
        List<IslandData> sorted = map.values().stream()
                .sorted(Comparator.<IslandData>comparingInt(IslandData::getLevel).reversed()
                        .thenComparingLong(IslandData::getXp).reversed())
                .limit(count)
                .collect(Collectors.toList());

        p.sendMessage(Text.color(plugin.getConfig().getString("ranking.header", "&b섬 랭킹 Top " + count)));

        int rank = 1;
        for (IslandData is : sorted) {
            String leader = nameOf(is.getOwner());
            String members = memberNames(is);
            int sizeUp = readInt(is, 0, "sizeUpgrades", "sizeUpgradeCount", "sizeLevel");
            int memberUp = readInt(is, 0, "memberUpgrades", "memberUpgradeCount", "memberLevel");
            String fmt = plugin.getConfig().getString(
                    "ranking.line",
                    "&7[ 섬 랭킹 <rank>위 ] &b<leader> &7(Lv.<level>) &8| &7업글: 크기 <size_up> / 인원 <member_up> &8| &7인원: <members>"
            );
            String line = Text.color(fmt
                    .replace("<rank>", String.valueOf(rank++))
                    .replace("<leader>", leader)
                    .replace("<level>", String.valueOf(is.getLevel()))
                    .replace("<size_up>", String.valueOf(sizeUp))
                    .replace("<member_up>", String.valueOf(memberUp))
                    .replace("<members>", members));
            p.sendMessage(line);
        }

        p.sendMessage(Text.color(plugin.getConfig().getString("ranking.footer", "&8------------------------------------")));
    }

    /* ===== internals ===== */

    private Map<UUID, IslandData> fetchAllIslands() {
        // 우선 DataStore API 시도
        try {
            Method m = DataStore.class.getMethod("getAllIslands");
            Object r = m.invoke(store);
            if (r instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<UUID, IslandData> map = (Map<UUID, IslandData>) r;
                return map;
            }
        } catch (Throwable ignore) {}
        // 폴백: private 필드 접근
        try {
            Field f = DataStore.class.getDeclaredField("islandMap");
            f.setAccessible(true);
            Object obj = f.get(store);
            if (obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<UUID, IslandData> map = (Map<UUID, IslandData>) obj;
                return map;
            }
        } catch (Throwable ignore) {}
        return new HashMap<>();
    }

    private String nameOf(UUID uuid) {
        if (uuid == null) return "알수없음";
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        return op != null && op.getName() != null ? op.getName() : uuid.toString().substring(0, 8);
    }

    @SuppressWarnings("unchecked")
    private Set<UUID> membersOf(IslandData is) {
        try {
            try { return (Set<UUID>) IslandData.class.getMethod("getMembers").invoke(is); }
            catch (NoSuchMethodException ignore) {}
            Field f = IslandData.class.getDeclaredField("members");
            f.setAccessible(true);
            Object v = f.get(is);
            if (v instanceof Set) return (Set<UUID>) v;
            if (v instanceof Collection) return new HashSet<>((Collection<UUID>) v);
        } catch (Throwable ignored) {}
        return Collections.emptySet();
    }

    private String memberNames(IslandData is) {
        Set<UUID> members = membersOf(is);
        if (members.isEmpty()) return "없음";
        return members.stream().map(this::nameOf).collect(Collectors.joining(", "));
    }

    private int readInt(IslandData is, int def, String... fields) {
        for (String fn : fields) {
            try {
                Method m = IslandData.class.getMethod("get" + up(fn));
                Object v = m.invoke(is);
                if (v instanceof Number) return ((Number) v).intValue();
            } catch (Throwable ignore) {
                try {
                    Field f = IslandData.class.getDeclaredField(fn);
                    f.setAccessible(true);
                    Object v = f.get(is);
                    if (v instanceof Number) return ((Number) v).intValue();
                } catch (Throwable ignored) {}
            }
        }
        return def;
    }
    private String up(String s) { return s.length()==0? s : Character.toUpperCase(s.charAt(0))+s.substring(1); }
}
