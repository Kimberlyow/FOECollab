package io.github.foecollab.handler;

import io.github.foecollab.FOMC.Types.FOMCItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class LookTickHandler {
    private static LookTickHandler INSTANCE = new LookTickHandler();

    public ItemStack targetedItemInItemFrame = null;

    public static LookTickHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new LookTickHandler();
        }
        return INSTANCE;
    }

    public void tick() {
        HitResult hitResult = RayTracingHandler.instance().getTarget();

        if(hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) hitResult).getEntity();

            if (entity instanceof ItemFrameEntity itemFrame) {
                ItemStack itemStack = itemFrame.getHeldItemStack();
                targetedItemInItemFrame = FOMCItem.isFOMCItem(itemStack) ? itemStack : null;
            }
        } else {
            targetedItemInItemFrame = null;
        }
    }
}
