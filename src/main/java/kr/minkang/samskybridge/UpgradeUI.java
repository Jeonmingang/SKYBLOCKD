package kr.minkang.samskybridge;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class UpgradeUI {

// Simple GUI opener to satisfy Main.java call: open(Player, IslandData)
// You can expand this to a full-featured upgrade UI later.
public void open(org.bukkit.entity.Player player, IslandData data) {
    // Build a minimal inventory so the method is functional.
    org.bukkit.inventory.Inventory inv = org.bukkit.Bukkit.createInventory(
            player, 27, org.bukkit.ChatColor.GREEN + "섬 업그레이드");

    // Example items showing island stats (size/team); replace with real upgrade buttons.
    org.bukkit.inventory.ItemStack size = new org.bukkit.inventory.ItemStack(org.bukkit.Material.GRASS_BLOCK);
    org.bukkit.inventory.meta.ItemMeta sm = size.getItemMeta();
    if (sm != null) {
        sm.setDisplayName(org.bukkit.ChatColor.YELLOW + "섬 크기: " + data.sizeRadius);
        sm.setLore(java.util.Arrays.asList(
                org.bukkit.ChatColor.GRAY + "업그레이드로 섬 크기를 확장하세요."
        ));
        size.setItemMeta(sm);
    }
    inv.setItem(11, size);

    org.bukkit.inventory.ItemStack team = new org.bukkit.inventory.ItemStack(org.bukkit.Material.PLAYER_HEAD);
    org.bukkit.inventory.meta.ItemMeta tm = team.getItemMeta();
    if (tm != null) {
        tm.setDisplayName(org.bukkit.ChatColor.AQUA + "팀 인원: " + data.teamMax);
        tm.setLore(java.util.Arrays.asList(
                org.bukkit.ChatColor.GRAY + "업그레이드로 팀 최대 인원을 늘리세요."
        ));
        team.setItemMeta(tm);
    }
    inv.setItem(15, team);

    player.openInventory(inv);
}

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