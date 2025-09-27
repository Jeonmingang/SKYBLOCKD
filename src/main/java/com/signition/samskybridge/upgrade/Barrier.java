package com.signition.samskybridge.upgrade;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Barrier {
    private final org.bukkit.plugin.Plugin plugin;
    public Barrier(org.bukkit.plugin.Plugin plugin){ this.plugin = plugin; }

    public void show(Player p, int radius){
        if (!plugin.getConfig().getBoolean("barrier.enabled", true)) return;
        int seconds = plugin.getConfig().getInt("barrier.show-seconds", 4);
        double step = plugin.getConfig().getDouble("barrier.step", 1.5);
        World w = p.getWorld();
        Location c = p.getLocation();
        int r = Math.max(4, radius);
        int maxTicks = seconds * 20;
        Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(255,255,255), 1.2f);
        new org.bukkit.scheduler.BukkitRunnable(){
            int t=0;
            @Override public void run(){
                if (t++ > maxTicks) { cancel(); return; }
                // Draw square border at center c
                drawSquare(w, c, r, step, dust);
            }
        }.runTaskTimer(plugin, 1L, 5L);
    }

    private void drawSquare(World w, Location center, int r, double step, Particle.DustOptions dust){
        double y = center.getY() + 0.2;
        for (double x = center.getX()-r; x <= center.getX()+r; x += step){
            w.spawnParticle(Particle.REDSTONE, new Location(w, x, y, center.getZ()-r), 1, dust);
            w.spawnParticle(Particle.REDSTONE, new Location(w, x, y, center.getZ()+r), 1, dust);
        }
        for (double z = center.getZ()-r; z <= center.getZ()+r; z += step){
            w.spawnParticle(Particle.REDSTONE, new Location(w, center.getX()-r, y, z), 1, dust);
            w.spawnParticle(Particle.REDSTONE, new Location(w, center.getX()+r, y, z), 1, dust);
        }
    }
}
