
package kr.minkang.samskybridge;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class IslandData {
    public final UUID owner;
    public int level = 1;
    public int xp = 0;
    public int sizeRadius = 50;
    public int teamMax = 2;
    public final Set<UUID> members = new HashSet<>();

    public IslandData(UUID owner) { this.owner = owner; }

    public int getLevel() { return level; }
    public int getXp() { return xp; }
}
