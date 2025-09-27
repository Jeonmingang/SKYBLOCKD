package com.signition.samskybridge.rank;

import com.signition.samskybridge.data.DataStore;

public class RankingService {
    private final int pageSize;

    private final DataStore store;
    public RankingService(org.bukkit.plugin.Plugin plugin, DataStore store){
        this.store = store;
    
    public int rankOf(java.util.UUID id){
        java.util.List<java.util.UUID> ids = store.listPlayers();
        java.util.Collections.sort(ids, new java.util.Comparator<java.util.UUID>(){
            @Override public int compare(java.util.UUID a, java.util.UUID b){
                long ax = store.getXP(a), bx = store.getXP(b);
                return Long.compare(bx, ax);
            }
        });
        int pos = 1;
        for (java.util.UUID x : ids){
            if (x.equals(id)) return pos;
            pos++;
        }
        return -1;
    }
    
}

    public java.util.List<java.util.UUID> top(int n){
        java.util.List<java.util.UUID> ids = store.listPlayers();
        java.util.Collections.sort(ids, new java.util.Comparator<java.util.UUID>(){
            @Override public int compare(java.util.UUID a, java.util.UUID b){
                long ax = store.getXP(a), bx = store.getXP(b);
                return Long.compare(bx, ax);
            
    public int rankOf(java.util.UUID id){
        java.util.List<java.util.UUID> ids = store.listPlayers();
        java.util.Collections.sort(ids, new java.util.Comparator<java.util.UUID>(){
            @Override public int compare(java.util.UUID a, java.util.UUID b){
                long ax = store.getXP(a), bx = store.getXP(b);
                return Long.compare(bx, ax);
            }
        });
        int pos = 1;
        for (java.util.UUID x : ids){
            if (x.equals(id)) return pos;
            pos++;
        }
        return -1;
    }
    
}
        
    public int rankOf(java.util.UUID id){
        java.util.List<java.util.UUID> ids = store.listPlayers();
        java.util.Collections.sort(ids, new java.util.Comparator<java.util.UUID>(){
            @Override public int compare(java.util.UUID a, java.util.UUID b){
                long ax = store.getXP(a), bx = store.getXP(b);
                return Long.compare(bx, ax);
            }
        });
        int pos = 1;
        for (java.util.UUID x : ids){
            if (x.equals(id)) return pos;
            pos++;
        }
        return -1;
    }
    
});
        if (ids.size() > n) ids = ids.subList(0, n);
        return ids;
    
    public int rankOf(java.util.UUID id){
        java.util.List<java.util.UUID> ids = store.listPlayers();
        java.util.Collections.sort(ids, new java.util.Comparator<java.util.UUID>(){
            @Override public int compare(java.util.UUID a, java.util.UUID b){
                long ax = store.getXP(a), bx = store.getXP(b);
                return Long.compare(bx, ax);
            }
        });
        int pos = 1;
        for (java.util.UUID x : ids){
            if (x.equals(id)) return pos;
            pos++;
        }
        return -1;
    }
    
}

    public int rankOf(java.util.UUID id){
        java.util.List<java.util.UUID> ids = store.listPlayers();
        java.util.Collections.sort(ids, new java.util.Comparator<java.util.UUID>(){
            @Override public int compare(java.util.UUID a, java.util.UUID b){
                long ax = store.getXP(a), bx = store.getXP(b);
                return Long.compare(bx, ax);
            }
        });
        int pos = 1;
        for (java.util.UUID x : ids){
            if (x.equals(id)) return pos;
            pos++;
        }
        return -1;
    }
    
}
