package org.example.zandar.petsRevive;

import org.bukkit.plugin.java.JavaPlugin;
import org.example.zandar.petsRevive.data.DeadPetData;
import org.example.zandar.petsRevive.data.DeadPetsManager;
import org.example.zandar.petsRevive.listeners.PetsListener;

import java.util.Map;
import java.util.UUID;

public final class PetsRevive extends JavaPlugin {
    private DeadPetsManager deadPetsManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        deadPetsManager = new DeadPetsManager(this);
        getServer().getPluginManager().registerEvents(new PetsListener(this), this);
        loadDeadPetsOnStartup();
    }

    public DeadPetsManager getDeadPetsManager() {
        return deadPetsManager;
    }

    private void loadDeadPetsOnStartup() {
        PetsListener petsListener = new PetsListener(this);
        Map<UUID, DeadPetData> loadedPets = deadPetsManager.loadDeadPets();
        for (Map.Entry<UUID, DeadPetData> entry : loadedPets.entrySet()) {
            UUID petUuid = entry.getKey();
            DeadPetData data = entry.getValue();
            petsListener.restoreDeadPet(petUuid, data);
        }
    }
}
