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
    public void setName(String s){ this.name = s; }

    public int getLevel(){ return level; }
    public void setLevel(int v){ this.level = v; }

    public long getXp(){ return xp; }
    public void setXp(long v){ this.xp = v; }

    public int getSize(){ return size; }
    public void setSize(int v){ this.size = v; }

    public int getTeamMax(){ return teamMax; }
    public void setTeamMax(int v){ this.teamMax = v; }

    public java.util.UUID getOwner(){ return id; }
    public String getOwnerName(){ return name; }
}
