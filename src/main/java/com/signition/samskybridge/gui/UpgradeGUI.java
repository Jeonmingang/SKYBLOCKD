// --- Add to your UpgradeGUI (build/open method) ---
// Assumes you have: Main plugin; UpgradeService upgrade; LevelService level;
// Inventory inv = Bukkit.createInventory(player, 54, Text.color("&b섬 업그레이드"));

int slotSize = 13;  // 잔디 블럭: 섬 크기 업그레이드
int slotMembers = 15; // 스티브 머리: 섬 인원수 업그레이드

// === 섬 크기 업그레이드 (slot 13) ===
{
    ItemStack it = new ItemStack(Material.GRASS_BLOCK);
    ItemMeta im = it.getItemMeta();
    im.setDisplayName(Text.color("&e섬 &l크기 &f업그레이드"));
    List<String> lore = new ArrayList<>();

    IslandData is = level.getIslandOf(player);
    int sizeNow = upgrade.getProtectedSize(is);          // 현재 보호반경(블럭)
    int sizeNext = upgrade.getNextSize(is);              // 다음 단계
    int needLv  = upgrade.getRequiredLevelForSize(is);   // 요구 레벨
    long price  = upgrade.getPriceForSize(is);           // 필요 금액
    
    lore.add(Text.color("&7현재 보호반경: &f" + sizeNow + " 블럭"));
    lore.add(Text.color("&7다음 단계: &a" + sizeNext + " 블럭"));
    lore.add(Text.color("&7요구 레벨: &bLv." + needLv));
    lore.add(Text.color("&7필요 금액: &d" + String.format("%,d", price)));
    lore.add(Text.color("&8업그레이드 레벨: &7" + upgrade.getSizeTier(is) + "&8/∞"));
    lore.add(Text.color("&7클릭: 업그레이드"));
    im.setLore(lore);
    it.setItemMeta(im);
    inv.setItem(slotSize, it);
}

// === 섬 인원수 업그레이드 (slot 15) ===
{
    ItemStack it = new ItemStack(Material.PLAYER_HEAD);
    SkullMeta sm = (SkullMeta) it.getItemMeta();
    sm.setOwningPlayer(Bukkit.getOfflinePlayer("MHF_Steve")); // 스티브 머리
    sm.setDisplayName(Text.color("&e섬 &l인원수 &f업그레이드"));
    List<String> lore = new ArrayList<>();

    IslandData is = level.getIslandOf(player);
    int capNow = upgrade.getMemberCap(is);
    int capNext = upgrade.getNextMemberCap(is);
    int needLv  = upgrade.getRequiredLevelForMemberCap(is);
    long price  = upgrade.getPriceForMemberCap(is);

    lore.add(Text.color("&7현재 인원수: &f" + capNow + " 명"));
    lore.add(Text.color("&7다음 단계: &a" + capNext + " 명"));
    lore.add(Text.color("&7요구 레벨: &bLv." + needLv));
    lore.add(Text.color("&7필요 금액: &d" + String.format("%,d", price)));
    lore.add(Text.color("&7클릭: 업그레이드"));
    sm.setLore(lore);
    it.setItemMeta(sm);
    inv.setItem(slotMembers, it);
}

// --- In your InventoryClick callback inside UpgradeGUI ---
// if (e.getInventory().equals(this.inv)) { ... }
if (slot == 13) {
    upgrade.tryUpgradeSize(player);
    player.closeInventory();
    Bukkit.getScheduler().runTaskLater(plugin, () -> open(player), 2L);
    e.setCancelled(true);
}
if (slot == 15) {
    upgrade.tryUpgradeMemberCap(player);
    player.closeInventory();
    Bukkit.getScheduler().runTaskLater(plugin, () -> open(player), 2L);
    e.setCancelled(true);
}
