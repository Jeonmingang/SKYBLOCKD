package com.signition.samskybridge.data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class IslandData {
    private int xp = 0;
    private int level = 0;
    private int sizeLevel = 0;   // 보호반경 업그레이드 단계
    private int teamLevel = 0;   // 팀 슬롯 업그레이드 단계
    private UUID owner;          // 소유자
    private final Set<UUID> members = new HashSet<>(); // 멤버 목록 (owner 포함 가능)

    // --- getters / setters ---
    public int getXp(){ return xp; }
    public void setXp(int v){ this.xp = v; }
    public int getLevel(){ return level; }
    public void setLevel(int v){ this.level = v; }
    public int getSizeLevel(){ return sizeLevel; }
    public void setSizeLevel(int v){ this.sizeLevel = v; }
    public int getTeamLevel(){ return teamLevel; }
    public void setTeamLevel(int v){ this.teamLevel = v; }
    public UUID getOwner(){ return owner; }
    public void setOwner(UUID u){ this.owner = u; }

    public boolean hasMember(UUID uid){
        if (uid == null) return false;
        if (owner != null && owner.equals(uid)) return true;
        return members.contains(uid);
    }
    public Set<UUID> getMembers(){ return members; }
}
