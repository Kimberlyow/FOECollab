package io.github.foecollab.mixin;

import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/// Exposes {@link NbtComponent}'s internal {@code nbt} compound for read-only access.
/// The public {@link NbtComponent#copyNbt()} performs a full deep copy on every call,
/// which is wasteful on hot paths that only read a couple of fields and run per item
/// slot per frame. Callers that use this MUST NOT mutate the returned compound — it is
/// the component's live backing data, shared across every stack with the same component.
@Mixin(NbtComponent.class)
public interface NbtComponentAccessor {
    @Accessor("nbt")
    NbtCompound foecollab$getNbtView();
}
