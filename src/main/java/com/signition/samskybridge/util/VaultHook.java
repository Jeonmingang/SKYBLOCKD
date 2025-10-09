
package com.signition.samskybridge.util;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class VaultHook {
  private static Economy econ;
  private VaultHook(){}
  public static Economy economy(){
    if (econ != null) return econ;
    if (Bukkit.getServer().getPluginManager().getPlugin("Vault")==null) return null;
    RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
    if (rsp != null) econ = rsp.getProvider();
    return econ;
  }
}
