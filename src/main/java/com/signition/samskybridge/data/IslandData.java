
package com.signition.samskybridge.data;
import java.util.*;

public class IslandData {
  private final UUID id;
  private UUID owner;
  private String name;
  private final Set<UUID> coOwners = new HashSet<>();
  private final Set<UUID> members = new HashSet<>();
  private int level = 0;
  private long xp = 0;
  private int sizeLevel = 0;
  private int teamLevel = 0;

  public IslandData(UUID owner){
    this.id = owner; this.owner = owner; this.name = owner.toString();
    members.add(owner);
  }
  public UUID getId(){ return id; }
  public UUID getOwner(){ return owner; }
  public void setOwner(UUID u){ owner = u; members.add(u); }
  public String getName(){ return name; }
  public void setName(String n){ name = n; }
  public Set<UUID> getCoOwners(){ return coOwners; }
  public Set<UUID> getMembers(){ return members; }
  public boolean hasMember(UUID u){ return u!=null && (u.equals(owner) || coOwners.contains(u) || members.contains(u)); }
  public int getLevel(){ return level; }
  public void setLevel(int v){ level = v; }
  public long getXp(){ return xp; }
  public void setXp(long v){ xp = v; }
  public int getSizeLevel(){ return sizeLevel; }
  public void setSizeLevel(int v){ sizeLevel = v; }
  public int getTeamLevel(){ return teamLevel; }
  public void setTeamLevel(int v){ teamLevel = v; }
}
