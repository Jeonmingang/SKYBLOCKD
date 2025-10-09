
package com.signition.samskybridge.level;
import org.bukkit.configuration.Configuration;
public class LevelService {
  private final Configuration cfg;
  public LevelService(Configuration cfg){ this.cfg = cfg; }
  public int requiredXpFor(int level){
    int base = cfg.getInt("level.base-xp", 500);
    int per = cfg.getInt("level.per-level", 250);
    double growth = cfg.getDouble("level.growth", 1.0);
    double req = base + (level-1) * per;
    return (int)Math.round(req * Math.pow(growth, Math.max(0, level-1)));
  }
}
