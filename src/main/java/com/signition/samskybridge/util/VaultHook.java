package com.signition.samskybridge.util;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {
    private Economy eco;

    public void setup(){
        try {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) eco = rsp.getProvider();
        } catch (Throwable ignored) {}
    }

    public boolean has(String playerName, double amount){
        if (eco == null) return false;
        try { return eco.has(playerName, amount); } catch (Throwable t){ return false; }
    }

    public boolean withdraw(String playerName, double amount){
        if (eco == null) return false;
        try { return eco.withdrawPlayer(playerName, amount).transactionSuccess(); } catch (Throwable t){ return false; }
    }
}
