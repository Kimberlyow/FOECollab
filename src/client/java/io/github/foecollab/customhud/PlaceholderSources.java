package io.github.foecollab.customhud;

import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.customhud.PlaceholderEngine.ValueResult;
import io.github.foecollab.handler.BossBarHandler;
import io.github.foecollab.handler.ProfileDataHandler;
import io.github.foecollab.handler.ProfileDataHandler.ProfileData;
import io.github.foecollab.handler.ScoreboardHandler;
import io.github.foecollab.handler.TabHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * The data-source placeholders for custom HUDs — the {@code %source.path%} half of the custom-code
 * language. Each source maps a path (everything after the first dot, e.g. {@code health} in
 * {@code %player.health%}) to a value pulled from FishOnMC-Extras' live data. Returning an absent
 * value hides the line that referenced it.
 *
 * <p>Supported sources: {@code player}, {@code world}, {@code time}, {@code boss_bar},
 * {@code location}, {@code scoreboard}, {@code tab}, {@code stats}, {@code stats_data},
 * {@code constant_data}. The last two mirror FoE-R's detailed stats/constant sources so HUDs
 * shared from FoE-R resolve their per-rarity/size/variant catch counts and dry streaks.</p>
 */
public class PlaceholderSources {
    static final Map<String, Function<String[], ValueResult>> SOURCES = Map.ofEntries(
            Map.entry("player", guard(PlaceholderSources::player)),
            Map.entry("world", guard(PlaceholderSources::world)),
            Map.entry("time", guard(PlaceholderSources::time)),
            Map.entry("boss_bar", guard(PlaceholderSources::bossBar)),
            Map.entry("location", guard(PlaceholderSources::location)),
            Map.entry("scoreboard", guard(PlaceholderSources::scoreboard)),
            Map.entry("tab", guard(PlaceholderSources::tab)),
            Map.entry("stats", guard(PlaceholderSources::stats)),
            Map.entry("stats_data", guard(PlaceholderSources::statsData)),
            Map.entry("constant_data", guard(PlaceholderSources::constantData)));

    /** A data lookup that never throws — any error resolves to an absent value (hides the line). */
    private static Function<String[], ValueResult> guard(Function<String[], ValueResult> source) {
        return params -> {
            try {
                return source.apply(params);
            } catch (Exception e) {
                return ValueResult.absent();
            }
        };
    }

    private static String first(String[] params) {
        return params.length > 0 ? params[0] : "";
    }

