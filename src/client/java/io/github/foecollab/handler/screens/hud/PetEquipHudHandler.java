package io.github.foecollab.handler.screens.hud;

import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.FOMC.Types.Pet;
import io.github.foecollab.common.HudFont;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.config.TrackerPetEquipHUDConfig.PetEquipTracker.ActivePetHUDOptions;
import io.github.foecollab.handler.PetEquipHandler;
import io.github.foecollab.handler.ProfileDataHandler;
import io.github.foecollab.util.TextHelper;
import io.github.foecollab.util.ThrottledCache;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class PetEquipHudHandler {
    private static PetEquipHudHandler INSTANCE = new PetEquipHudHandler();

    private final ThrottledCache<List<PetRow>> petRowCache =
            new ThrottledCache<>(200L, this::buildPetRows);

    public static PetEquipHudHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new PetEquipHudHandler();
        }
        return INSTANCE;
    }

    private Formatting getProgressColor(double value) {
        return value <= 25 ? Formatting.RED
                : value <= 50 ? Formatting.GOLD : value <= 75 ? Formatting.YELLOW : Formatting.GREEN;
    }

    public List<PetRow> assemblePetRows() {
        return petRowCache.get();
    }

    /// Flat text view of the current rows (the bar row contributes its level label), used by the
    /// Move-HUD editor for sizing.
    public List<Text> assemblePetText() {
        List<PetRow> rows = petRowCache.get();
        List<Text> out = new ArrayList<>(rows.size());
        for (PetRow row : rows) {
            out.add(row.text());
        }
        return out;
    }

    private List<PetRow> buildPetRows() {
        ProfileDataHandler.ProfileData profileData = ProfileDataHandler.instance().profileData;
        ActivePetHUDOptions opts = FOEConfig.getConfig().petEquipTracker.activePetHUDOptions;

        List<PetRow> rows = new ArrayList<>();

        if (PetEquipHandler.instance().petStatus == PetEquipHandler.PetStatus.HAS_PET) {
            Text namePet = PetEquipHandler.instance().currentPetItem.getName();
            int level = profileData.equippedPet.lvl;
            float currentXp = profileData.equippedPet.currentXp;
            float neededXp = profileData.equippedPet.neededXp;
            float percentXp = neededXp > 0 ? currentXp / neededXp * 100f : 100f;
            float petPercent = profileData.equippedPet.percentPetRating * 100f;
            Constant ratingTag = Pet.getConstantFromPercent(profileData.equippedPet.percentPetRating);
            Text petItemText = resolvePetItemText(profileData.equippedPet.petItem);

            boolean barMode = opts.xpDisplayMode == XpDisplayMode.BAR;
            // The level label lives on the bar line only when there actually is a bar.
            boolean levelOnBar = barMode && opts.showXp && opts.showLevel;

            // Name line (+ level when it isn't being shown on the bar)
            rows.add(PetRow.text(buildNameLine(profileData.equippedPet.rarity, namePet, level,
                    opts.showLevel && !levelOnBar)));

            // XP — number line or progress bar
            if (opts.showXp) {
                if (barMode) {
                    float progress = level >= 100 || neededXp <= 0
                            ? 1f
                            : Math.max(0f, Math.min(1f, currentXp / neededXp));
                    // Tooltip-style XP bar: coloured by progress while levelling (the same red/gold/
                    // yellow/green tiers the old XP text used), teal once maxed (level 100), like in game.
                    int barColor;
                    if (level >= 100) {
                        barColor = 0xFF00EFB2;
                    } else {
                        Integer progressRgb = getProgressColor(percentXp).getColorValue();
                        barColor = progressRgb != null ? 0xFF000000 | progressRgb : 0xFFFFFFFF;
                    }
                    Text label = levelOnBar ? buildBarLevelLabel(level) : Text.empty();
                    rows.add(PetRow.bar(label, progress, barColor));
                } else {
                    rows.add(PetRow.text(buildXpTextLine(level, currentXp, neededXp, percentXp)));
                }
            }

            // Rating (text + tier color)
            if (opts.showRating) {
                rows.add(PetRow.text(TextHelper.concat(
                        Text.literal("ʀᴀᴛɪɴɢ ").formatted(Formatting.GRAY),
                        ratingTag.TAG,
                        Text.literal(" "),
                        Text.literal("(").formatted(Formatting.DARK_GRAY),
                        Text.literal(TextHelper.fmt(petPercent, 1) + "%").withColor(ratingTag.COLOR),
                        Text.literal(")").formatted(Formatting.DARK_GRAY))));
            }

            // Item
            if (opts.showItemLine && petItemText != null) {
                rows.add(PetRow.text(TextHelper.concat(
                        Text.literal("ɪᴛᴇᴍ ").formatted(Formatting.GRAY),
                        petItemText)));
            }
        } else if (PetEquipHandler.instance().petStatus == PetEquipHandler.PetStatus.NO_PET) {
            rows.add(PetRow.text(Text.literal("No pet equipped").formatted(Formatting.RED)));
        } else if (PetEquipHandler.instance().petStatus == PetEquipHandler.PetStatus.LOADING) {
            rows.add(PetRow.text(Text.literal("Loading").formatted(Formatting.RED)));
        }

        return applyTextPipeline(rows);
    }

    private Text buildNameLine(Constant rarity, Text namePet, int level, boolean includeLevel) {
        if (!includeLevel) {
            return TextHelper.concat(rarity.TAG, Text.literal(" "), namePet);
        }
        Formatting levelColor = getProgressColor(level);
        return TextHelper.concat(
                rarity.TAG,
                Text.literal(" "),
                namePet,
                Text.literal(" (").formatted(Formatting.DARK_GRAY),
                Text.literal("ʟᴠʟ ").formatted(Formatting.GRAY),
                Text.literal(String.valueOf(level)).formatted(levelColor),
                Text.literal(")").formatted(Formatting.DARK_GRAY));
    }

    private Text buildBarLevelLabel(int level) {
        Formatting levelColor = getProgressColor(level);
        return TextHelper.concat(
                Text.literal("ʟᴠʟ ").formatted(Formatting.GRAY),
                Text.literal(String.valueOf(level)).formatted(levelColor));
    }

    private Text buildXpTextLine(int level, float currentXp, float neededXp, float percentXp) {
        if (level >= 100) {
            return TextHelper.concat(
                    Text.literal("xᴘ ").formatted(Formatting.GRAY),
                    Text.literal("(").formatted(Formatting.DARK_GRAY),
                    Text.literal(TextHelper.fmnt(currentXp)).formatted(Formatting.AQUA),
                    Text.literal("/").formatted(Formatting.DARK_GRAY),
                    Text.literal("MAX").formatted(Formatting.BLUE),
                    Text.literal(") ").formatted(Formatting.DARK_GRAY),
                    Text.literal("100").formatted(Formatting.GREEN),
                    Text.literal("%").formatted(Formatting.GREEN));
        }
        Formatting percentColor = getProgressColor(percentXp);
        return TextHelper.concat(
                Text.literal("xᴘ ").formatted(Formatting.GRAY),
                Text.literal("(").formatted(Formatting.DARK_GRAY),
                Text.literal(TextHelper.fmnt(currentXp)).formatted(Formatting.AQUA),
                Text.literal("/").formatted(Formatting.DARK_GRAY),
                Text.literal(TextHelper.fmnt(neededXp)).formatted(Formatting.BLUE),
                Text.literal(") ").formatted(Formatting.DARK_GRAY),
                Text.literal(TextHelper.fmt(percentXp, 1)).formatted(percentColor),
                Text.literal("%").formatted(percentColor));
    }

    private Text resolvePetItemText(String petItemId) {
        if (petItemId == null) {
            return null;
        }
        Constant petItemConstant = Constant.valueOfId(petItemId);
        if (petItemConstant != Constant.DEFAULT || Constant.DEFAULT.ID.equals(petItemId)) {
            return petItemConstant.TAG;
        }
        return Text.literal(petItemId);
    }

    /// Runs the row texts through the Cleaner Display + theme recolour pipeline (both preserve list
    /// size/order), then rebuilds the rows so the bar row keeps its progress/colour.
    private List<PetRow> applyTextPipeline(List<PetRow> rows) {
        List<Text> texts = new ArrayList<>(rows.size());
        for (PetRow row : rows) {
            texts.add(row.text());
        }
        HudFont.applyCleanerDisplay(texts);
        List<Text> recolored = HudFont.recolorAll(texts);
        List<PetRow> out = new ArrayList<>(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            PetRow row = rows.get(i);
            Text processed = recolored.get(i);
            out.add(row.isBar() ? PetRow.bar(processed, row.barProgress(), row.barColor()) : PetRow.text(processed));
        }
        return out;
    }

    /// One pet HUD line: either a text line, or the XP progress bar carrying its level label.
    public record PetRow(Text text, boolean isBar, float barProgress, int barColor) {
        public static PetRow text(Text text) {
            return new PetRow(text, false, 0f, 0);
        }

        public static PetRow bar(Text label, float progress, int color) {
            return new PetRow(label, true, progress, color);
        }
    }

    public enum XpDisplayMode {
        TEXT,
        BAR
    }
}
