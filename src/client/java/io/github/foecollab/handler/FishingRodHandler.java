package io.github.foecollab.handler;

import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.FOMC.LocationInfo;
import io.github.foecollab.FOMC.Types.Bait;
import io.github.foecollab.FOMC.Types.FOMCItem;
import io.github.foecollab.FOMC.Types.FishingRod;
import io.github.foecollab.FOMC.Types.Lure;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.mixin.InGameHudAccessor;
import io.github.foecollab.util.TextHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3f;

import java.util.*;

public class FishingRodHandler {
    private static FishingRodHandler INSTANCE = new FishingRodHandler();
    private final FOEConfig config = FOEConfig.getConfig();

    private ItemStack fishingRodStack = null;
    private Map<Integer, Integer> baitDisplay = new HashMap<>();

    private BlockPos previousPos = BlockPos.ofFloored(0, 0, 0);
    private BlockState previousBlockState = Blocks.AIR.getDefaultState();
    private boolean isBobberOut = false;

    public FishingRod fishingRod = null;
    public boolean isWrongBait = false;
    public boolean isWrongLure = false;
    public boolean isWrongPole = false;
    public boolean isWrongReel = false;
    public boolean isWrongLine = false;

    // Bobber waiting-time, surfaced to BobberTimerHud when shown as a HUD element rather than
    // floating over the bobber. Set each tick the player's own bobber is out; reset before each scan.
    public boolean showTimerHud = false;
    public float timerSeconds = 0f;

    // A catch leaves its action-bar message up for ~3s (60 ticks), which would hide the timer of a
    // quick recast. Snapshot the overlay's remaining ticks at cast time and count down alongside it:
    // while overlayRemaining stays at or below this, the overlay predates the cast and the timer may
    // show; the reel-in minigame sets a fresh overlay, jumping above it. See BobberTimerHud.
    public int staleOverlayTicks = 0;
    private boolean hadBobberOut = false;


