package io.github.foecollab.screens.hud;

import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.handler.ChummerHandler;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.Vec3d;

/**
 * Draws every tracked chummer's range as a one block high translucent wall standing on the ground
 * ({@link ChummerRingGeometry}) in place of the server's particle circle, which
 * {@code ClientWorldMixin} suppresses while this is enabled. Registered once at mod init;
 * does nothing while no chummer is active.
 */
public class ChummerRangeRenderer {
    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            FOEConfig config = FOEConfig.getConfig();
            if (!config.chummerTracker.solidRangeBar || !ChummerHandler.instance().isActive()) {
                return;
            }
            Vec3d camera = context.worldState().cameraRenderState.pos;
            int color = ChummerRingGeometry.argb(config.chummerTracker.barColor);

            // Camera-relative vertices (see WorldRenderContext#consumers). The debug-quads layer
            // is position+color, translucent and cull-free, so the wall shows from both sides.
            // Only positions saved by the last scan are read here, nothing from the world.
            VertexConsumer buffer = context.consumers().getBuffer(RenderLayers.debugQuads());
            for (ChummerHandler.Active active : ChummerHandler.instance().actives()) {
                Vec3d center = active.pos;
                ChummerRingGeometry.emit((float) (center.x - camera.x), (float) (center.y - camera.y),
                        (float) (center.z - camera.z), (x, y, z) -> buffer.vertex(x, y, z).color(color));
            }
        });
    }
}
