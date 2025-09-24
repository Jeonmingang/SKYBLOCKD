package com.signition.samskybridge.data;

import java.util.UUID;

public class IslandData {
    private final UUID id;
    private String name;
    private int level;
    private long xp;
    private int size;
    private int teamMax;

    public IslandData(UUID id, String name, int level, long xp, int size, int teamMax){
        this.id = id;
        this.name = name;
        this.level = level;
        this.xp = xp;
        this.size = size;
        this.teamMax = teamMax;
    }

    public UUID getId(){ return id; }
    public String getName(){ return name; }
    public int getLevel(){ return level; }
    public long getXp(){ return xp; }
    public int getSize(){ return size; }
    public int getTeamMax(){ return teamMax; }

    // ---- Display helpers (compat) ----
    public java.util.UUID getOwner() { return this.id; } // owner=uuid convention
    public String getOwnerName(){
        java.util.UUID owner = getOwner();
        org.bukkit.OfflinePlayer op = org.bukkit.Bukkit.getOfflinePlayer(owner);
        return (op != null && op.getName()!=null) ? op.getName() : (this.name != null ? this.name : "unknown");
    }
    public java.util.List<String> getMemberNames(){
        // data model doesn't track members here; return empty for compatibility
        return java.util.Collections.emptyList();
    }
}
