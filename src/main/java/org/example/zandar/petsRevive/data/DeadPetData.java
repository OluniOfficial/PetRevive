package org.example.zandar.petsRevive.data;

import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.example.zandar.petsRevive.utils.TimeUtils;

import java.util.UUID;

public class DeadPetData {
    private final UUID entityID, armorstandID;
    private final long experantionTime;
    public DeadPetData(Entity pet, long experantionTime){
        entityID = pet.getUniqueId();
        armorstandID = pet.getWorld().spawn(pet.getLocation().add(0, 1, 0), ArmorStand.class).getUniqueId();
        this.experantionTime = experantionTime;
    }
    public void update(){
        long timeDifference = experantionTime - TimeUtils.getCurrentSecond();
        Bukkit.getEntity(armorstandID).customName();
        Bukkit.getEntity(armorstandID).setCustomNameVisible(true);
    }
}
