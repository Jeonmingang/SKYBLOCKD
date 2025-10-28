
package com.minkang.ultimate.rps.gui;

import com.minkang.ultimate.rps.UltimateRpsPlugin;
import com.minkang.ultimate.rps.game.RpsChoice;
import com.minkang.ultimate.rps.station.Station;
import com.minkang.ultimate.rps.util.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class GuiListener implements Listener {

    private final UltimateRpsPlugin plugin;
    private final String P;

    public GuiListener(UltimateRpsPlugin plugin) {
        this.plugin = plugin;
        this.P = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix", "&a[ 가위바위보 ]&f "));
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Inventory inv = e.getInventory();
        InventoryHolder holder = inv.getHolder();

        if (holder instanceof RpsGui) {
            RpsGui gui = (RpsGui) holder;
            Player p = (Player) e.getWhoClicked();
            int raw = e.getRawSlot();
            int topSize = inv.getSize();

            if (raw < topSize) {
                e.setCancelled(true);
                int slot = raw;

                // 카운트다운 잠금: 상단 GUI 차단
                if (gui.getSession().locked) {
                    p.sendMessage(color("&7카운트다운 중입니다. 잠시만 기다려주세요!"));
                    return;
                }

                // 숫자키/더블클릭 차단
                if (e.getClick() == ClickType.NUMBER_KEY || e.getClick() == ClickType.DOUBLE_CLICK) { return; }

                // 상단 허용 슬롯만 처리
                if (slot != RpsGui.SLOT_COIN && slot != RpsGui.SLOT_ROCK && slot != RpsGui.SLOT_PAPER
                        && slot != RpsGui.SLOT_SCISSORS && slot != RpsGui.SLOT_START) {
                    return;
                }

                // 코인칸
                if (slot == RpsGui.SLOT_COIN) {
                    Station st = gui.getSession().station;
                    ItemStack cursor = e.getCursor();
                    ItemStack slotItem = inv.getItem(RpsGui.SLOT_COIN);
                    if (st.getCoinItem() == null) { p.sendMessage(color(plugin.getConfig().getString("messages.coin-not-set"))); return; }
                    if (cursor == null || cursor.getType() == Material.AIR) {
                        if (slotItem != null && slotItem.getType() != Material.AIR) {
                            e.getView().setCursor(slotItem);
                            inv.setItem(RpsGui.SLOT_COIN, new ItemStack(Material.AIR));
                        }
                        return;
                    }
                    if (!ItemUtils.isSimilarIgnoreAmount(cursor, st.getCoinItem())) { p.sendMessage(color(plugin.getConfig().getString("messages.coin-wrong"))); return; }
                    int current = (slotItem == null || slotItem.getType() == Material.AIR) ? 0 : slotItem.getAmount();
                    int can = Math.max(0, 3 - current);
                    if (can <= 0) { p.sendMessage(color(plugin.getConfig().getString("messages.coin-too-much"))); return; }
                    int move = Math.min(can, cursor.getAmount());
                    if (current == 0) { ItemStack put = cursor.clone(); put.setAmount(move); inv.setItem(RpsGui.SLOT_COIN, put); }
                    else { slotItem.setAmount(current + move); inv.setItem(RpsGui.SLOT_COIN, slotItem); }
                    int remain = cursor.getAmount() - move;
                    if (remain <= 0) e.getView().setCursor(new ItemStack(Material.AIR));
                    else { ItemStack nc = cursor.clone(); nc.setAmount(remain); e.getView().setCursor(nc); }
                    return;
                }

                // 선택
                if (slot == RpsGui.SLOT_ROCK) { gui.updatePlayerChoice(RpsChoice.ROCK); p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f,1f); return; }
                if (slot == RpsGui.SLOT_PAPER) { gui.updatePlayerChoice(RpsChoice.PAPER); p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f,1.2f); return; }
                if (slot == RpsGui.SLOT_SCISSORS) { gui.updatePlayerChoice(RpsChoice.SCISSORS); p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f,1.4f); return; }

                // 시작
                boolean isStart = (slot == RpsGui.SLOT_START);
                if (!isStart) {
                    ItemStack clicked = inv.getItem(slot);
                    if (clicked == null) clicked = e.getCurrentItem();
                    if (clicked != null && clicked.getType() == Material.LIME_DYE && clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) isStart = true;
                }
                if (isStart) {
                    ItemStack bet = inv.getItem(RpsGui.SLOT_COIN);
                    if (bet == null || bet.getType() == Material.AIR) { p.sendMessage(color(plugin.getConfig().getString("messages.need-coin"))); return; }
                    if (bet.getAmount() > 3) { p.sendMessage(color(plugin.getConfig().getString("messages.coin-too-much"))); return; }
                    if (gui.getSession().playerChoice == null) { p.sendMessage(color(plugin.getConfig().getString("messages.need-choose"))); return; }

                    int betAmount = bet.getAmount();
                    ItemStack coinItem = bet.clone(); coinItem.setAmount(1);
                    inv.setItem(RpsGui.SLOT_COIN, new ItemStack(Material.AIR));

                    startCountdown(gui, betAmount, coinItem);
                    return;
                }
                return;
            } else {
                // 하단 인벤토리: shift로 코인만 코인칸으로
                if (e.isShiftClick()) {
                    ItemStack cur = e.getCurrentItem();
                    Station st = ((RpsGui) holder).getSession().station;
                    if (cur != null && cur.getType() != Material.AIR && st.getCoinItem() != null
                            && ItemUtils.isSimilarIgnoreAmount(cur, st.getCoinItem())) {
                        ItemStack slot = inv.getItem(RpsGui.SLOT_COIN);
                        int slotAmt = (slot == null || slot.getType() == Material.AIR) ? 0 : slot.getAmount();
                        int can = Math.max(0, 3 - slotAmt);
                        if (can > 0) {
                            int move = Math.min(can, cur.getAmount());
                            if (slotAmt == 0) { ItemStack put = cur.clone(); put.setAmount(move); inv.setItem(RpsGui.SLOT_COIN, put); }
                            else { slot.setAmount(slotAmt + move); inv.setItem(RpsGui.SLOT_COIN, slot); }
                            cur.setAmount(cur.getAmount() - move);
                            e.setCurrentItem(cur.getAmount() <= 0 ? new ItemStack(Material.AIR) : cur);
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
                return;
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        Inventory inv = e.getInventory();
        InventoryHolder holder = inv.getHolder();
        if (holder instanceof RpsGui) {
            int topSize = inv.getSize();
            for (int raw : e.getRawSlots()) if (raw < topSize) { e.setCancelled(true); return; }
        }
    }

    private void startCountdown(RpsGui gui, int betAmount, ItemStack coinItem) {
        Player p = (Player) gui.getInventory().getViewers().get(0);
        gui.getSession().locked = true;
        p.sendMessage(color(plugin.getConfig().getString("messages.countdown","&73... &72... &71...")));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            gui.getInventory().setItem(RpsGui.SLOT_SYSTEM_HEAD, ItemUtils.named(Material.CLOCK, ChatColor.LIGHT_PURPLE + "3"));
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.9f);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                gui.getInventory().setItem(RpsGui.SLOT_SYSTEM_HEAD, ItemUtils.named(Material.CLOCK, ChatColor.LIGHT_PURPLE + "2"));
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.0f);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    gui.getInventory().setItem(RpsGui.SLOT_SYSTEM_HEAD, ItemUtils.named(Material.CLOCK, ChatColor.LIGHT_PURPLE + "1"));
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.1f);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        RpsChoice sys = RpsChoice.values()[ThreadLocalRandom.current().nextInt(3)];
                        gui.updateSystemChoice(sys);
                        int result = RpsChoice.compare(gui.getSession().playerChoice, sys);
                        if (result == 0) {
                            p.sendMessage(color(plugin.getConfig().getString("messages.draw","&e무승부! 다시 선택해주세요.")));
                            gui.getSession().locked = false;
                        } else if (result < 0) {
                            p.sendMessage(color(plugin.getConfig().getString("messages.lose","&c패배! 베팅한 코인은 회수되었습니다.")));
                            UltimateRpsPlugin.get().stats().addLoss(p.getUniqueId(), betAmount);
                            gui.getSession().locked = false;
                        } else {
                            // 승리: 2초 뒤 룰렛 전환
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                RouletteGui roll = new RouletteGui(p, gui.getSession().station, betAmount, coinItem);
                                p.openInventory(roll.getInventory());
                                roll.startSpin();
                            }, 40L);
                        }
                    }, 12L);
                }, 12L);
            }, 12L);
        }, 10L);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) { /* no-op */ }

    private String color(String s) { return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s); }
}
