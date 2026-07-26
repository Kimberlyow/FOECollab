package io.github.foecollab.handler;

import io.github.foecollab.FOECollab;
import io.github.foecollab.config.ConfigConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

public class DebugHelperHandler {
    private static DebugHelperHandler INSTANCE = new DebugHelperHandler();

    public boolean toggle = false;

    public static DebugHelperHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new DebugHelperHandler();
        }
        return INSTANCE;
    }

    public void logCurrentScreen() {
        if (!ConfigConstants.DEV) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        Screen screen = client != null ? client.currentScreen : null;

        if (screen == null) {
            FOECollab.LOGGER.info("Current screen: null");
            return;
        }

        String title = screen.getTitle() != null ? screen.getTitle().getString() : "null";
        FOECollab.LOGGER.info("Current screen: {} (title='{}')", screen.getClass().getSimpleName(), title);
    }
}
