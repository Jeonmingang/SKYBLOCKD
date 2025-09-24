package com.signition.samskybridge.rank;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import java.util.*;

public class RankingService {
    private final Main plugin;
    private final DataStore store;

    public RankingService(Main plugin, DataStore store){
        this.plugin = plugin;
        this.store = store;
    }

    public List<IslandData> getSortedIslands(){
        List<IslandData> out = new ArrayList<IslandData>(this.store.all());
        Collections.sort(out, new Comparator<IslandData>(){
            public int compare(IslandData a, IslandData b){
                int c = Integer.compare(b.getLevel(), a.getLevel());
                if (c != 0) return c;
                return Long.compare(b.getXp(), a.getXp());
            }
        });
        return out;
    }
}