    // region Sources
    private static ValueResult player(String[] params) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return ValueResult.absent();
        }
        return switch (first(params)) {
            case "name" -> ValueResult.str(player.getName().getString());
            case "health" -> ValueResult.str(String.valueOf((int) player.getHealth()));
            case "max_health" -> ValueResult.str(String.valueOf((int) player.getMaxHealth()));
            case "hunger", "food" -> ValueResult.str(String.valueOf(player.getHungerManager().getFoodLevel()));
            case "saturation" -> ValueResult.str(String.valueOf((int) player.getHungerManager().getSaturationLevel()));
            case "armor" -> ValueResult.str(String.valueOf(player.getArmor()));
            case "air" -> ValueResult.str(String.valueOf(player.getAir()));
            case "xp_level", "level" -> ValueResult.str(String.valueOf(player.experienceLevel));
            // FoE only sends the level + bar-fill fraction (no real XP point totals), so the % toward
            // the next level is all that's meaningful. experienceProgress is the 0-1 bar fill.
            case "xp_progress" -> ValueResult.str(StyledText.floatToString(player.experienceProgress * 100f, 1));
            case "x" -> ValueResult.str(String.valueOf(player.getBlockX()));
            case "y" -> ValueResult.str(String.valueOf(player.getBlockY()));
            case "z" -> ValueResult.str(String.valueOf(player.getBlockZ()));
            case "exact_x" -> ValueResult.str(StyledText.floatToString((float) player.getX(), 1));
            case "exact_y" -> ValueResult.str(StyledText.floatToString((float) player.getY(), 1));
            case "exact_z" -> ValueResult.str(StyledText.floatToString((float) player.getZ(), 1));
            case "yaw" -> ValueResult.str(String.valueOf((int) player.getYaw()));
            case "pitch" -> ValueResult.str(String.valueOf((int) player.getPitch()));
            case "direction" -> ValueResult.str(direction(player.getYaw()));
            case "dimension" -> ValueResult.of(player.getEntityWorld().getRegistryKey().getValue().getPath());
            case "held_item" -> player.getMainHandStack().isEmpty()
                    ? ValueResult.absent()
                    : ValueResult.str(player.getMainHandStack().getName().getString());
            default -> ValueResult.absent();
        };
    }

    private static ValueResult world(String[] params) {
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) {
            return ValueResult.absent();
        }
        long timeOfDay = world.getTimeOfDay() % 24000L;
        return switch (first(params)) {
            case "time" -> ValueResult.str(String.valueOf(timeOfDay));
            case "day" -> ValueResult.str(String.valueOf(world.getTimeOfDay() / 24000L));
            case "raining" -> ValueResult.str(String.valueOf(world.isRaining()));
            case "thundering" -> ValueResult.str(String.valueOf(world.isThundering()));
            default -> ValueResult.absent();
        };
    }

    private static ValueResult time(String[] params) {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        return switch (first(params)) {
            case "hour" -> ValueResult.str(String.format("%02d", hour));
            case "hour12" -> ValueResult.str(String.valueOf(((hour + 11) % 12) + 1));
            case "minute" -> ValueResult.str(String.format("%02d", now.getMinute()));
            case "second" -> ValueResult.str(String.format("%02d", now.getSecond()));
            case "ampm" -> ValueResult.str(hour < 12 ? "AM" : "PM");
            case "clock" -> ValueResult.str(String.format("%02d:%02d", hour, now.getMinute()));
            case "clock12" -> ValueResult.str(String.format("%d:%02d %s", ((hour + 11) % 12) + 1, now.getMinute(), hour < 12 ? "AM" : "PM"));
            case "date" -> ValueResult.str(String.format("%04d-%02d-%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth()));
            default -> ValueResult.absent();
        };
    }

    private static ValueResult bossBar(String[] params) {
        BossBarHandler handler = BossBarHandler.instance();
        return switch (first(params)) {
            case "time" -> ValueResult.of(handler.time);
            case "weather" -> ValueResult.of(handler.weather);
            case "temperature" -> ValueResult.of(handler.temperature);
            case "location" -> ValueResult.of(handler.currentLocation.TAG.getString());
            default -> ValueResult.absent();
        };
    }

    private static ValueResult location(String[] params) {
        BossBarHandler handler = BossBarHandler.instance();
        return switch (first(params)) {
            case "name" -> ValueResult.of(handler.currentLocation.TAG.getString());
            case "id" -> ValueResult.of(handler.currentLocation.ID);
            default -> ValueResult.absent();
        };
    }

    private static ValueResult scoreboard(String[] params) {
        ScoreboardHandler handler = ScoreboardHandler.instance();
        return switch (first(params)) {
            case "name" -> ValueResult.of(handler.playerName);
            case "level" -> ValueResult.str(String.valueOf(handler.level));
            case "wallet", "money" -> ValueResult.of(handler.wallet);
            case "credits" -> ValueResult.of(handler.credits);
            case "catches" -> ValueResult.of(handler.catches);
            case "catch_rate" -> ValueResult.of(handler.catchRate);
            case "crew_name" -> ValueResult.of(handler.crewName);
            case "crew_level" -> ValueResult.of(handler.crewLevel);
            case "location_min" -> ValueResult.of(handler.locationMin);
            case "location_max" -> ValueResult.of(handler.locationMax);
            default -> ValueResult.absent();
        };
    }

    private static ValueResult tab(String[] params) {
        TabHandler handler = TabHandler.instance();
        MinecraftClient client = MinecraftClient.getInstance();
        return switch (first(params)) {
            case "player" -> ValueResult.of(handler.player.getString());
            case "instance" -> ValueResult.of(handler.instance);
            case "is_instance" -> ValueResult.str(String.valueOf(handler.isInstance));
            case "count" -> client.getNetworkHandler() == null
                    ? ValueResult.absent()
                    : ValueResult.str(String.valueOf(client.getNetworkHandler().getPlayerList().size()));
            default -> ValueResult.absent();
        };
    }

    private static ValueResult stats(String[] params) {
        ProfileDataHandler.ProfileData data = ProfileDataHandler.instance().profileData;
        if (data == null) {
            return ValueResult.absent();
        }
        return switch (first(params)) {
            case "fish_caught" -> ValueResult.str(String.valueOf(data.fishCaughtCount));
            case "all_fish_caught" -> ValueResult.str(String.valueOf(data.allFishCaughtCount));
            case "total_xp" -> ValueResult.str(String.valueOf((int) data.totalXP));
            case "all_total_xp" -> ValueResult.str(String.valueOf((int) data.allTotalXP));
            case "total_value" -> ValueResult.str(String.valueOf((int) data.totalValue));
            case "all_total_value" -> ValueResult.str(String.valueOf((int) data.allTotalValue));
            case "pets" -> ValueResult.str(String.valueOf(data.petCaughtCount));
            case "all_pets" -> ValueResult.str(String.valueOf(data.allPetCaughtCount));
            case "shards" -> ValueResult.str(String.valueOf(data.shardCaughtCount));
            case "all_shards" -> ValueResult.str(String.valueOf(data.allShardCaughtCount));
            case "quests" -> ValueResult.str(String.valueOf(data.questsCompleted));
            case "all_quests" -> ValueResult.str(String.valueOf(data.allQuestsCompleted));
            case "lightning_bottles" -> ValueResult.str(String.valueOf(data.lightningBottleCount));
            case "infusion_capsules" -> ValueResult.str(String.valueOf(data.infusionCapsuleCount));
            // The fish-tracker session timer ("ꜰɪѕʜ ᴛɪᴍᴇ"): formatted HH:MM:SS like the tracker HUD,
            // or as raw total seconds for use in expressions.
            case "fish_time" -> ValueResult.str(formatFishTime(data.activeTime));
            case "fish_time_seconds" -> ValueResult.str(String.valueOf(data.activeTime / 1000L));
            default -> ValueResult.absent();
        };
    }

    /** The fish-tracker session timer in ms, formatted HH:MM:SS exactly as the tracker HUD shows it. */
    private static String formatFishTime(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * FoE-R's detailed catch stats, e.g. {@code %stats_data.data.fish.rarity.common.count%} or
     * {@code %stats_data.data.item.armorShard.dry_streak%}. Paths are {@code data.<fish|pet|item>.…}.
     * Counts are all-time; a dry streak is {@code allFishCaught - <caughtOn>}, matching the FoE
     * drystreak breakdown command.
     *
     * <p>Pets and shards each come in three flavours — {@code total}/{@code count} = self-caught
     * (fished up), {@code quest} = received from quests, {@code both} = the sum:
     * {@code %stats_data.data.pet.total%} / {@code .pet.quest%} / {@code .pet.both%} and
     * {@code %stats_data.data.item.armorShard.count%} / {@code .quest%} / {@code .both%}.</p>
     *
     * <p>Lightning bottles and infusion capsules are separate item types
     * ({@code item.lightningBottle.*}, {@code item.infusionCapsule.*}); {@code item.craftingComponent.*}
     * stays as the combined sum of the two for HUDs that want them grouped.</p>
     */
    private static ValueResult statsData(String[] params) {
        ProfileData data = ProfileDataHandler.instance().profileData;
        if (data == null || params.length < 3 || !params[0].equals("data")) {
            return ValueResult.absent();
        }
        int total = data.allFishCaughtCount;
        return switch (params[1]) {
            case "fish" -> {
                if (params[2].equals("total")) {
                    yield ValueResult.str(String.valueOf(total));
                }
                yield params.length >= 5 ? fishStat(data, params[2], params[3], params[4], total) : ValueResult.absent();
            }
            case "pet" -> switch (params[2]) {
                case "total" -> ValueResult.str(String.valueOf(data.allPetCaughtCount));
                case "quest" -> ValueResult.str(String.valueOf(data.allPetsFromQuests));
                case "both" -> ValueResult.str(String.valueOf(data.allPetCaughtCount + data.allPetsFromQuests));
                case "dry_streak" -> ValueResult.str(String.valueOf(Math.max(0, total - data.petDryStreak)));
                default -> ValueResult.absent();
            };
            case "item" -> params.length >= 4 ? itemStat(data, params[2], params[3], total) : ValueResult.absent();
            default -> ValueResult.absent();
        };
    }

    private static ValueResult fishStat(ProfileData data, String subCategory, String field, String type, int total) {
        Constant constant = Constant.valueOfId(field);
        if (constant == Constant.DEFAULT && !field.equalsIgnoreCase("default")) {
            return ValueResult.absent();
        }
        Map<Constant, Integer> counts;
        Map<Constant, Integer> dryStreak;
        switch (subCategory) {
            case "rarity" -> { counts = data.allRarityCounts; dryStreak = data.rarityDryStreak; }
            case "size" -> { counts = data.allFishSizeCounts; dryStreak = data.fishSizeDryStreak; }
            case "variant" -> { counts = data.allVariantCounts; dryStreak = data.variantDryStreak; }
            default -> { return ValueResult.absent(); }
        }
        return switch (type) {
            case "count" -> ValueResult.str(String.valueOf(counts.getOrDefault(constant, 0)));
            case "dry_streak" -> ValueResult.str(String.valueOf(dryStreakDisplay(constant, total, dryStreak)));
            default -> ValueResult.absent();
        };
    }

    /**
     * The displayed dry streak for a rarity/size/variant, matching the default Fish Tracker HUD.
     * Normally it's {@code total - anchor} (the anchor being the all-time fish count at the last
     * catch). Fabled is the exception: its stored value is a direct counter that only ticks during a
     * fabled event at the fabled location (and resets to 0 on a fabled catch), so it's shown as-is.
     */
    private static int dryStreakDisplay(Constant constant, int total, Map<Constant, Integer> dryStreak) {
        int stored = dryStreak.getOrDefault(constant, 0);
        return constant == Constant.FABLED ? Math.max(0, stored) : Math.max(0, total - stored);
    }

    private static ValueResult itemStat(ProfileData data, String field, String type, int total) {
        int count;          // self-caught (fished up), all-time
        int caughtOn;       // dry-streak anchor (all-time fish count at last catch)
        int questCount = -1; // -1 = no quest source for this item type
        switch (field) {
            case "armorShard", "shard" -> {
                count = data.allShardCaughtCount;
                caughtOn = data.shardDryStreak;
                questCount = data.allShardsFromQuests;
            }
            case "craftingComponent" -> {
                count = data.allLightningBottleCount + data.allInfusionCapsuleCount;
                caughtOn = Math.max(data.lightningBottleDryStreak, data.infusionCapsuleDryStreak);
            }
            case "lightningBottle", "lightning_bottle" -> { count = data.allLightningBottleCount; caughtOn = data.lightningBottleDryStreak; }
            case "infusionCapsule", "infusion_capsule" -> { count = data.allInfusionCapsuleCount; caughtOn = data.infusionCapsuleDryStreak; }
            default -> { return ValueResult.absent(); }
        }
        return switch (type) {
            case "count" -> ValueResult.str(String.valueOf(count));
            case "dry_streak" -> ValueResult.str(String.valueOf(Math.max(0, total - caughtOn)));
            case "quest" -> questCount < 0 ? ValueResult.absent() : ValueResult.str(String.valueOf(questCount));
            case "both" -> questCount < 0 ? ValueResult.absent() : ValueResult.str(String.valueOf(count + questCount));
            default -> ValueResult.absent();
        };
    }

    /**
     * FoE-R's constant labels, e.g. {@code %constant_data.data.fish.rarity.common%} → the rarity
     * glyph, {@code …fish.size.gigantic%} → the coloured size label. Resolves the last path segment
     * against {@link Constant} (its ids are globally unique) and returns the styled tag component.
     */
    private static ValueResult constantData(String[] params) {
        if (params.length < 2 || !params[0].equals("data")) {
            return ValueResult.absent();
        }
        String field = params[params.length - 1];
        Constant constant = Constant.valueOfId(field);
        if (constant == Constant.DEFAULT && !field.equalsIgnoreCase("default")) {
            return ValueResult.absent();
        }
        return ValueResult.comp(constant.TAG);
    }
    // endregion

    private static String direction(float yaw) {
        int index = Math.floorMod(Math.round(yaw / 90f), 4);
        return switch (index) {
            case 0 -> "S";
            case 1 -> "W";
            case 2 -> "N";
            default -> "E";
        };
    }
}
