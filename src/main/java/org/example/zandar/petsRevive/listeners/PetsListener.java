package org.example.zandar.petsRevive.listeners;

import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.example.zandar.petsRevive.PetsRevive;
import org.jetbrains.annotations.NotNull;

public class PetsListener implements Listener {
    private final PetsRevive plugin;

    public PetsListener(PetsRevive plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void PetsGraves(EntityDeathEvent event) {
        if (event.getEntity() instanceof Tameable pet) {
            if (pet.getOwnerUniqueId() != null) {
                event.setCancelled(true);
                pet.setAI(false);
                pet.setSilent(true);
                pet.setInvulnerable(true);
            }

        }
    }
}