package com.signition.samskybridge.util;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class VaultHook {
    private final JavaPlugin plugin;
    private Economy economy;

    public VaultHook(JavaPlugin plugin){
        this.plugin = plugin;
        if (plugin.getConfig().getBoolean("economy.enabled", true)) {
            if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
                RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
                if (rsp != null) {
                    economy = rsp.getProvider();
                    plugin.getLogger().info("Vault economy hooked: " + economy.getName());
                } else {
                    plugin.getLogger().warning("Vault not found or economy provider missing. Economy costs will be ignored.");
                }
            } else {
                plugin.getLogger().warning("Vault plugin not present. Economy costs will be ignored.");
            }
        }
    }

    public boolean withdraw(String playerName, double amount){
        if (economy == null) return true; // treat as free if no economy
        return economy.withdrawPlayer(playerName, amount).transactionSuccess();
    }
}