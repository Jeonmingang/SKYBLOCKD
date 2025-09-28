
package kr.minkang.samskybridge;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * UpgradeUI - fixed to remove raw `econ` variable and support both constructors:
 *  - UpgradeUI()
 *  - UpgradeUI(Main main, Storage storage, Integration integration)
 *
 * Uses Vault to resolve Economy safely at runtime.
 * Works on Java 8 / 1.16.5 with Vault installed.
 */
public class UpgradeUI {

    private Economy economy; // resolved lazily

    // Optional references if the rest of your code expects them
    private Main main;
    private Storage storage;
    private Integration integration;

    /** No-arg constructor (default) */
    public UpgradeUI() {
        // leave fields null unless needed by your UI logic
    }

    /** 3-arg constructor to match existing instantiation in Main.java */
    public UpgradeUI(Main main, Storage storage, Integration integration) {
        this.main = main;
        this.storage = storage;
        this.integration = integration;
    }

    /** Resolve economy via Vault. Throws a clear IllegalStateException if not found. */
    private Economy getEconomy() {
        if (this.economy != null) return this.economy;

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer()
                .getServicesManager()
                .getRegistration(Economy.class);

        if (rsp == null || rsp.getProvider() == null) {
            throw new IllegalStateException("[SamSkyBridge] Vault economy provider not found. " +
                    "Make sure Vault and an economy plugin (e.g., EssentialsX Economy) are installed.");
        }
        this.economy = rsp.getProvider();
        return this.economy;
    }

    // ==== Example APIs used by the rest of the code ====
    public boolean canAfford(Player player, double cost) {
        return getEconomy().has(player, cost);
    }

    public boolean charge(Player player, double cost) {
        return getEconomy().withdrawPlayer(player, cost).transactionSuccess();
    }

    public void reward(Player player, double amount) {
        getEconomy().depositPlayer(player, amount);
    }

    // Getters for optional references if other code needs them
    public Main getMain() { return main; }
    public Storage getStorage() { return storage; }
    public Integration getIntegration() { return integration; }
}
