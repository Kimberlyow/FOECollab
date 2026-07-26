package io.github.foecollab.handler;

import com.google.gson.*;
import io.github.foecollab.FOECollab;
import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.FOMC.Types.Fish;
import io.github.foecollab.FOMC.Types.Pet;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.handler.EventHandler.WeatherEvent;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ProfileDataHandler {
    private static ProfileDataHandler INSTANCE = new ProfileDataHandler();
    private final FOEConfig config = FOEConfig.getConfig();
    private boolean isSavedAfterTimer = false;

    public ProfileData profileData = new ProfileData();
    public ProfileData prevProfileData = new ProfileData();
    public boolean isDataLoaded = false;
    public long lastUpdateTime = System.currentTimeMillis();
    public UUID playerUUID = null;

    public static ProfileDataHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ProfileDataHandler();
        }
        return INSTANCE;
    }

    public void tick() {
        if(isDataLoaded && LoadingHandler.instance().wasOnServer && !prevProfileData.equals(profileData)) {
            prevProfileData = new ProfileData(profileData);
            this.saveStats();
        }

        //TODO remove in 0.2.4
        if(profileData.lightningBottleDryStreak == 0) {
            profileData.lightningBottleDryStreak = profileData.allFishCaughtCount;
        }

        if(!profileData.variantCounts.containsKey(Constant.ALTERNATE)) {
            profileData.variantCounts.put(Constant.ALTERNATE, 0);
        }

        // Fabled Drystreak is 0 if not present, since its different to the other variants
        if(!profileData.variantDryStreak.containsKey(Constant.FABLED)) {
            profileData.variantDryStreak.put(Constant.FABLED, 0);
        }

        if(!profileData.variantDryStreak.containsKey(Constant.ALTERNATE) || (profileData.variantDryStreak.containsKey(Constant.ALTERNATE) && profileData.variantDryStreak.get(Constant.ALTERNATE) == 0) ) {
            profileData.variantDryStreak.put(Constant.ALTERNATE, profileData.allFishCaughtCount);
        }
    }

    public void onJoinServer(PlayerEntity player) {
        ProfileDataHandler.instance().playerUUID = player.getUuid();
        ProfileDataHandler.instance().loadStats();
    }

    /**
     * Update stats from new Fish
     */
    public void updateStatsOnCatch(Fish fish) {
        // All-time stats
        this.profileData.allFishCaughtCount++;
        this.profileData.timerFishCaughtCount++;
        this.profileData.allTotalXP += fish.xp;
        this.profileData.allTotalValue += fish.value;
        this.profileData.allFishSizeCounts.put(fish.size, this.profileData.allFishSizeCounts.getOrDefault(fish.size, 0) + 1);
        this.profileData.allVariantCounts.put(fish.variant, this.profileData.allVariantCounts.getOrDefault(fish.variant, 0) + 1);
        if (fish.variant == Constant.SPOOKY) {
            FOECollab.LOGGER.info("[FoE] Tracking SPOOKY variant fish: {} (count: {})", fish.fishId, this.profileData.allVariantCounts.get(Constant.SPOOKY));
        }
        this.profileData.allRarityCounts.put(fish.rarity, this.profileData.allRarityCounts.getOrDefault(fish.rarity, 0) + 1);

        this.profileData.lastFishCaughtTime = System.currentTimeMillis();
        this.profileData.timerPaused = false;

        // Session stats
        this.profileData.fishCaughtCount++;
        this.profileData.totalXP += fish.xp;
        this.profileData.totalValue += fish.value;
        this.profileData.fishSizeCounts.put(fish.size, this.profileData.fishSizeCounts.getOrDefault(fish.size, 0) + 1);
        this.profileData.variantCounts.put(fish.variant, this.profileData.variantCounts.getOrDefault(fish.variant, 0) + 1);
        this.profileData.rarityCounts.put(fish.rarity, this.profileData.rarityCounts.getOrDefault(fish.rarity, 0) + 1);

        FishCatchHandler.instance().onFishCaughtSendDryStreak(fish);

        this.profileData.fishSizeDryStreak.put(fish.size, this.profileData.allFishCaughtCount);
        if (fish.variant == Constant.FABLED) {
            this.profileData.variantDryStreak.put(Constant.FABLED, 0);
        } else {
            this.profileData.variantDryStreak.put(fish.variant, this.profileData.allFishCaughtCount);
        }
        this.profileData.rarityDryStreak.put(fish.rarity, this.profileData.allFishCaughtCount);

        this.isSavedAfterTimer = false;
    }

    public void updateStatsOnCatch() {
        if (!Objects.equals(BossBarHandler.instance().weather, Constant.THUNDERSTORM.ID)) {
            this.profileData.lightningBottleDryStreak++;
        }
        // Only increment infusion capsule dry streak if it's not currently a Blood Moon (incrementing this var avoids it being counted in stats counterintuitively)
        if (!(EventHandler.instance().currentMoon == WeatherEvent.BLOOD_MOON)) {         
            
            this.profileData.infusionCapsuleDryStreak++;
        }

        // Fabled dry streak
        // only count while fabled event is active and player is at fabled location (or any location if unknown)
        String fabledLoc = EventHandler.instance().fabledLocation;
        boolean atFabledLocation = fabledLoc.isEmpty() || BossBarHandler.instance().currentLocation.ID.equalsIgnoreCase(fabledLoc);
        if (EventHandler.instance().isFabledActive && atFabledLocation) {
            this.profileData.variantDryStreak.put(Constant.FABLED,
                this.profileData.variantDryStreak.getOrDefault(Constant.FABLED, 0) + 1);
        }
    }

    public void updatePetCaughtStatsOnCatch() {
        // All-time stats
        this.profileData.allPetCaughtCount++;

        // Session stats
        this.profileData.petCaughtCount++;

        this.profileData.petDryStreak = this.profileData.allFishCaughtCount;
    }

    public void updateShardCaughtStatsOnCatch(int count) {
        // All-time stats
        this.profileData.allShardCaughtCount += count;

        // Session stats
        this.profileData.shardCaughtCount += count;

        this.profileData.shardDryStreak = this.profileData.allFishCaughtCount;
    }

    public void updateLightningBottleCaughtStatsOnCatch() {
        // All-time stats
        this.profileData.allLightningBottleCount++;

        // Session stats
        this.profileData.lightningBottleCount++;

        this.profileData.lightningBottleDryStreak = this.profileData.allFishCaughtCount;
    }

    public void updateInfusionCapsuleCaughtStatsOnCatch() {
        // All-time stats
        this.profileData.allInfusionCapsuleCount++;

        // Session stats
        this.profileData.infusionCapsuleCount++;

        this.profileData.infusionCapsuleDryStreak = this.profileData.allFishCaughtCount;
    }

    public void updatePet(Pet pet, int slot) {
        this.profileData.equippedPet = pet;
        this.profileData.equippedPetSlot = slot;
    }

    public void resetPet() {
        this.profileData.equippedPet = null;
        this.profileData.equippedPetSlot = -1;
    }

    /**
     * Save Stats to disk
     */
    public void saveStats() {
        try {
            if (playerUUID != null && MinecraftClient.getInstance().player != null && Objects.equals(playerUUID, MinecraftClient.getInstance().player.getUuid())) {
                Path configDir = FabricLoader.getInstance().getConfigDir();
                Path subDir = configDir.resolve("foe");
                Path statsDir = subDir.resolve("stats");
                Files.createDirectories(statsDir);
                Path filePath = statsDir.resolve(playerUUID.toString() + ".json");
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(this.profileData);
                Files.writeString(filePath, json);
            }
        } catch (IOException e) {
            FOECollab.LOGGER.error(e.getMessage());
        }
    }

    // Backups so people like Aidan dont lose all their stats when something goes wrong
    public boolean createBackup() {
        try {
            if (playerUUID != null && MinecraftClient.getInstance().player != null && Objects.equals(playerUUID, MinecraftClient.getInstance().player.getUuid())) {
                Path configDir = FabricLoader.getInstance().getConfigDir();
                Path subDir = configDir.resolve("foe");
                Path statsDir = subDir.resolve("stats");
                Path backupDir = statsDir.resolve("backups");
                Files.createDirectories(backupDir);
                
                Path sourceFile = statsDir.resolve(playerUUID.toString() + ".json");
                if (!Files.exists(sourceFile)) {
                    return false;
                }
                
                String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
                Path backupFile = backupDir.resolve(playerUUID.toString() + "_" + timestamp + ".json");
                
                Files.copy(sourceFile, backupFile);
                FOECollab.LOGGER.info("[FoE] Created stats backup: {}", backupFile.getFileName());
                return true;
            }
        } catch (IOException e) {
            FOECollab.LOGGER.error("Failed to create stats backup: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Load Stats from disk
     */
    public void loadStats() {
        try {
            if(playerUUID != null) {
                Path configDir = FabricLoader.getInstance().getConfigDir();
                Path subDir = configDir.resolve("foe");
                Path statsDir = subDir.resolve("stats");
                Files.createDirectories(statsDir);
                Path filePath = statsDir.resolve(playerUUID.toString() + ".json");
                if (!Files.exists(filePath)) {
                    Files.createFile(filePath);
                    isDataLoaded = true;
                    saveStats();
                    return;
                };
                String json = Files.readString(filePath, UTF_8);
                Gson gson = new GsonBuilder().create();

                if (json == null || json.isBlank()) {
                    isDataLoaded = true;
                    saveStats();
                    return;
                }

                try {
                    this.profileData = gson.fromJson(json, ProfileData.class);
                } catch (JsonSyntaxException ex) {
                    FOECollab.LOGGER.error("[FoE] Failed to parse stats JSON ({}): {}", filePath, ex.getMessage());
                    try {
                        JsonElement root = JsonParser.parseString(json);
                        this.profileData = gson.fromJson(root, ProfileData.class);
                        if (this.profileData == null) {
                            this.profileData = new ProfileData();
                        }
                        saveStats();
                    } catch (Exception ex2) {
                        FOECollab.LOGGER.error("[FoE] Failed to recover stats JSON ({}): {}", filePath, ex2.getMessage());
                        this.profileData = new ProfileData();
                        saveStats();
                    }
                }

                if (this.profileData == null) {
                    this.profileData = new ProfileData();
                }
                isDataLoaded = true;
            }
        } catch (IOException e) {
            FOECollab.LOGGER.error(e.getMessage());
        }
    }

    /**
     * Reset Stats, but not all-time stats
     */
    public void resetStats() {
        this.profileData.fishCaughtCount = 0;
        this.profileData.totalXP = 0.0f;
        this.profileData.totalValue = 0.0f;
        this.profileData.variantCounts.clear();
        this.profileData.rarityCounts.clear();
        this.profileData.fishSizeCounts.clear();
        this.profileData.questsCompleted = 0;
        this.profileData.petCaughtCount = 0;
        this.profileData.shardCaughtCount = 0;
        this.profileData.lightningBottleCount = 0;
        this.profileData.infusionCapsuleCount = 0;
        this.profileData.petsFromQuests = 0;
        this.profileData.shardsFromQuests = 0;
        if(config.fishTracker.isFishTrackerOnTimer) {
            this.profileData.timerFishCaughtCount = 0;
            this.profileData.activeTime = 0;
        }
        this.profileData.lastFishCaughtTime = 0;
        this.profileData.timerPaused = true;

        FishCatchHandler.instance().reset();
    }

    public void resetTimer() {
        this.profileData.timerFishCaughtCount = 0;
        this.profileData.activeTime = 0;
    }

    public void tickTimer() {
        long currentTime = System.currentTimeMillis();
        // Pause timer when not fishing after x seconds
        long timeSinceLastFish = currentTime - ProfileDataHandler.instance().profileData.lastFishCaughtTime;
        if (timeSinceLastFish >= TimeUnit.SECONDS.toMillis(config.fishTracker.autoPauseTimer)) {
            ProfileDataHandler.instance().profileData.timerPaused = true;
            if(!isSavedAfterTimer) {
                this.isSavedAfterTimer = true;
            }
        }

        long delta = currentTime - ProfileDataHandler.instance().lastUpdateTime;
        ProfileDataHandler.instance().lastUpdateTime = currentTime;

        // Track time when fishing
        if(!ProfileDataHandler.instance().profileData.timerPaused) {
            ProfileDataHandler.instance().profileData.activeTime += delta;
        }
    }

    public void resetDryStreak() {
        profileData.petDryStreak = profileData.allFishCaughtCount;
        profileData.shardDryStreak = profileData.allFishCaughtCount;
        profileData.lightningBottleDryStreak = profileData.allFishCaughtCount;
        profileData.infusionCapsuleDryStreak = profileData.allFishCaughtCount;
        profileData.rarityDryStreak.put(Constant.COMMON, profileData.allFishCaughtCount);
        profileData.rarityDryStreak.put(Constant.RARE, profileData.allFishCaughtCount);
        profileData.rarityDryStreak.put(Constant.EPIC, profileData.allFishCaughtCount);
        profileData.rarityDryStreak.put(Constant.LEGENDARY, profileData.allFishCaughtCount);
        profileData.rarityDryStreak.put(Constant.MYTHICAL, profileData.allFishCaughtCount);
        profileData.rarityDryStreak.put(Constant.SPECIAL, profileData.allFishCaughtCount);
        profileData.fishSizeDryStreak.put(Constant.BABY, profileData.allFishCaughtCount);
        profileData.fishSizeDryStreak.put(Constant.JUVENILE, profileData.allFishCaughtCount);
        profileData.fishSizeDryStreak.put(Constant.ADULT, profileData.allFishCaughtCount);
        profileData.fishSizeDryStreak.put(Constant.LARGE, profileData.allFishCaughtCount);
        profileData.fishSizeDryStreak.put(Constant.GIGANTIC, profileData.allFishCaughtCount);
        profileData.variantDryStreak.put(Constant.ALBINO, profileData.allFishCaughtCount);
        profileData.variantDryStreak.put(Constant.MELANISTIC, profileData.allFishCaughtCount);
        profileData.variantDryStreak.put(Constant.TROPHY, profileData.allFishCaughtCount);
        profileData.variantDryStreak.put(Constant.FABLED, 0);
        profileData.variantDryStreak.put(Constant.ALTERNATE, profileData.allFishCaughtCount);
    }

    public static class ProfileData {
        // Session stats
        public int fishCaughtCount = 0;
        public float totalXP = 0.0f;
        public float totalValue = 0.0f;
        public Map<Constant, Integer> variantCounts = new HashMap<>();
        public Map<Constant, Integer> rarityCounts = new HashMap<>();
        public Map<Constant, Integer> fishSizeCounts = new HashMap<>();
        public int questsCompleted = 0;
        public int petCaughtCount = 0;
        public int shardCaughtCount = 0;
        public int lightningBottleCount = 0;
        public int infusionCapsuleCount = 0;
        public int petsFromQuests = 0;
        public int shardsFromQuests = 0;

        // Current active timer stats
        public long activeTime = 0;
        public long lastFishCaughtTime = 0;
        public boolean timerPaused = true;

        // All-time stats
        public int allFishCaughtCount = 0;
        public float allTotalXP = 0.0f;
        public float allTotalValue = 0.0f;
        public Map<Constant, Integer> allRarityCounts = new HashMap<>();
        public Map<Constant, Integer> allVariantCounts = new HashMap<>();
        public Map<Constant, Integer> allFishSizeCounts = new HashMap<>();
        public int allQuestsCompleted = 0;
        public int allPetCaughtCount = 0;
        public int allShardCaughtCount = 0;
        public int allLightningBottleCount = 0;
        public int allInfusionCapsuleCount = 0;
        public int allPetsFromQuests = 0;
        public int allShardsFromQuests = 0;

        public int timerFishCaughtCount = 0;

        // Equipped Pet
        public int equippedPetSlot = -1;
        public Pet equippedPet = null;

        // Dry streak count
        public int petDryStreak;
        public int shardDryStreak;
        public int lightningBottleDryStreak;
        public int infusionCapsuleDryStreak;
        public Map<Constant, Integer> rarityDryStreak = new HashMap<>();
        public Map<Constant, Integer> variantDryStreak = new HashMap<>();
        public Map<Constant, Integer> fishSizeDryStreak = new HashMap<>();

        // Crew Data
        public List<UUID> crewMembers = new ArrayList<>();
        public boolean isInCrewChat = false;

        // Friend Data
        public List<UUID> friends = new ArrayList<>();

        // Quest Data
        public Map<Constant, List<QuestHandler.Quest>> activeQuests = new HashMap<>();

        // Locked Rolls Data
        public Map<Integer, List<String>> lockedArmorRolls = new HashMap<>();

        // Bait Sorting Helper Toggle
        public boolean baitSortingHelperToggle = false;

        // Stats Data
        public boolean isStatsInitialized = false;

        public ProfileData() {
        }

        public ProfileData(ProfileData prevData) {
            fishCaughtCount = prevData.fishCaughtCount;
            totalXP = prevData.totalXP;
            totalValue = prevData.totalValue;
            variantCounts = new HashMap<>(prevData.variantCounts);
            rarityCounts = new HashMap<>(prevData.rarityCounts);
            fishSizeCounts = new HashMap<>(prevData.fishSizeCounts);
            petCaughtCount = prevData.petCaughtCount;
            shardCaughtCount = prevData.shardCaughtCount;
            lightningBottleCount = prevData.lightningBottleCount;
            infusionCapsuleCount = prevData.infusionCapsuleCount;
            activeTime = prevData.activeTime;
            lastFishCaughtTime = prevData.lastFishCaughtTime;
            timerPaused = prevData.timerPaused;
            allFishCaughtCount = prevData.allFishCaughtCount;
            allTotalXP = prevData.allTotalXP;
            allTotalValue = prevData.allTotalValue;
            allRarityCounts = new HashMap<>(prevData.allRarityCounts);
            allVariantCounts = new HashMap<>(prevData.allVariantCounts);
            allFishSizeCounts = new HashMap<>(prevData.allFishSizeCounts);
            allPetCaughtCount = prevData.allPetCaughtCount;
            allShardCaughtCount = prevData.allShardCaughtCount;
            allLightningBottleCount = prevData.allLightningBottleCount;
            allInfusionCapsuleCount = prevData.allInfusionCapsuleCount;
            timerFishCaughtCount = prevData.timerFishCaughtCount;
            equippedPetSlot = prevData.equippedPetSlot;
            equippedPet = prevData.equippedPet;
            petDryStreak = prevData.petDryStreak;
            shardDryStreak = prevData.shardDryStreak;
            lightningBottleDryStreak = prevData.lightningBottleDryStreak;
            infusionCapsuleDryStreak = prevData.infusionCapsuleDryStreak;
            rarityDryStreak = new HashMap<>(prevData.rarityDryStreak);
            variantDryStreak = new HashMap<>(prevData.variantDryStreak);
            fishSizeDryStreak = new HashMap<>(prevData.fishSizeDryStreak);
            crewMembers = new ArrayList<>(prevData.crewMembers);
            isInCrewChat = prevData.isInCrewChat;
            friends = new ArrayList<>(prevData.friends);
            activeQuests = new HashMap<>(prevData.activeQuests);
            lockedArmorRolls = new HashMap<>(prevData.lockedArmorRolls);
            baitSortingHelperToggle = prevData.baitSortingHelperToggle;
            isStatsInitialized = prevData.isStatsInitialized;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == this) {
                return true;
            }

            return obj instanceof ProfileData oldProfileData
                    && this.fishCaughtCount == oldProfileData.fishCaughtCount
                    && this.totalXP == oldProfileData.totalXP
                    && this.totalValue == oldProfileData.totalValue
//                    && this.variantCounts.equals(oldProfileData.variantCounts)
//                    && this.rarityCounts.equals(oldProfileData.rarityCounts)
//                    && this.fishSizeCounts.equals(oldProfileData.fishSizeCounts)
                    && this.petCaughtCount == oldProfileData.petCaughtCount
                    && this.shardCaughtCount == oldProfileData.shardCaughtCount
                    && this.lightningBottleCount == oldProfileData.lightningBottleCount
                    && this.infusionCapsuleCount == oldProfileData.infusionCapsuleCount
                    && this.lastFishCaughtTime == oldProfileData.lastFishCaughtTime
                    && this.timerPaused == oldProfileData.timerPaused
                    && this.allFishCaughtCount == oldProfileData.allFishCaughtCount
                    && this.allTotalXP == oldProfileData.allTotalXP
                    && this.allTotalValue == oldProfileData.allTotalValue
//                    && this.allRarityCounts.equals(oldProfileData.allRarityCounts)
//                    && this.allVariantCounts.equals(oldProfileData.allVariantCounts)
//                    && this.allFishSizeCounts.equals(oldProfileData.allFishSizeCounts)
                    && this.allPetCaughtCount == oldProfileData.allPetCaughtCount
                    && this.allShardCaughtCount == oldProfileData.allShardCaughtCount
                    && this.allLightningBottleCount == oldProfileData.allLightningBottleCount
                    && this.allInfusionCapsuleCount == oldProfileData.allInfusionCapsuleCount
                    && this.timerFishCaughtCount == oldProfileData.timerFishCaughtCount
                    && this.equippedPetSlot == oldProfileData.equippedPetSlot
                    && this.petDryStreak == oldProfileData.petDryStreak
                    && this.shardDryStreak == oldProfileData.shardDryStreak
                    && this.lightningBottleDryStreak == oldProfileData.lightningBottleDryStreak
                    && this.infusionCapsuleDryStreak == oldProfileData.infusionCapsuleDryStreak
                    && this.rarityDryStreak.equals(oldProfileData.rarityDryStreak)
                    && this.variantDryStreak.equals(oldProfileData.variantDryStreak)
                    && this.fishSizeDryStreak.equals(oldProfileData.fishSizeDryStreak)
                    && this.crewMembers.equals(oldProfileData.crewMembers)
                    && this.isInCrewChat == oldProfileData.isInCrewChat
                    && this.friends.equals(oldProfileData.friends)
                    && this.activeQuests.equals(oldProfileData.activeQuests)
                    && this.baitSortingHelperToggle == oldProfileData.baitSortingHelperToggle;
        }
    }
}
