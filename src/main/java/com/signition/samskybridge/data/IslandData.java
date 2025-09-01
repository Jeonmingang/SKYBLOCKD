
package com.signition.samskybridge.data;
import java.util.*; import java.util.concurrent.ConcurrentHashMap;
public class IslandData {
  private final java.util.UUID id;
  private String name; private java.util.UUID owner;
  private final java.util.Set<java.util.UUID> coOwners = new java.util.HashSet<java.util.UUID>();
  private final java.util.Set<java.util.UUID> members = new java.util.HashSet<java.util.UUID>();
  private int level; private long xp; private int sizeLevel; private int teamLevel;
  private final java.util.Set<String> xpOnce = ConcurrentHashMap.newKeySet();
  private boolean forSale; private double price;
  public IslandData(java.util.UUID owner){ this.id=owner; this.owner=owner; this.name=owner.toString(); }
  public java.util.UUID getId(){ return id; } public java.util.UUID getOwner(){ return owner; } public void setOwner(java.util.UUID u){ owner=u; }
  public String getName(){ return name; } public void setName(String s){ name=s; }
  public java.util.Set<java.util.UUID> getCoOwners(){ return coOwners; } public java.util.Set<java.util.UUID> getMembers(){ return members; }
  public int getLevel(){ return level; } public void setLevel(int lv){ level=lv; }
  public long getXp(){ return xp; } public void setXp(long v){ xp=v; } public void addXp(long add){ xp+=add; }
  public int getSizeLevel(){ return sizeLevel; } public void setSizeLevel(int lv){ sizeLevel=lv; }
  public int getTeamLevel(){ return teamLevel; } public void setTeamLevel(int lv){ teamLevel=lv; }
  public boolean hasXpOnce(String k){ return xpOnce.contains(k); } public void markXpOnce(String k){ xpOnce.add(k); } public java.util.Set<String> getXpOnce(){ return xpOnce; }
  public boolean isForSale(){ return forSale; } public void setForSale(boolean b){ forSale=b; }
  public double getPrice(){ return price; } public void setPrice(double p){ price=p; }
}
