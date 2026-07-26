package io.github.foecollab.screens.hud;

import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.handler.ChummerHandler;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.Vec3d;

/**
 * Draws every tracked chummer's range as a solid translucent ring (a one-block-tall band at
 * the configured radius) in place of the server's particle circle, which
 * {@code ClientWorldMixin} suppresses while this is enabled. Registered once at mod init;
 * does nothing while no chummer is active.
 */
public class ChummerRangeRenderer {
    private static final int SEGMENTS = 96;
    private static final float BAND_HEIGHT = 1.0f;

    // Unit circle, computed once; per frame the segments only get scaled and translated.
    private static final float[] UNIT_COS = new float[SEGMENTS + 1];
    private static final float[] UNIT_SIN = new float[SEGMENTS + 1];

    static {
        for (int i = 0; i <= SEGMENTS; i++) {
            double angle = (Math.PI * 2) * i / SEGMENTS;
            UNIT_COS[i] = (float) Math.cos(angle);
            UNIT_SIN[i] = (float) Math.sin(angle);
        }
    }

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            FOEConfig config = FOEConfig.getConfig();
            if (!config.chummerTracker.solidRangeBar || !ChummerHandler.instance().isActive()) {
                return;
            }
            Vec3d camera = context.worldState().cameraRenderState.pos;
            int color = 0x73000000 | (config.chummerTracker.barColor & 0xFFFFFF);

            // Camera-relative vertices (see WorldRenderContext#consumers). The debug-quads layer
            // is position+color, translucent and cull-free, so the band shows from both sides.
            VertexConsumer buffer = context.consumers().getBuffer(RenderLayers.debugQuads());
            for (ChummerHandler.Active active : ChummerHandler.instance().actives()) {
                Vec3d center = active.pos;
                float radius = active.radius;
                float cx = (float) (center.x - camera.x);
                float cz = (float) (center.z - camera.z);
                float y0 = (float) (center.y - camera.y);
                float y1 = y0 + BAND_HEIGHT;

                for (int i = 0; i < SEGMENTS; i++) {
                    float x0 = cx + UNIT_COS[i] * radius;
                    float z0 = cz + UNIT_SIN[i] * radius;
                    float x1 = cx + UNIT_COS[i + 1] * radius;
                    float z1 = cz + UNIT_SIN[i + 1] * radius;

                    buffer.vertex(x0, y0, z0).color(color);
                    buffer.vertex(x0, y1, z0).color(color);
                    buffer.vertex(x1, y1, z1).color(color);
                    buffer.vertex(x1, y0, z1).color(color);
                }
            }
        });
    }
}
