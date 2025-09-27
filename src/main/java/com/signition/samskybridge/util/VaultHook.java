package com.signition.samskybridge.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {
    private Object economy; // net.milkbowl.vault.economy.Economy
    public VaultHook(org.bukkit.plugin.Plugin plugin){
        try {
            Class<?> econClass = Class.forName("net.milkbowl.vault.economy.Economy");
            RegisteredServiceProvider<?> rsp = Bukkit.getServicesManager().getRegistration(econClass);
            if (rsp != null) economy = rsp.getProvider();
            if (economy == null) plugin.getLogger().info("[Vault] Economy not found; treating as free.");
        } catch (Throwable t){
            plugin.getLogger().info("[Vault] Not installed; treating as free.");
            economy = null;
        }
    }
    public boolean present(){ return economy != null; }
    public double balance(Player p){
        if (economy == null) return Double.MAX_VALUE;
        try {
            java.lang.reflect.Method m = economy.getClass().getMethod("getBalance", org.bukkit.OfflinePlayer.class);
            Object res = m.invoke(economy, p);
            if (res instanceof Number) return ((Number)res).doubleValue();
        } catch (Throwable ignored){}
        return 0D;
    }
    public boolean withdraw(Player p, double amount){
        if (economy == null) return true;
        try {
            java.lang.reflect.Method m = economy.getClass().getMethod("withdrawPlayer", org.bukkit.OfflinePlayer.class, double.class);
            Object res = m.invoke(economy, p, amount);
            try {
                java.lang.reflect.Method ok = res.getClass().getMethod("transactionSuccess");
                Object success = ok.invoke(res);
                if (success instanceof Boolean) return (Boolean) success;
            } catch (Throwable ignored){}
        } catch (Throwable ignored){}
        return false;
    }
}
