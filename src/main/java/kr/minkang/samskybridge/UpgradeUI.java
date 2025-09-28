package kr.minkang.samskybridge;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class UpgradeUI {
    private Economy economy;
    private Main main;
    private Storage storage;
    private Integration integration;

    public UpgradeUI() {}

    public UpgradeUI(Main main, Storage storage, Integration integration) {
        this.main = main;
        this.storage = storage;
        this.integration = integration;
    }

    private Economy getEconomy() {
        if (this.economy != null) return this.economy;
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null || rsp.getProvider() == null) {
            throw new IllegalStateException("[SamSkyBridge] Vault economy provider not found. Install Vault + an economy plugin.");
        }
        this.economy = rsp.getProvider();
        return this.economy;
    }

    public boolean canAfford(Player player, double cost) {
        return getEconomy().has(player, cost);
    }

    public boolean charge(Player player, double cost) {
        return getEconomy().withdrawPlayer(player, cost).transactionSuccess();
    }

    public void reward(Player player, double amount) {
        getEconomy().depositPlayer(player, amount);
    }

    public Main getMain() { return main; }
    public Storage getStorage() { return storage; }
    public Integration getIntegration() { return integration; }
}