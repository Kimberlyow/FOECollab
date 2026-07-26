package io.github.foecollab.handler;

import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.FOMC.LocationInfo;
import io.github.foecollab.FOMC.Types.Armor;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.util.ItemStackHelper;
import io.github.foecollab.util.TextHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Objects;

public class ArmorHandler {
    private static ArmorHandler INSTANCE = new ArmorHandler();
    private final FOEConfig config = FOEConfig.getConfig();

    public ItemStack currentChestplateItem = Items.AIR.getDefaultStack();
    public ItemStack currentLeggingsItem = Items.AIR.getDefaultStack();
    public ItemStack currentBootsItem = Items.AIR.getDefaultStack();
    public Armor currentChestplate = null;
    public Armor currentLeggings = null;
    public Armor currentBoots = null;
    public boolean isWrongChestplateClimate = false;
    public boolean isWrongLeggingsClimate = false;
    public boolean isWrongBootsClimate = false;

    public static ArmorHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ArmorHandler();
        }
        return INSTANCE;
    }

    public void tick(MinecraftClient minecraftClient) {
        if (minecraftClient.player == null) {
            return;
        }

        ItemStack bootsStack = minecraftClient.player.getEquippedStack(EquipmentSlot.FEET);
        if (!currentBootsItem.equals(bootsStack) && bootsStack.getItem() == Items.LEATHER_BOOTS) {
            Armor armor = Armor.getArmor(bootsStack);
            if (armor != null) {
                this.currentBootsItem = bootsStack;
                this.currentBoots = armor;
            }
        } else if (bootsStack.isEmpty()) {
            this.currentBootsItem = Items.AIR.getDefaultStack();
            this.currentBoots = null;
        }

        ItemStack leggingsStack = minecraftClient.player.getEquippedStack(EquipmentSlot.LEGS);
        if (!currentLeggingsItem.equals(leggingsStack) && leggingsStack.getItem() == Items.LEATHER_LEGGINGS) {
            Armor armor = Armor.getArmor(leggingsStack);
            if (armor != null) {
                this.currentLeggingsItem = leggingsStack;
                this.currentLeggings = armor;
            }
        } else if (leggingsStack.isEmpty()) {
            this.currentLeggingsItem = Items.AIR.getDefaultStack();
            this.currentLeggings = null;
        }

        ItemStack chestplateStack = minecraftClient.player.getEquippedStack(EquipmentSlot.CHEST);
        if (!currentChestplateItem.equals(chestplateStack) && chestplateStack.getItem() == Items.LEATHER_CHESTPLATE) {
            Armor armor = Armor.getArmor(chestplateStack);
            if (armor != null) {
                this.currentChestplateItem = chestplateStack;
                this.currentChestplate = armor;
            }
        } else if (chestplateStack.isEmpty()) {
            this.currentChestplateItem = Items.AIR.getDefaultStack();
            this.currentChestplate = null;
        }

        if (BossBarHandler.instance().currentLocation != Constant.DEFAULT
                && BossBarHandler.instance().currentLocation != Constant.CREW_ISLAND) {
            String currentLocationClimate = LocationInfo
                    .valueOfId(BossBarHandler.instance().currentLocation.ID).CLIMATE.ID;
            this.isWrongChestplateClimate = currentChestplate != null
                    && !Objects.equals(currentChestplate.climate.ID, currentLocationClimate);
            this.isWrongLeggingsClimate = currentLeggings != null
                    && !Objects.equals(currentLeggings.climate.ID, currentLocationClimate);
            this.isWrongBootsClimate = currentBoots != null
                    && !Objects.equals(currentBoots.climate.ID, currentLocationClimate);
        }
    }

    public void appendTooltip(List<Text> textList, ItemStack itemStack) {
        if (config.armorStatsTooltip.showOnRerollScreen)
            this.appendTooltipArmorRollScreen(textList, itemStack);
        if (config.armorStatsTooltip.showOnPressingKeybind)
            this.appendTooltipArmorRoll(textList, itemStack);
    }

    private void appendTooltipArmorRoll(List<Text> textList, ItemStack itemStack) {
        if (itemStack.getItem() == Items.LEATHER_CHESTPLATE || itemStack.getItem() == Items.LEATHER_BOOTS
                || itemStack.getItem() == Items.LEATHER_LEGGINGS) {
            if (KeybindHandler.instance().showExtraInfo) {
                int offsetSlot = 0;

                NbtCompound nbtCompound = ItemStackHelper.getNbt(itemStack);
                if (nbtCompound != null && nbtCompound.contains("renderInfo")) {
                    offsetSlot = 7;
                }

                Armor armor = Armor.getArmor(itemStack);
                if (armor != null && armor.identified) {
                    Text emptyLine = getTextRarity(armor.rarity).TAG;

                    int slot = textList.size() - (MinecraftClient.getInstance().options.advancedItemTooltips ? 9 : 7);
                    if (armor.armorBonuses.get(4).rolled)
                        insertArmorRollTooltip(slot - offsetSlot, 4, armor, emptyLine, textList);
                    if (armor.armorBonuses.get(3).rolled)
                        insertArmorRollTooltip(slot - offsetSlot, 3, armor, emptyLine, textList);
                    if (armor.armorBonuses.get(2).rolled)
                        insertArmorRollTooltip(slot - offsetSlot, 2, armor, emptyLine, textList);
                    if (armor.armorBonuses.get(1).rolled)
                        insertArmorRollTooltip(slot - offsetSlot, 1, armor, emptyLine, textList);
                    if (armor.armorBonuses.get(0).rolled)
                        insertArmorRollTooltip(slot - offsetSlot, 0, armor, emptyLine, textList);

                    // Who crafted/identified the piece (NBT "uuid"); the key is absent on
                    // some older pieces, and Armor.crafter is a random UUID in that case.
                    if (nbtCompound != null && nbtCompound.contains("uuid")) {
                        insertArmorCrafterTooltip(armor, emptyLine, textList);
                    }
                }
            }
        }
    }

    /// Inserts "ᴄʀᴀꜰᴛᴇᴅ ʙʏ: &lt;player&gt;" right under the quality line. The username
    /// resolves asynchronously; tooltips rebuild every frame, so a "loading" line
    /// swaps to the name as soon as the lookup lands.
    private void insertArmorCrafterTooltip(Armor armor, Text prefix, List<Text> textList) {
        for (int i = 0; i < textList.size(); i++) {
            // matches both ꞯᴜᴀʟɪᴛʏ and Qᴜᴀʟɪᴛʏ spellings
            if (textList.get(i).getString().contains("ᴜᴀʟɪᴛʏ")) {
                String username = ProfileNameHandler.instance().getUsername(armor.crafter);
                Text nameText = username == null
                        ? Text.literal("ʟᴏᴀᴅɪɴɢ...").formatted(Formatting.DARK_GRAY)
                        : username.isEmpty()
                                ? Text.literal("ᴜɴᴋɴᴏᴡɴ").formatted(Formatting.DARK_GRAY)
                                : Text.literal(username).formatted(Formatting.YELLOW);

                textList.add(i + 1, TextHelper.concat(
                        prefix,
                        Text.literal("    └ ᴄʀᴀꜰᴛᴇᴅ ʙʏ: ").formatted(Formatting.GRAY),
                        nameText));
                return;
            }
        }
    }

    private void insertArmorRollTooltip(int slot, int index, Armor armor, Text prefix, List<Text> textList) {
        if (armor.armorBonuses.get(index).rolled) {
            int amount = calculateMoneyRolls(armor.armorBonuses.get(index).rolls, armor.armorBonuses.get(index).tier);

            Text amountText = TextHelper.concat(
                    prefix,
                    Text.literal("    └ ʀᴇʀᴏʟʟѕ: ").formatted(Formatting.GRAY),
                    Text.literal(String.valueOf(armor.armorBonuses.get(index).rolls - 1)).formatted(Formatting.YELLOW),
                    Text.literal("x").formatted(Formatting.WHITE),
                    Text.literal(" | ᴛᴏᴛᴀʟ: ").formatted(Formatting.GRAY),
                    Text.literal("$").formatted(Formatting.GREEN),
                    Text.literal(TextHelper.fmnt(amount)).formatted(Formatting.GREEN));

            textList.add(slot + index, amountText);
        }
    }

    private void appendTooltipArmorRollScreen(List<Text> textList, ItemStack itemStack) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        boolean isEye = false;
        boolean isArmor = false;
        int slot = -1;
        Armor armor = null;
        Armor testArmor = null;

        if (minecraftClient.player != null) {
            testArmor = Armor.getArmor(minecraftClient.player.currentScreenHandler.getSlot(31).getStack());
        }

        for (int i = 0; i < (minecraftClient.player != null ? minecraftClient.player.currentScreenHandler.slots.size()
                : 0); i++) {
            ItemStack itemStackFromInv = minecraftClient.player.currentScreenHandler.getSlot(i).getStack();
            if (minecraftClient.player.currentScreenHandler.getSlot(i).inventory != minecraftClient.player
                    .getInventory()
                    && (itemStack.getItem() == Items.ENDER_EYE)
                    && itemStack.equals(itemStackFromInv)) {
                isEye = true;
                slot = i - 11;
            }

            if (minecraftClient.player.currentScreenHandler.getSlot(31).inventory != minecraftClient.player
                    .getInventory()
                    && testArmor != null
                    && isEye) {
                isArmor = true;
                armor = testArmor;
            }
        }

        if (isEye && isArmor && slot != -1) {
            int amount = calculateMoneyRolls(armor.armorBonuses.get(slot).rolls, armor.armorBonuses.get(slot).tier);
            Text amountText = TextHelper.concat(
                    textList.get(6).copy(),
                    Text.literal("  └ ʀᴇʀᴏʟʟѕ: ").formatted(Formatting.GRAY),
                    Text.literal(String.valueOf(armor.armorBonuses.get(slot).rolls - 1)).formatted(Formatting.YELLOW),
                    Text.literal("x").formatted(Formatting.WHITE),
                    Text.literal(" | ᴛᴏᴛᴀʟ: ").formatted(Formatting.GRAY),
                    Text.literal("$").formatted(Formatting.GREEN),
                    Text.literal(TextHelper.fmnt(amount)).formatted(Formatting.GREEN));

            textList.add(8, amountText);
        }
    }

    private static int calculateMoneyRolls(int rolls, int tier) {
        rolls = rolls - 1;

        int amount = 0;
        if (rolls > 0)
            amount += 100;
        if (rolls > 1)
            amount += 400;
        if (rolls > 2)
            amount += 900;
        if (rolls > 3)
            amount += 1600;
        if (rolls > 4)
            amount += 2500;
        if (rolls > 5)
            amount += 3600;
        if (rolls > 6)
            amount += 4900;
        if (rolls > 7)
            amount += 6400;
        if (rolls > 8)
            amount += 8100;
        if (rolls > 9)
            amount += 10000;
        if (rolls > 10)
            amount += 12100;
        if (rolls > 11)
            amount += 14400;
        if (rolls > 12)
            amount += 16900;
        if (rolls > 13)
            amount += 19600;
        if (rolls > 14)
            amount += 22500;

        // all tiers now cost the same as t1-3 used to so this can just be simplified
        if (rolls > 15) {
            int extraRolls = rolls - 15;
            amount = amount + extraRolls * 25000;
        }
        return amount;
    }

    private Constant getTextRarity(Constant rarity) {
        return switch (rarity) {
            case COMMON -> Constant.TEXTCOMMON;
            case RARE -> Constant.TEXTRARE;
            case EPIC -> Constant.TEXTEPIC;
            case LEGENDARY -> Constant.TEXTLEGENDARY;
            case MYTHICAL -> Constant.TEXTMYTHICAL;
            case SPECIAL -> Constant.TEXTSPECIAL;
            default -> Constant.TEXTDEFAULT;
        };
    }
}
