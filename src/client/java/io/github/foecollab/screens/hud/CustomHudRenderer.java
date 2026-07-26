package io.github.foecollab.screens.hud;

import io.github.foecollab.common.HudFont;
import io.github.foecollab.common.Theming;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.config.HudAlignment;
import io.github.foecollab.customhud.PlaceholderEngine;
import io.github.foecollab.customhud.PlaceholderEngine.ParseResult;
import io.github.foecollab.handler.CustomHudHandler;
import io.github.foecollab.handler.CustomHudHandler.CustomHud;
import io.github.foecollab.handler.ThemingHandler;
import io.github.foecollab.util.ThrottledCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Renders all enabled custom HUDs in FoE's HUD style — a translucent box with the resolved lines —
 * using the same layout maths as the built-in trackers so they sit and align consistently. The
 * expensive part (resolving each line's custom codes via {@link PlaceholderEngine} + recolouring)
 * is throttled to a few times a second; per-frame work is just the box + text layout.
 */
public class CustomHudRenderer {
    private record Resolved(CustomHud hud, List<Text> lines) {
    }

    private final ThrottledCache<List<Resolved>> cache = new ThrottledCache<>(200L, CustomHudRenderer::resolveAll);

    public void render(DrawContext drawContext, MinecraftClient client) {
        List<Resolved> resolvedHuds = cache.get();
        if (resolvedHuds.isEmpty()) {
            return;
        }
        for (Resolved resolved : resolvedHuds) {
            renderHud(drawContext, client, resolved.hud(), resolved.lines());
        }
    }

    private static List<Resolved> resolveAll() {
        List<Resolved> out = new ArrayList<>();
        for (CustomHud hud : CustomHudHandler.instance().getHuds()) {
            if (!hud.enabled) {
                continue;
            }
            List<Text> lines = resolveHudLines(hud);
            if (!lines.isEmpty()) {
                out.add(new Resolved(hud, lines));
            }
        }
        return out;
    }

    /**
     * Resolves one HUD's lines to display components: each line's custom codes are parsed and
     * recoloured, lines whose placeholders didn't all resolve are dropped (a false condition / missing
     * data — the line-filter mechanism), and a resolved-but-blank line is kept as a spacer, matching
     * FOE-R. Returns an empty list when the HUD has no visible content. Shared by the live renderer
     * and the Move-HUD editor (so a HUD is sized/positioned there exactly as it renders in game).
     */
    public static List<Text> resolveHudLines(CustomHud hud) {
        if (hud.lines == null || hud.lines.isEmpty()) {
            return List.of();
        }
        List<Text> lines = new ArrayList<>();
        boolean hasData = false;
        for (String template : hud.lines) {
            if (template == null) {
                continue;
            }
            ParseResult parsed = PlaceholderEngine.parse(template.replace("&", "§"));
            if (parsed.complete()) {
                lines.add(parsed.text());
                if (!parsed.text().getString().isBlank()) {
                    hasData = true;
                }
            }
        }
        return hasData ? HudFont.recolorAll(lines) : List.of();
    }

    private void renderHud(DrawContext drawContext, MinecraftClient client, CustomHud hud, List<Text> textList) {
        TextRenderer textRenderer = client.textRenderer;

        drawContext.getMatrices().pushMatrix();
        try {
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();

            HudAlignment alignment = hud.alignment == null ? HudAlignment.LEFT : hud.alignment;
            boolean center = alignment == HudAlignment.CENTER;
            boolean left = alignment == HudAlignment.LEFT;

            // hudX is the anchor's position from the screen's left edge for every alignment;
            // alignment only picks which point of the box sits there, so changing alignment keeps
            // the HUD in place instead of jumping.
            float xPercent = hud.hudX / 100f;
            float yPercent = hud.hudY / 100f;

            int baseX = (int) (screenWidth * xPercent);
            int baseY = (int) (screenHeight * yPercent);

            float scale = Math.max(1, hud.fontSize) / 10.0f;
            drawContext.getMatrices().scale(scale, scale);

            int alphaInt = (int) ((Math.max(0, Math.min(100, hud.backgroundOpacity)) / 100f) * 255f) << 24;

            int lineSpacing = 2;
            int lineHeight = (int) (textRenderer.fontHeight + (lineSpacing / scale));
            int scaledX = (int) (baseX / scale);
            int scaledY = (int) (baseY / scale);
            int padding = 8;
            AtomicInteger count = new AtomicInteger(0);

            int maxLength = textList.stream().map(textRenderer::getWidth).max(Integer::compareTo).orElse(0);
            // Flush model (no top reserve), unlike the built-in trackers, so a custom HUD can sit right
            // at the screen top/bottom. The Move-HUD drag box matches via MovableBoxWidget#topAnchored.
            int heightClampTranslation = (int) ((padding * 2 + textList.size() * lineHeight) * yPercent);

            int contentLeft;
            if (center) {
                contentLeft = scaledX - maxLength / 2;
            } else if (left) {
                contentLeft = scaledX + padding;
            } else {
                contentLeft = scaledX - padding - maxLength;
            }
            int contentRight = contentLeft + maxLength;
            int boxLeft = contentLeft - padding;
            int boxRight = contentRight + padding;

            if (alphaInt != 0) {
                drawContext.fill(boxLeft, scaledY - heightClampTranslation, boxRight,
                        scaledY + ((textList.size() - 1) * lineHeight) + padding * 3 - heightClampTranslation, alphaInt);
            }

            // Theming — draw the same 9-slice frame the built-in HUDs use so a chosen theme applies
            // to custom HUDs too. Custom HUDs already carry their own header line, so no title strip.
            FOEConfig config = FOEConfig.getConfig();
            if (config.theme.themeType != Theming.ThemeType.OFF) {
                Theming theme = ThemingHandler.instance().currentTheme;
                int colorOverlay = config.theme.colorOverlay;
                int alphaOverlay = (int) ((config.theme.opacity / 100f) * 255f) << 24;
                int bottomY = scaledY + padding * 2 + ((textList.size() - 1) * lineHeight) - heightClampTranslation;
                int topY = scaledY - padding - heightClampTranslation;
                int sideHeight = ((textList.size() - 1) * lineHeight) + padding;

                // Corners
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, theme.GUI_TOP_LEFT, boxLeft - padding, topY, 16, 16, alphaOverlay | colorOverlay);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, theme.GUI_TOP_RIGHT, boxRight - padding, topY, 16, 16, alphaOverlay | colorOverlay);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, theme.GUI_BOTTOM_LEFT, boxLeft - padding, bottomY, 16, 16, alphaOverlay | colorOverlay);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, theme.GUI_BOTTOM_RIGHT, boxRight - padding, bottomY, 16, 16, alphaOverlay | colorOverlay);

                // Sides
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, theme.GUI_LEFT, boxLeft - padding, scaledY + padding - heightClampTranslation, 16, sideHeight, alphaOverlay | colorOverlay);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, theme.GUI_RIGHT, boxRight - padding, scaledY + padding - heightClampTranslation, 16, sideHeight, alphaOverlay | colorOverlay);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, theme.GUI_TOP, contentLeft, topY, maxLength, 16, alphaOverlay | colorOverlay);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, theme.GUI_BOTTOM, contentLeft, bottomY, maxLength, 16, alphaOverlay | colorOverlay);
            }

            int finalHeightClampTranslation = heightClampTranslation;
            textList.forEach(text -> {
                int textWidth = textRenderer.getWidth(text);
                int x = center ? contentLeft + (maxLength - textWidth) / 2 : left ? contentLeft : contentRight - textWidth;
                drawContext.drawText(textRenderer, text,
                        x, scaledY + (count.getAndIncrement() * lineHeight) + padding - finalHeightClampTranslation,
                        0xFFFFFFFF, true);
            });
        } finally {
            drawContext.getMatrices().popMatrix();
        }
    }
}
