
package com.signition.samskybridge.data;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
public class IslandData {
  private final UUID id;
  private String name;
  private UUID owner;
  private final Set<UUID> coOwners = new HashSet<>();
  private final Set<UUID> members = new HashSet<>();
  private final Set<UUID> workers = new HashSet<>();
  private int level;
  private long xp;
  private int sizeLevel;
  private int teamLevel;
  private final Set<String> xpOnce = ConcurrentHashMap.newKeySet();
  private boolean forSale;
  private double price;
  public IslandData(UUID owner){ this.id = owner; this.owner = owner; this.name = owner.toString(); }
  public UUID getId(){ return id; }
  public UUID getOwner(){ return owner; }
  public void setOwner(UUID u){ this.owner = u; }
  public String getName(){ return name; }
  public void setName(String s){ this.name = s; }
  public Set<UUID> getCoOwners(){ return coOwners; }
  public Set<UUID> getMembers(){ return members; }
  public Set<UUID> getWorkers(){ return workers; }
  public boolean isWorker(UUID u){ return workers.contains(u); }
  public void toggleWorker(UUID u){ if (!workers.add(u)) workers.remove(u); }
  public boolean isMember(UUID u){ return u.equals(owner) || coOwners.contains(u) || members.contains(u); }
  public int getLevel(){ return level; }
  public void setLevel(int lv){ this.level = lv; }
  public long getXp(){ return xp; }
  public void setXp(long v){ this.xp = v; }
  public void addXp(long add){ this.xp += add; }
  public int getSizeLevel(){ return sizeLevel; }
  public void setSizeLevel(int lv){ this.sizeLevel = lv; }
  public int getTeamLevel(){ return teamLevel; }
  public void setTeamLevel(int lv){ this.teamLevel = lv; }
  public boolean hasXpOnce(String key){ return xpOnce.contains(key); }
  public void markXpOnce(String key){ xpOnce.add(key); }
  public Set<String> getXpOnce(){ return xpOnce; }
  public boolean isForSale(){ return forSale; }
  public void setForSale(boolean b){ this.forSale = b; }
  public double getPrice(){ return price; }
  public void setPrice(double p){ this.price = p; }
}
