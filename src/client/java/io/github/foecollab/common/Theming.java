package io.github.foecollab.common;

import io.github.foecollab.FOECollab;
import net.minecraft.util.Identifier;

public class Theming {
    public final Identifier GUI_TOP_LEFT;
    public final Identifier GUI_TOP_RIGHT;
    public final Identifier GUI_BOTTOM_LEFT;
    public final Identifier GUI_BOTTOM_RIGHT;

    public final Identifier GUI_TOP;
    public final Identifier GUI_BOTTOM;
    public final Identifier GUI_LEFT;
    public final Identifier GUI_RIGHT;

    public final Identifier GUI_TEXT_LEFT;
    public final Identifier GUI_TEXT_RIGHT;
    public final Identifier GUI_TEXT_MIDDLE;

    public Theming(ThemeType theme) {
        GUI_TOP_LEFT = Identifier.of(FOECollab.MOD_ID, "themes/" + theme.ID + "/container/top_left");
        GUI_TOP_RIGHT = Identifier.of(FOECollab.MOD_ID, "themes/" + theme.ID + "/container/top_right");
        GUI_BOTTOM_LEFT = Identifier.of(FOECollab.MOD_ID, "themes/" + theme.ID + "/container/bottom_left");
        GUI_BOTTOM_RIGHT = Identifier.of(FOECollab.MOD_ID, "themes/" + theme.ID + "/container/bottom_right");

        GUI_TOP = Identifier.of(FOECollab.MOD_ID, "themes/" + theme.ID + "/container/top");
        GUI_BOTTOM = Identifier.of(FOECollab.MOD_ID, "themes/" + theme.ID + "/container/bottom");
        GUI_LEFT = Identifier.of(FOECollab.MOD_ID, "themes/" + theme.ID + "/container/left");
        GUI_RIGHT = Identifier.of(FOECollab.MOD_ID, "themes/" + theme.ID + "/container/right");

        GUI_TEXT_LEFT = Identifier.of(FOECollab.MOD_ID, "themes/" + theme.ID + "/container/text_left");
        GUI_TEXT_RIGHT = Identifier.of(FOECollab.MOD_ID, "themes/" + theme.ID + "/container/text_right");
        GUI_TEXT_MIDDLE = Identifier.of(FOECollab.MOD_ID, "themes/" + theme.ID + "/container/text_middle");
    }

    public enum ThemeType {
        OFF("off", 0xFFFFFFFF),
        CLEAN_SQUARE("clean_square", 0xFFFFFFFF),
        CLEAN_ROUNDED("clean_rounded", 0xFFFFFFFF),
        CLEAN_SQUARE_THICK("clean_square_thick", 0xFFFFFFFF),
        CLEAN_ROUNDED_THICK("clean_rounded_thick", 0xFFFFFFFF);

        public final String ID;
        public final int TEXT_COLOR;
        ThemeType(String id, int color) {
            this.ID = id;
            this.TEXT_COLOR = color;
        }
    }
}
