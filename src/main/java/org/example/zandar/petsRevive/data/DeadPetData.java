package org.example.zandar.petsRevive.data;

import org.example.zandar.petsRevive.enums.Timer;

import java.util.UUID;

public class DeadPetData {
    private final UUID timerASU;
    private final UUID ownerASU;
    private long expTime;
    private Timer timerType;

    public DeadPetData(UUID timerASU, UUID ownerASU, long expTime, Timer timerType) {
        this.timerASU = timerASU;
        this.ownerASU = ownerASU;
        this.expTime = expTime;
        this.timerType = timerType;
    }

    public UUID getTimerArmorStandUuid() {
        return timerASU;
    }

    public UUID getOwnerArmorStandUuid() {
        return ownerASU;
    }

    public long getExpirationTime() {
        return expTime;
    }

    public void setExpirationTime(long expTime) {
        this.expTime = expTime;
    }

    public Timer getTimerType() {
        return timerType;
    }

    public void setTimerType(Timer timerType) {
        this.timerType = timerType;
    }
}