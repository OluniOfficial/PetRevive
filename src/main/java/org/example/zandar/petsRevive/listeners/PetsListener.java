package org.example.zandar.petsRevive.listeners;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.example.zandar.petsRevive.PetsRevive;
import org.example.zandar.petsRevive.data.DeadPetData;
import org.example.zandar.petsRevive.enums.Timer;
import org.example.zandar.petsRevive.utils.TimeUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class PetsListener implements Listener {
    private final PetsRevive plugin;
    private final Map<UUID, DeadPetData> deadPets = new HashMap<>();

    public PetsListener(PetsRevive plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void PetsGraves(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Tameable pet) || pet.getOwner() == null) return;
        event.setCancelled(true);
        pet.setAI(false);
        pet.setSilent(true);
        pet.setInvulnerable(true);
        int redD = getDeathTime((Player) pet.getOwner());
        long expirationTime = TimeUtils.getCurrentSecond() + redD;
        ArmorStand timeStand = pet.getWorld().spawn(pet.getLocation().add(0, 1, 0), ArmorStand.class);
        timeStand.setMarker(true);
        timeStand.setVisible(false);
        timeStand.setCustomNameVisible(true);
        ArmorStand ownerStand = pet.getWorld().spawn(pet.getLocation().add(0, 0.75, 0), ArmorStand.class);
        ownerStand.setMarker(true);
        ownerStand.setVisible(false);
        ownerStand.setCustomNameVisible(true);
        DeadPetData data = new DeadPetData(timeStand.getUniqueId(), ownerStand.getUniqueId(), expirationTime, Timer.RED);
        deadPets.put(pet.getUniqueId(), data);
        plugin.getDeadPetsManager().saveDeadPet(pet.getUniqueId(), expirationTime, Timer.RED,
                timeStand.getUniqueId(), ownerStand.getUniqueId());
        startTimer(pet.getUniqueId(), pet, timeStand, ownerStand, data);
    }

    @EventHandler
    public void PlayerPetGraveInteract(PlayerInteractAtEntityEvent e) {
        if (!(e.getRightClicked() instanceof Tameable pet) || pet.getOwner() == null) return;
        if (!deadPets.containsKey(pet.getUniqueId())) return;
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        if (item.getType() != Material.TOTEM_OF_UNDYING) return;
        DeadPetData data = deadPets.get(pet.getUniqueId());
        if (data.getTimerType() != Timer.RED) return;
        int greenD = getResurrectionTime((Player) pet.getOwner());
        long newExpiration = TimeUtils.getCurrentSecond() + greenD;
        data.setExpirationTime(newExpiration);
        data.setTimerType(Timer.GREEN);
        plugin.getDeadPetsManager().saveDeadPet(pet.getUniqueId(), newExpiration, Timer.GREEN,
                data.getTimerArmorStandUuid(), data.getOwnerArmorStandUuid());

        if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            item.setAmount(item.getAmount() - 1);
        }
    }

    @EventHandler
    public void HurtGravePet(EntityDamageEvent e) {
        if (e.getEntity() instanceof Tameable pet && pet.getOwner() != null) {
            if (deadPets.containsKey(pet.getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }

    private void startTimer(UUID petUuid, Tameable pet, ArmorStand timeStand, ArmorStand ownerStand, DeadPetData data) {
        new BukkitRunnable() {
            @Override
            public void run() {
                long remaining = data.getExpirationTime() - TimeUtils.getCurrentSecond();
                if (timeStand == null || ownerStand == null) {
                    deadPets.remove(petUuid);
                    plugin.getDeadPetsManager().removeDeadPet(petUuid);
                    this.cancel();
                    return;
                }
                MiniMessage miniMessage = MiniMessage.miniMessage();
                String formattedTime = formatTime(remaining);
                String timeMessage = Objects.requireNonNull(plugin.getConfig().getString(
                                "messages." + (data.getTimerType() == Timer.RED ? "death-message" : "revive-message")))
                        .replace("{time}", formattedTime)
                        .replace("{owner}", Objects.requireNonNull(Objects.requireNonNull(pet.getOwner()).getName()));
                String ownerMessage = Objects.requireNonNull(plugin.getConfig().getString("messages.owner-message"))
                        .replace("{owner}", pet.getOwner().getName());
                timeMessage = timeMessage.replaceAll("&#([A-Fa-f0-9]{6})", "<#$1>");
                ownerMessage = ownerMessage.replaceAll("&#([A-Fa-f0-9]{6})", "<#$1>");
                timeStand.customName(miniMessage.deserialize(timeMessage));
                ownerStand.customName(miniMessage.deserialize(ownerMessage));
                if (remaining <= 0) {
                    if (data.getTimerType() == Timer.RED) {
                        timeStand.remove();
                        ownerStand.remove();
                        pet.remove();
                    } else {
                        pet.setAI(true);
                        pet.setSilent(false);
                        pet.setInvulnerable(false);
                        pet.setInvisible(false);
                        if (pet.getAttribute(Attribute.MAX_HEALTH) != null) {
                            pet.setHealth(Objects.requireNonNull(pet.getAttribute(Attribute.MAX_HEALTH)).getValue());
                        }
                        timeStand.remove();
                        ownerStand.remove();
                    }
                    deadPets.remove(petUuid);
                    plugin.getDeadPetsManager().removeDeadPet(petUuid);
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 20, 20);
    }

    public void restoreDeadPet(UUID petUuid, DeadPetData data) {
        Entity petEntity = Bukkit.getEntity(petUuid);
        if (!(petEntity instanceof Tameable pet) || pet.getOwner() == null) {
            plugin.getDeadPetsManager().removeDeadPet(petUuid);
            return;
        }
        Entity timerEntity = Bukkit.getEntity(data.getTimerArmorStandUuid());
        if (!(timerEntity instanceof ArmorStand timeStand)) {
            plugin.getDeadPetsManager().removeDeadPet(petUuid);
            return;
        }
        Entity ownerEntity = Bukkit.getEntity(data.getOwnerArmorStandUuid());
        if (!(ownerEntity instanceof ArmorStand ownerStand)) {
            plugin.getDeadPetsManager().removeDeadPet(petUuid);
            return;
        }

        long remaining = data.getExpirationTime() - TimeUtils.getCurrentSecond();
        if (remaining <= 0) {
            if (data.getTimerType() == Timer.RED) {
                timeStand.remove();
                ownerStand.remove();
                pet.remove();
            } else {
                pet.setAI(true);
                pet.setSilent(false);
                pet.setInvulnerable(false);
                pet.setInvisible(false);
                if (pet.getAttribute(Attribute.MAX_HEALTH) != null) {
                    pet.setHealth(Objects.requireNonNull(pet.getAttribute(Attribute.MAX_HEALTH)).getValue());
                }
                timeStand.remove();
                ownerStand.remove();
            }
            plugin.getDeadPetsManager().removeDeadPet(petUuid);
        } else {
            deadPets.put(petUuid, data);
            startTimer(petUuid, pet, timeStand, ownerStand, data);
        }
    }

    private String formatTime(long remaining) {
        long minutes = remaining / 60;
        long seconds = remaining % 60;
        if (minutes > 0 && seconds > 0) {
            return minutes + " хв. " + seconds + " с.";
        } else if (minutes > 0) {
            return minutes + " хв.";
        } else {
            return seconds + " с.";
        }
    }

    private int getDeathTime(Player player) {
        Map<String, Integer> deathTimes = Objects.requireNonNull(plugin.getConfig().getConfigurationSection("death-time-overrides")).getValues(false)
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (Integer) e.getValue()));
        for (Map.Entry<String, Integer> entry : deathTimes.entrySet()) {
            if (player.hasPermission(entry.getKey())) {
                return entry.getValue();
            }
        }
        return plugin.getConfig().getInt("default-death-time", 120);
    }

    private int getResurrectionTime(Player player) {
        Map<String, Integer> resurrectionTimes = Objects.requireNonNull(plugin.getConfig().getConfigurationSection("resurrection-time-overrides")).getValues(false)
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (Integer) e.getValue()));
        for (Map.Entry<String, Integer> entry : resurrectionTimes.entrySet()) {
            if (player.hasPermission(entry.getKey())) {
                return entry.getValue();
            }
        }
        return plugin.getConfig().getInt("default-resurrection-time", 60);
    }
}