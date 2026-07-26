package io.github.foecollab.handler;

import io.github.foecollab.FOECollab;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.Objects;

public class LoadingHandler {
    private static LoadingHandler INSTANCE = new LoadingHandler();

    public boolean isLoadingDone = false;
    public boolean isOnServer = false;
    public boolean wasOnServer = false;

    public static LoadingHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new LoadingHandler();
        }
        return INSTANCE;
    }

    public void init() {
        isLoadingDone = false;
    }

    public void tick(MinecraftClient minecraftClient) {
        if(minecraftClient.player != null && !isLoadingDone) {
            ItemStack stack = minecraftClient.player.getInventory().getMainStacks().getFirst();
            if(!stack.isEmpty() && stack.get(DataComponentTypes.CUSTOM_DATA) != null && stack.getItem() == Items.FISHING_ROD) {
                isLoadingDone = true;

                FOECollab.LOGGER.info("[FoE] Loading Done");

                PetEquipHandler.instance().startScanTime = System.currentTimeMillis();

                
            }
        }
    }

    public boolean checkAddress(MinecraftClient minecraftClient) {
        return Objects.requireNonNull(minecraftClient.getCurrentServerEntry()).address.toLowerCase().contains("fishonmc.net");
    }
}
