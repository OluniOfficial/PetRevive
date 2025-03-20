package org.example.zandar.petsRevive.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.example.zandar.petsRevive.PetsRevive;
import org.example.zandar.petsRevive.enums.Timer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class DeadPetsManager {
    private final PetsRevive pr;
    private final FileConfiguration deadPetsDB;
    private final File deadPetsFile;

    public DeadPetsManager(PetsRevive pr) {
        this.pr = pr;
        deadPetsFile = new File(pr.getDataFolder(), "dead_pets.yml");
        deadPetsDB = YamlConfiguration.loadConfiguration(deadPetsFile);
    }

    public void saveDeadPet(UUID petUuid, long expTime, Timer timerType, UUID timerASU, UUID ownerASU) {
        String petUuidS = petUuid.toString();
        deadPetsDB.set("dead_pets." + petUuidS + ".exp_time", expTime);
        deadPetsDB.set("dead_pets." + petUuidS + ".timer_type", timerType.toString());
        deadPetsDB.set("dead_pets." + petUuidS + ".timer_asu", timerASU.toString());
        deadPetsDB.set("dead_pets." + petUuidS + ".owner_asu", ownerASU.toString());
        saveConfig();
    }

    public void removeDeadPet(UUID petUuid) {
        String petUuidS = petUuid.toString();
        deadPetsDB.set("dead_pets." + petUuidS, null);
        saveConfig();
    }

    public Map<UUID, DeadPetData> loadDeadPets() {
        Map<UUID, DeadPetData> loadedDeadPets = new HashMap<>();
        if (!deadPetsDB.contains("dead_pets")) {
            return loadedDeadPets;
        }
        for (String petUuidS : Objects.requireNonNull(deadPetsDB.getConfigurationSection("dead_pets")).getKeys(false)) {
            UUID petUuid = UUID.fromString(petUuidS);
            long expTime = deadPetsDB.getLong("dead_pets." + petUuidS + ".exp_time");
            String timerTypeStr = deadPetsDB.getString("dead_pets." + petUuidS + ".timer_type");
            Timer timerType = Timer.valueOf(timerTypeStr);
            String timerArmorStandUuidStr = deadPetsDB.getString("dead_pets." + petUuidS + ".timer_asu");
            UUID timerASU = UUID.fromString(Objects.requireNonNull(timerArmorStandUuidStr));
            String ownerArmorStandUuidStr = deadPetsDB.getString("dead_pets." + petUuidS + ".owner_asu");
            UUID ownerASU = UUID.fromString(Objects.requireNonNull(ownerArmorStandUuidStr));
            loadedDeadPets.put(petUuid, new DeadPetData(timerASU, ownerASU, expTime, timerType));
        }
        return loadedDeadPets;
    }

    private void saveConfig() {
        try {
            deadPetsDB.save(deadPetsFile);
        } catch (IOException e) {
            pr.getLogger().severe("Не вдалося зберегти dead_pets.yml: " + e.getMessage());
        }
    }
}