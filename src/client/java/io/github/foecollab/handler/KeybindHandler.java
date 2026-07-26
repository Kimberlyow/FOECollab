package io.github.foecollab.handler;

import io.github.foecollab.FOECollab;
import io.github.foecollab.mixin.KeyBindingAccessor;
import io.github.foecollab.screens.main.MainScreen;
import io.github.foecollab.util.AdvancedKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class KeybindHandler {
	// NOTE: CATEGORY must be initialized BEFORE INSTANCE. INSTANCE = new KeybindHandler() constructs the
	// keybinding instance fields, which capture CATEGORY; static fields initialize in source order, so if
	// INSTANCE came first CATEGORY would still be null and the keybindings would register with a null
	// category — which crashes 1.21.11's controls screen (KeyBindsList builds a CategoryEntry from each
	// binding's getCategory()).
	public static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifier.of(FOECollab.MOD_ID, "general"));

	private static KeybindHandler INSTANCE = new KeybindHandler();

	public final AdvancedKeyBinding openConfigKeybind = new AdvancedKeyBinding("key.foecollab.openconfig",
			GLFW.GLFW_KEY_O, CATEGORY);
	public final AdvancedKeyBinding openExtraInfoKeybind = new AdvancedKeyBinding("key.foecollab.openextrainfo",
			GLFW.GLFW_KEY_Z, CATEGORY);
	public final AdvancedKeyBinding baitSortingHelper = new AdvancedKeyBinding("key.foecollab.baitsortinghelper",
			GLFW.GLFW_KEY_B, CATEGORY);

	public boolean showExtraInfo = false;
	public boolean visualizeBaitSorting = false;

	public static KeybindHandler instance() {
		if (INSTANCE == null) {
			INSTANCE = new KeybindHandler();
		}
		return INSTANCE;
	}

	public void init() {
		KeybindHandler.register(
				this.openConfigKeybind,
				this.openExtraInfoKeybind,
				this.baitSortingHelper);
	}

	public void tick(MinecraftClient minecraftClient) {
		this.openConfigKeybind.onPressed(
				() -> minecraftClient.setScreen(new MainScreen(minecraftClient, minecraftClient.currentScreen)));

		if (minecraftClient.currentScreen != null) {
			this.showExtraInfo = InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(),
					((KeyBindingAccessor) openExtraInfoKeybind).getBoundKey().getCode());

			// Only meaningful in keybind-hold mode; tracks whether the key is currently held.
			this.visualizeBaitSorting = InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(),
					((KeyBindingAccessor) baitSortingHelper).getBoundKey().getCode());
		}
	}

	private static void register(KeyBinding... keyBindings) {
		for (KeyBinding keyBinding : keyBindings) {
			KeyBindingHelper.registerKeyBinding(keyBinding);
		}
	}
}
