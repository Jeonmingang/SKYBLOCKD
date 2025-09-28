
package kr.minkang.samskybridge;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Minimal, drop-in fix for "cannot find symbol: variable econ".
 * 
 * - Removes the raw/global "econ" reference.
 * - Resolves Vault Economy at runtime via ServicesManager.
 * - Call getEconomy() wherever you previously used "econ".
 *
 * Works on Java 8, Spigot/Paper 1.16.5 with Vault installed.
 */
public class UpgradeUI {

    private Economy economy; // resolved lazily

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

    // ===== Example usage patterns =====
    // Replace previous `econ.withdrawPlayer(player, cost)` with:
    // getEconomy().withdrawPlayer(player, cost);
    // And `econ.depositPlayer(player, amount)` with:
    // getEconomy().depositPlayer(player, amount);

    // Below are stub examples; keep your existing logic/methods and just swap `econ` -> `getEconomy()`.

    public boolean canAfford(Player player, double cost) {
        return getEconomy().has(player, cost);
    }

    public boolean charge(Player player, double cost) {
        return getEconomy().withdrawPlayer(player, cost).transactionSuccess();
    }

    public void reward(Player player, double amount) {
        getEconomy().depositPlayer(player, amount);
    }

    // ... keep the rest of your UI / inventory code as-is, just replace usages of `econ`.
}
