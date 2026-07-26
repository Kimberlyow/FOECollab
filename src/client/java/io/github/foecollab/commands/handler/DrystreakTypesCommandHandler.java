package io.github.foecollab.commands.handler;

import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.handler.ProfileDataHandler;
import io.github.foecollab.util.TextHelper;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class DrystreakTypesCommandHandler {

    private DrystreakTypesCommandHandler() {
    }

    private static ProfileDataHandler.ProfileData profileData = ProfileDataHandler.instance().profileData;
    private static int allFishCaught = profileData.allFishCaughtCount;

    public static List<Text> getDryStreakBreakdown(String type) {
        List<Text> breakdownList = new ArrayList<>(List.of(TextHelper.concat(Text.literal("Drystreak Tracker:\n"))));
        switch (type.toLowerCase()) {
            case "all":
                breakdownList.addAll(getSizeBreakdown("sizes"));
                breakdownList.add(Text.literal("\n"));
                breakdownList.addAll(getRarityBreakdown("rarities"));
                breakdownList.add(Text.literal("\n"));
                breakdownList.addAll(getVariantBreakdown("variants"));
                breakdownList.add(Text.literal("\n"));
                breakdownList.add(Text.literal("─ ʀᴀʀᴇ ᴅʀᴏᴘs: \n"));
                breakdownList.addAll(getPetAndRareDropBreakdown("pet"));
                breakdownList.add(Text.literal("\n"));
                breakdownList.addAll(getPetAndRareDropBreakdown("shard"));
                breakdownList.add(Text.literal("\n"));
                breakdownList.addAll(getPetAndRareDropBreakdown("infusioncapsule"));
                breakdownList.add(Text.literal("\n"));
                breakdownList.addAll(getPetAndRareDropBreakdown("lightningbottle"));
                break;
            case "sizes", "baby", "juvenile", "adult", "large", "gigantic":
                breakdownList.addAll(getSizeBreakdown(type));
                break;
            case "rarities", "common", "rare", "epic", "legendary", "mythical":
                breakdownList.addAll(getRarityBreakdown(type));
                break;
            case "variants", "albino", "melanistic", "trophy", "fabled":
                breakdownList.addAll(getVariantBreakdown(type));
                break;
            case "pet", "shard", "infusioncapsule", "lightningbottle":
                breakdownList.addAll(getPetAndRareDropBreakdown(type));
                break;
            default:
                breakdownList.add(Text.literal("Invalid drystreak type specified."));
                break;
        }
        return breakdownList;
    }

    private static List<Text> getRarityBreakdown(String rarity) {
        switch (rarity.toLowerCase()) {
            case "rarities":
                return 
                List.of(TextHelper.concat(
                    Text.literal("─ ʀᴀʀɪᴛɪᴇs: \n"),
                    Text.literal("└ "), Constant.COMMON.TAG, Text.literal(" "), Text.literal(TextHelper.fmt(allFishCaught - profileData.rarityDryStreak.getOrDefault(Constant.COMMON, 0))), Text.literal("\n"),
                    Text.literal("└ "), Constant.RARE.TAG, Text.literal(" "), Text.literal(TextHelper.fmt(allFishCaught - profileData.rarityDryStreak.getOrDefault(Constant.RARE, 0))), Text.literal("\n"),
                    Text.literal("└ "), Constant.EPIC.TAG, Text.literal(" "), Text.literal(TextHelper.fmt(allFishCaught - profileData.rarityDryStreak.getOrDefault(Constant.EPIC, 0))), Text.literal("\n"),
                    Text.literal("└ "), Constant.LEGENDARY.TAG, Text.literal(" "), Text.literal(TextHelper.fmt(allFishCaught - profileData.rarityDryStreak.getOrDefault(Constant.LEGENDARY, 0))), Text.literal("\n"),
                    Text.literal("└ "), Constant.MYTHICAL.TAG, Text.literal(" "), Text.literal(TextHelper.fmt(allFishCaught - profileData.rarityDryStreak.getOrDefault(Constant.MYTHICAL, 0)))));
            default:
                Constant rarityConstant = Constant.valueOf(rarity.toUpperCase());
                return List.of(TextHelper.concat(
                    Text.literal("└ "), rarityConstant.TAG, Text.literal(" "), Text.literal(TextHelper.fmt(allFishCaught - profileData.rarityDryStreak.getOrDefault(rarityConstant, 0)))));
        }
    }

    private static List<Text> getSizeBreakdown(String size) {
        switch (size.toLowerCase()) {
            case "sizes":
                return 
                List.of(TextHelper.concat(
                    Text.literal("─ sɪᴢᴇs: \n"),
                    Text.literal("└ "), Constant.BABY.TAG, Text.literal(" "), Text.literal(TextHelper.fmt(allFishCaught - profileData.fishSizeDryStreak.getOrDefault(Constant.BABY, 0))), Text.literal("\n"),
                    Text.literal("└ "), Constant.JUVENILE.TAG, Text.literal(" "), Text.literal(TextHelper.fmt(allFishCaught - profileData.fishSizeDryStreak.getOrDefault(Constant.JUVENILE, 0))), Text.literal("\n"),
                    Text.literal("└ "), Constant.ADULT.TAG, Text.literal(" "), Text.literal(TextHelper.fmt(allFishCaught - profileData.fishSizeDryStreak.getOrDefault(Constant.ADULT, 0))), Text.literal("\n"),
                    Text.literal("└ "), Constant.LARGE.TAG, Text.literal(" "), Text.literal(TextHelper.fmt(allFishCaught - profileData.fishSizeDryStreak.getOrDefault(Constant.LARGE, 0))), Text.literal("\n"),
                    Text.literal("└ "), Constant.GIGANTIC.TAG, Text.literal(" "), Text.literal(TextHelper.fmt(allFishCaught - profileData.fishSizeDryStreak.getOrDefault(Constant.GIGANTIC, 0)))));
            default:
                Constant sizeConstant = Constant.valueOf(size.toUpperCase());
                return List.of(TextHelper.concat(
                    Text.literal("└ "), sizeConstant.TAG, Text.literal(" "), Text.literal(TextHelper.fmt(allFishCaught - profileData.fishSizeDryStreak.getOrDefault(sizeConstant, 0)))));
        }
    }

    private static List<Text> getVariantBreakdown(String variant) {
        switch (variant.toLowerCase()) {
            case "variants":
                return 
                List.of(TextHelper.concat(
                    Text.literal("─ ᴠᴀʀɪᴀɴᴛs: \n"),
                    Text.literal("└ "), Constant.ALBINO.TAG, Text.literal(" "), Text.literal(TextHelper.fmt(allFishCaught - profileData.variantDryStreak.getOrDefault(Constant.ALBINO, 0))), Text.literal("\n"),
                    Text.literal("└ "), Constant.MELANISTIC.TAG, Text.literal(" "), Text.literal(TextHelper.fmt(allFishCaught - profileData.variantDryStreak.getOrDefault(Constant.MELANISTIC, 0))), Text.literal("\n"),
                    Text.literal("└ "), Constant.TROPHY.TAG, Text.literal(" "), Text.literal(TextHelper.fmt(allFishCaught - profileData.variantDryStreak.getOrDefault(Constant.TROPHY, 0))), Text.literal("\n"),
                    Text.literal("└ "), Constant.FABLED.TAG, Text.literal(" "), Text.literal(TextHelper.fmt(allFishCaught - profileData.variantDryStreak.getOrDefault(Constant.FABLED, 0)))));
            default:
                Constant variantConstant = Constant.valueOf(variant.toUpperCase());
                return List.of(TextHelper.concat(
                    Text.literal("└ "), variantConstant.TAG, Text.literal(" "), Text.literal(TextHelper.fmt(allFishCaught - profileData.variantDryStreak.getOrDefault(variantConstant, 0)))));
        }
    }

    private static List<Text> getPetAndRareDropBreakdown(String type) {
        switch (type.toLowerCase()) {
            case "pet":
                return 
                List.of(TextHelper.concat(
                    Text.literal("└ "), Constant.PET.TAG, Text.literal(" "), Text.literal(TextHelper.fmt(allFishCaught - profileData.petDryStreak))));
            case "shard":
                return 
                List.of(TextHelper.concat(
                    Text.literal("└ "), Constant.SHARD.TAG, Text.literal(" "), Text.literal(TextHelper.fmt(allFishCaught - profileData.shardDryStreak))));
            case "infusioncapsule":
                return 
                List.of(TextHelper.concat(
                    Text.literal("└ "), Constant.INFUSION_CAPSULE.TAG, Text.literal(" "), Text.literal(TextHelper.fmt(allFishCaught - profileData.infusionCapsuleDryStreak))));
            case "lightningbottle":
                return 
                List.of(TextHelper.concat(
                    Text.literal("└ "), Constant.LIGHTNING_BOTTLE.TAG, Text.literal(" "), Text.literal(TextHelper.fmt(allFishCaught - profileData.lightningBottleDryStreak))));
            default:
                return List.of(Text.literal("Invalid drystreak type specified."));
        }
    }
}