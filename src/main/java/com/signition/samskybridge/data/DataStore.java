
package com.signition.samskybridge.data;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class DataStore {
  private final File file;
  private final FileConfiguration yml;
  public DataStore(File dataFolder){
    if (!dataFolder.exists()) dataFolder.mkdirs();
    this.file = new File(dataFolder, "data.yml");
    this.yml = YamlConfiguration.loadConfiguration(file);
  }
  public int getSize(UUID u, int def){ return yml.getInt("size."+u.toString(), def); }
  public void setSize(UUID u, int val){ yml.set("size."+u.toString(), val); save(); }
  public int getTeam(UUID u, int def){ return yml.getInt("team."+u.toString(), def); }
  public void setTeam(UUID u, int val){ yml.set("team."+u.toString(), val); save(); }
  private void save(){
    try { yml.save(file); } catch(IOException e){ e.printStackTrace(); }
  }
}
