package io.github.foecollab.handler;

import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.config.FOEConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.HashMap;
import java.util.Map;

public class NotificationSoundHandler {
    private static NotificationSoundHandler INSTANCE = new NotificationSoundHandler();
    private final FOEConfig config = FOEConfig.getConfig();
    private final Map<NotificationType, Long> lastPlayedSoundTime = new HashMap<>();

    public static NotificationSoundHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new NotificationSoundHandler();
        }
        return INSTANCE;
    }

    public void init() {
        lastPlayedSoundTime.put(NotificationType.PET_EQUIP, 0L);
        lastPlayedSoundTime.put(NotificationType.INVENTORY_FULL, 0L);
        lastPlayedSoundTime.put(NotificationType.WEATHER_ALERT, 0L);
    }

    public void tick(MinecraftClient minecraftClient) {
        if(minecraftClient.player != null) {
            if(config.notifications.showWarningHud) {
                // Pet Equip Warning Sound
                if(config.petEquipTracker.warningOptions.showPetEquipWarningHUD
                        && config.petEquipTracker.warningOptions.usePetEquipWarningSound
                        && PetEquipHandler.instance().petStatus == PetEquipHandler.PetStatus.NO_PET
                        && BossBarHandler.instance().currentLocation != Constant.CREW_ISLAND
                        && BossBarHandler.instance().currentLocation != Constant.SPAWNHUB
                ) {
                    if(System.currentTimeMillis() - lastPlayedSoundTime.get(NotificationType.PET_EQUIP) > config.petEquipTracker.warningOptions.timePetEquipWarningSound * 1000L) {
                        playSoundWarning(config.petEquipTracker.warningOptions.petEquipSoundType, minecraftClient);
                        lastPlayedSoundTime.put(NotificationType.PET_EQUIP, System.currentTimeMillis());
                    }
                }

                // Full Inventory Warning Sound
                if(config.fullInventoryTracker.showFullInventoryWarningHUD
                        && config.fullInventoryTracker.useInventoryWarningSound
                        && FullInventoryHandler.instance().isOverThreshold
                        && BossBarHandler.instance().currentLocation != Constant.CREW_ISLAND
                        && BossBarHandler.instance().currentLocation != Constant.SPAWNHUB
                ) {
                    if(System.currentTimeMillis() - lastPlayedSoundTime.get(NotificationType.INVENTORY_FULL) > config.fullInventoryTracker.timeInventoryWarningSound * 1000L) {
                        playSoundWarning(config.fullInventoryTracker.fullInventorySoundType, minecraftClient);
                        lastPlayedSoundTime.put(NotificationType.INVENTORY_FULL, System.currentTimeMillis());
                    }
                }
            }
        }
    }

    /** Loud, master-category default for notification sounds (1.0 = full master volume). */
    private static final float DEFAULT_VOLUME = 1.0f;
    /** Extra amplitude multiplier on top of the configured volume; >1.0 plays a stacked copy. */
    private static final float LOUDNESS_BOOST = 1.5f;

    public void playSoundWarning(SoundType soundType, MinecraftClient client) {
        playSoundWarning(soundType, client, DEFAULT_VOLUME);
    }

    public void playSoundWarning(SoundType soundType, MinecraftClient client, float volume) {
        if (client.player == null || volume <= 0f) {
            return;
        }
        SoundEvent sound = switch (soundType) {
            case PLING -> SoundEvents.BLOCK_NOTE_BLOCK_PLING.value();
            case BASS -> SoundEvents.BLOCK_NOTE_BLOCK_BASS.value();
            case BELL -> SoundEvents.BLOCK_NOTE_BLOCK_BELL.value();
            case BIT -> SoundEvents.BLOCK_NOTE_BLOCK_BIT.value();
            case CHIME -> SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value();
            case DIDGERIDOO -> SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO.value();
            case COW_BELL -> SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL.value();
            case FLUTE -> SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value();
            case GUITAR -> SoundEvents.BLOCK_NOTE_BLOCK_GUITAR.value();
            case HARP -> SoundEvents.BLOCK_NOTE_BLOCK_HARP.value();
        };
        // Master-category, non-positional playback so no sub-slider (Players, Note Blocks, ...)
        // can quiet it. A single instance's amplitude caps at 1.0 no matter the volume value, so
        // the boost past 100% comes from stacking a second copy of the same sound.
        float boosted = volume * LOUDNESS_BOOST;
        client.getSoundManager().play(PositionedSoundInstance.master(sound, 1f, Math.min(boosted, 1f)));
        float extra = Math.min(boosted - 1f, 1f);
        if (extra > 0f) {
            client.getSoundManager().play(PositionedSoundInstance.master(sound, 1f, extra));
        }
    }

    public enum SoundType {
        PLING, // Default Pet
        BASS, // Default Inventory
        BELL,
        BIT,
        CHIME,
        DIDGERIDOO,
        COW_BELL,
        FLUTE,
        GUITAR,
        HARP
    }

    private enum NotificationType {
        PET_EQUIP,
        INVENTORY_FULL,
        WEATHER_ALERT
    }
}