    public static FishingRodHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new FishingRodHandler();
        }
        return INSTANCE;
    }

    public void tick(MinecraftClient minecraftClient) {
        boolean bobberOut = minecraftClient.player != null && minecraftClient.player.fishHook != null;
        if (bobberOut && !this.hadBobberOut) {
            this.staleOverlayTicks = ((InGameHudAccessor) minecraftClient.inGameHud).getOverlayRemaining();
        } else if (this.staleOverlayTicks > 0) {
            this.staleOverlayTicks--;
        }
        this.hadBobberOut = bobberOut;

        if(minecraftClient.player != null && minecraftClient.player.getInventory().getMainStacks().getFirst().getItem() == Items.FISHING_ROD) {
            ItemStack heldRod = minecraftClient.player.getInventory().getMainStacks().getFirst();
            // Compare against a *copy* of the rod we last parsed. The client updates the held
            // stack's components in place when the server decrements the equipped bait on a
            // catch, so storing the live reference would mean comparing it against itself
            // (always "equal") and the bait count would never refresh. ItemStack#equals is also
            // identity-only (Yarn warns against it), so use areEqual to detect the change.
            if(this.fishingRodStack == null || !ItemStack.areEqual(this.fishingRodStack, heldRod)) {
                this.fishingRodStack = heldRod.copy();
                FishingRod fishingRod = FishingRod.getFishingRod(heldRod);
                if(fishingRod != null) {
                    this.fishingRod = fishingRod;
                }
            }
        }

        if(this.fishingRod != null) {
            Constant locationWater = LocationInfo.valueOfId(BossBarHandler.instance().currentLocation.ID).WATER;

            // The equipped bait/lure now lives in its own activeBait slot, not the tacklebox, so the
            // wrong-location warning reads from there. Bait sitting unequipped in the tacklebox isn't
            // what's being fished with, so it shouldn't trigger the warning (mirrors the bait HUD).
            FOMCItem activeBait = this.fishingRod.getActiveBaitItem();
            if (activeBait instanceof Bait bait && bait.water != Constant.ANY_WATER) {
                this.isWrongBait = bait.water != locationWater;
                this.isWrongLure = false;
            } else if (activeBait instanceof Lure lure && lure.water != Constant.ANY_WATER) {
                this.isWrongLure = lure.water != locationWater;
                this.isWrongBait = false;
            } else {
                this.isWrongBait = false;
                this.isWrongLure = false;
            }

            if(this.fishingRod.reel != null && this.fishingRod.reel.water != Constant.GLOBAL_WATER) {
                this.isWrongReel = this.fishingRod.reel.water != LocationInfo.valueOfId(BossBarHandler.instance().currentLocation.ID).WATER;
            } else {
                this.isWrongReel = false;
            }

            if(this.fishingRod.pole != null && this.fishingRod.pole.water != Constant.GLOBAL_WATER) {
                this.isWrongPole = this.fishingRod.pole.water != LocationInfo.valueOfId(BossBarHandler.instance().currentLocation.ID).WATER;
            } else {
                this.isWrongPole = false;
            }

            if(this.fishingRod.line != null && this.fishingRod.line.water != Constant.GLOBAL_WATER) {
                this.isWrongLine = this.fishingRod.line.water != LocationInfo.valueOfId(BossBarHandler.instance().currentLocation.ID).WATER;
            } else {
                this.isWrongLine = false;
            }
        }

        List<Integer> entityToRemove = new ArrayList<>();
        baitDisplay.forEach((entity, bait) -> {
            if(minecraftClient.world != null) {
                Entity bobberEntity = minecraftClient.world.getEntityById(entity);
                if(bobberEntity != null) {
                    Entity baitEntity = minecraftClient.world.getEntityById(bait);
                    if (baitEntity != null) baitEntity.setPosition(bobberEntity.getEntityPos().add(0, -0.32, 0));
                } else {
                    entityToRemove.add(entity);
                }
            }
        });
        entityToRemove.forEach(id -> {
            if (minecraftClient.world != null) {
                minecraftClient.world.removeEntity(baitDisplay.get(id), Entity.RemovalReason.DISCARDED);
            }
            baitDisplay.remove(id);
        });
    }

    public boolean isTackleboxDisabled(MinecraftClient client) {
        if (client.player == null) return false;
        ItemStack rodStack = fishingRodStack;
        if (rodStack == null || rodStack.isEmpty()) {
            rodStack = client.player.getInventory().getMainStacks().stream()
                    .filter(stack -> !stack.isEmpty() && stack.getItem() == Items.FISHING_ROD && FishingRod.getFishingRod(stack) != null)
                    .findFirst()
                    .orElse(null);
        }
        if (rodStack == null) return false;
        return FishingRod.isTackleboxDisabled(rodStack);
    }

    public void tickEntities(Entity entity, MinecraftClient minecraftClient) {
        if(entity instanceof FishingBobberEntity fishingBobberEntity) {
            PlayerEntity player = fishingBobberEntity.getPlayerOwner();
            if(minecraftClient.player != null && player != null && Objects.equals(minecraftClient.player.getUuid(), player.getUuid())) {
                this.isBobberOut = true;
                List<Text> textList = new ArrayList<>();
                int remaining = ((InGameHudAccessor) minecraftClient.inGameHud).getOverlayRemaining();
                InGameHudAccessor inGameHudAccessor = ((InGameHudAccessor) minecraftClient.inGameHud);

                // Add Text
                if(config.bobberTracker.skyLightWarning
                        && minecraftClient.world != null
                        && (inGameHudAccessor.getTitle() == null || !Objects.equals(inGameHudAccessor.getTitle().getString(), "BITE!"))
                        && !MinecraftClient.getInstance().world.isSkyVisible(fishingBobberEntity.getBlockPos().up())
                        && minecraftClient.world.getBlockState(fishingBobberEntity.getBlockPos().up()).getBlock() != Blocks.WATER
                        && remaining <= 0
                ) this.addText(textList, Text.literal("ʙᴏʙʙᴇʀ ᴜɴᴅᴇʀ ᴀ ʙʟᴏᴄᴋ").formatted(Formatting.RED));

                if (config.bobberTracker.showWaitingTime) {
                    float seconds = fishingBobberEntity.age / 20f;
                    if (config.bobberTracker.timerAsHud) {
                        // Render as a fixed HUD element (see BobberTimerHud) instead of over the bobber.
                        this.showTimerHud = true;
                        this.timerSeconds = seconds;
                    } else {
                        Text timerText = Text.literal(String.format("%.1f", seconds)).withColor(config.bobberTracker.timerColor).formatted(Formatting.BOLD);
                        if (config.bobberTracker.timerSecondsSuffix) {
                            timerText = TextHelper.concat(timerText, Text.literal("s").formatted(Formatting.GRAY));
                        }
                        this.addText(textList, timerText);
                    }
                }

                if((config.fun.immersionMode || config.fun.biteBobber)
                        && inGameHudAccessor.getTitle() != null
                        && Objects.equals(inGameHudAccessor.getTitle().getString(), "BITE!")
                        && inGameHudAccessor.getTitleRemainTicks() > 0
                ) {
                    this.addText(textList, inGameHudAccessor.getTitle());
                }

                // Render Text
                if (config.fun.minigameOnBobber && remaining > 0) {
                    Text message = ((InGameHudAccessor) minecraftClient.inGameHud).getOverlayMessage();

                    fishingBobberEntity.setCustomName(message);
                    fishingBobberEntity.setCustomNameVisible(true);
                } else if(!textList.isEmpty()) {
                    Text concatText = TextHelper.concat(textList.toArray(new Text[0]));
                    fishingBobberEntity.setCustomName(concatText);
                    fishingBobberEntity.setCustomNameVisible(true);
                } else {
                    fishingBobberEntity.setCustomNameVisible(false);
                }

                if(config.fun.lightOnBobber) {
                    this.spawnLight(minecraftClient, fishingBobberEntity);
                }
            }

            // Bait Display
            if (player == null) return;
            // Once a bait display exists for this bobber, tick() keeps it positioned, so
            // there's nothing left to do here. Bail before the expensive rod-NBT parsing
            // below so a crowd of nearby bobbers doesn't re-parse every rod every tick.
            if (!config.bobberTracker.showBait || baitDisplay.containsKey(entity.getId())) return;

            ItemStack rodStack = player.getMainHandStack();
            boolean isOwnBobber = minecraftClient.player != null && Objects.equals(minecraftClient.player.getUuid(), player.getUuid());

            if (!isOwnBobber && FishingRod.isTackleboxDisabled(rodStack)) {
                return;
            }

            FishingRod rod = FishingRod.getFishingRod(rodStack);
            if (rod == null && isOwnBobber) {
                rodStack = fishingRodStack;
                rod = this.fishingRod;
            }
            if (rod == null) return;

            FOMCItem activeBait = rod.getActiveBaitItem();
            if(activeBait != null && !this.isTackleboxDisabled(minecraftClient)) {
                if (minecraftClient.world == null || minecraftClient.player == null) return;
                DisplayEntity.ItemDisplayEntity itemDisplayEntity = Objects.requireNonNull(EntityType.ITEM_DISPLAY.create(minecraftClient.world, SpawnReason.TRIGGERED));
                minecraftClient.world.addEntity(itemDisplayEntity);

                baitDisplay.put(entity.getId(), itemDisplayEntity.getId());

                ItemStack baitStack = Items.COOKED_COD.getDefaultStack().copy();
                baitStack.set(DataComponentTypes.CUSTOM_MODEL_DATA, activeBait instanceof Bait bait ?
                        bait.customModelData : activeBait instanceof Lure lure ? lure.customModelData : CustomModelDataComponent.DEFAULT);

                itemDisplayEntity.setItemStack(baitStack);
                itemDisplayEntity.setPosition(entity.getEntityPos().add(0, -0.32, 0));
                itemDisplayEntity.setBillboardMode(DisplayEntity.BillboardMode.VERTICAL);
                itemDisplayEntity.setTransformation(new AffineTransformation(null, null, new Vector3f(0.6f, 0.6f, 0.6f), null));
            }
        }
    }

    public void beforeTickEntitiess() {
        isBobberOut = false;
        showTimerHud = false;
    }

    public void afterTickEntities(MinecraftClient minecraftClient) {
        if(!isBobberOut) {
            if(minecraftClient.world != null && !Objects.equals(this.previousPos, BlockPos.ofFloored(0, 0, 0))) {
                minecraftClient.world.setBlockState(this.previousPos, this.previousBlockState);
                this.previousPos = BlockPos.ofFloored(0, 0, 0);
                this.previousBlockState = Blocks.AIR.getDefaultState();
            }
        }
    }

    private void spawnLight(MinecraftClient minecraftClient, FishingBobberEntity fishingBobberEntity) {
        if(fishingBobberEntity != null && minecraftClient.world != null && !Objects.equals(this.previousPos, BlockPos.ofFloored(fishingBobberEntity.getEntityPos().add(0, .40, 0)))) {
            minecraftClient.world.setBlockState(this.previousPos, this.previousBlockState);
            this.previousBlockState = minecraftClient.world.getBlockState(BlockPos.ofFloored(fishingBobberEntity.getEntityPos().add(0, .40, 0)));
            this.previousPos = BlockPos.ofFloored(fishingBobberEntity.getEntityPos().add(0, .40, 0));
            if(minecraftClient.world.getBlockState(this.previousPos).getBlock() == Blocks.WATER) {
                BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
                IntProperty LEVEL_15 = Properties.LEVEL_15;
                minecraftClient.world.setBlockState(this.previousPos, Blocks.LIGHT.getDefaultState().with(WATERLOGGED, Boolean.TRUE).with(LEVEL_15, config.fun.lightLevel));
            } else if(minecraftClient.world.getBlockState(this.previousPos).getBlock() == Blocks.AIR) {
                IntProperty LEVEL_15 = Properties.LEVEL_15;
                minecraftClient.world.setBlockState(this.previousPos, Blocks.LIGHT.getDefaultState().with(LEVEL_15, config.fun.lightLevel));
            }
        }
    }

    private void addText(List<Text> textList, Text text) {
        if(textList.isEmpty()) {
            textList.add(text);
        } else {
            textList.add(TextHelper.concat(Text.literal(" | ").formatted(Formatting.WHITE), text));
        }
    }
}
