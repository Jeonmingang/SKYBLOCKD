
package com.signition.samskybridge.util;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Eco {
  private static Economy economy;
  public static Economy econ(){
    if (economy != null) return economy;
    RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
    if (rsp != null) economy = rsp.getProvider();
    return economy;
  }
  public static boolean withdraw(org.bukkit.OfflinePlayer p, double amount){
    Economy e = econ(); if (e == null) return false;
    return e.withdrawPlayer(p, amount).transactionSuccess();
  }
  public static void deposit(org.bukkit.OfflinePlayer p, double amount){
    Economy e = econ(); if (e != null) e.depositPlayer(p, amount);
  }
  public static double balance(org.bukkit.OfflinePlayer p){
    Economy e = econ(); return e==null? 0.0 : e.getBalance(p);
  }
}
