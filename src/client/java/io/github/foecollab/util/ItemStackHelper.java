package io.github.foecollab.util;

import com.mojang.serialization.DataResult;
import io.github.foecollab.mixin.NbtComponentAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;

public class ItemStackHelper {

    public static NbtCompound getNbt(ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        return component != null ? component.copyNbt() : null;
    }

    /// Read-only view of a stack's custom-data NBT that skips the deep copy
    /// {@link #getNbt} pays for. {@link NbtComponent#copyNbt()} clones the whole tree on
    /// every call; on per-slot-per-frame render paths that only read a field or two that
    /// copy is pure overhead. The returned compound is the component's live backing data
    /// — treat it as immutable and NEVER mutate it. Use {@link #getNbt} when you need a
    /// compound you can modify or hold onto.
    public static NbtCompound getNbtView(ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        return component != null ? ((NbtComponentAccessor) (Object) component).foecollab$getNbtView() : null;
    }

    /// Decodes a serialized item ({@code {id, count, components}}) straight from NBT.
    /// Decoding via NbtOps (instead of round-tripping through JSON) preserves NBT types
    /// — booleans, byte/int distinctions, int-arrays — so component-heavy stacks parse
    /// correctly. Returns {@link ItemStack#EMPTY} if the NBT can't be decoded.
    public static ItemStack nbtToItemStack(NbtCompound nbt) {
        if (nbt == null) return ItemStack.EMPTY;
        var world = MinecraftClient.getInstance().world;
        if (world == null) return ItemStack.EMPTY;

        DynamicRegistryManager registryManager = world.getRegistryManager();
        RegistryOps<NbtElement> ops = RegistryOps.of(NbtOps.INSTANCE, registryManager);
        DataResult<ItemStack> result = ItemStack.CODEC.parse(ops, nbt);
        return result.result().orElse(ItemStack.EMPTY);
    }
}
