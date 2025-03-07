package org.example.zandar.petsRevive;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.example.zandar.petsRevive.listeners.PetsListener;

public final class PetsRevive extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(new PetsListener(this), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
