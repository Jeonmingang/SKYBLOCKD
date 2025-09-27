package com.signition.samskybridge.rank;

import com.signition.samskybridge.data.DataStore;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class RankingService {
    private final Plugin plugin;
    private final DataStore store;
    private final int pageSize;

    public RankingService(Plugin plugin, DataStore store){
        this.plugin = plugin;
        this.store = store;
        this.pageSize = plugin.getConfig().getInt("ranking.page-size", 10);
    }

    public int pageSize(){ return pageSize; }

    public List<UUID> top(int n){
        List<UUID> ids = new ArrayList<UUID>(store.listPlayers());
        Collections.sort(ids, new Comparator<UUID>(){
            @Override public int compare(UUID a, UUID b){
                long ax = store.getXP(a), bx = store.getXP(b);
                return Long.compare(bx, ax);
            }
        });
        if (n < 0 || n > ids.size()) n = ids.size();
        return ids.subList(0, n);
    }

    public int rankOf(UUID id){
        List<UUID> ids = new ArrayList<UUID>(store.listPlayers());
        Collections.sort(ids, new Comparator<UUID>(){
            @Override public int compare(UUID a, UUID b){
                long ax = store.getXP(a), bx = store.getXP(b);
                return Long.compare(bx, ax);
            }
        });
        int pos = 1;
        for (UUID x : ids){
            if (x.equals(id)) return pos;
            pos++;
        }
        return -1;
    }

    public List<UUID> page(int page){
        if (page < 1) page = 1;
        List<UUID> ids = new ArrayList<UUID>(store.listPlayers());
        Collections.sort(ids, new Comparator<UUID>(){
            @Override public int compare(UUID a, UUID b){
                long ax = store.getXP(a), bx = store.getXP(b);
                return Long.compare(bx, ax);
            }
        });
        int from = (page - 1) * pageSize;
        if (from >= ids.size()) return Collections.<UUID>emptyList();
        int to = Math.min(ids.size(), from + pageSize);
        return ids.subList(from, to);
    }

    public int totalPages(){
        int sz = store.listPlayers().size();
        return Math.max(1, (int)Math.ceil(sz / (double) pageSize));
    }
}
