package io.github.foecollab.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.foecollab.commands.argument.DrystreakTypesArgumentType;
import io.github.foecollab.commands.argument.FriendsCommandArgumentType;
import io.github.foecollab.commands.argument.PlayerArgumentType;
import io.github.foecollab.commands.handler.DrystreakTypesCommandHandler;
import io.github.foecollab.commands.handler.FriendsCommandHandler;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.handler.CrewHandler;
import io.github.foecollab.handler.EventHandler;
import io.github.foecollab.handler.OtherPlayerHandler;
import io.github.foecollab.handler.ProfileDataHandler;
import io.github.foecollab.util.TextHelper;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class CommandRegistry {
    public static void initialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerCommands(dispatcher));
    }

    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(command("foe")
                .then(command("config").executes(Command::config))
                .then(command("resetsession").executes(Command::resetSession))
                .then(command("resetdrystreak").executes(Command::resetDryStreak))
                .then(command("reload").executes(Command::reload))
                .then(command("nocrew").executes(Command::noCrew))
                .then(command("fabledend").executes(Command::fabledEnd))
                .then(command("resettimer").executes(Command::resetTimer))
                .then(command("cancelimport").executes(Command::cancelImport))
                .then(command("highlightuser").then(argument("username", PlayerArgumentType.getPlayerArgumentType()).executes(Command::highlightUser)))
                .then(command("stophighlight").executes(Command::stopHighlight))
                .then(command("immersionmode").executes(Command::immersionMode))
                .then(command("armorvisibility").executes(Command::armorVisibility))
                .then(command("fishcounter").executes(Command::fishCounter))
                .then(command("drystreak").then(argument("type", DrystreakTypesArgumentType.getDrystreakTypesArgumentType()).executes(Command::dryStreak)))
                // manual backup command
                .then(command("backup").executes(Command::backup))
                .then(command("friends")
                    .then(argument("type", FriendsCommandArgumentType.getFriendsCommandArgumentType())
                        .executes(Command::friends)
                        .then(argument("username", (PlayerArgumentType.getPlayerArgumentType())).executes(Command::friends))))
        );
    }

    private static class Command {
        private static int config(CommandContext<FabricClientCommandSource> context) {
            return executeCommand(() -> {
                MinecraftClient.getInstance().setScreen(AutoConfig.getConfigScreen(FOEConfig.class, MinecraftClient.getInstance().currentScreen).get());
            });
        }

        private static int resetSession(CommandContext<FabricClientCommandSource> context) {
            return executeCommand(context, "Session Fish Tracker reset", () -> ProfileDataHandler.instance().resetStats());
        }

        private static int resetDryStreak(CommandContext<FabricClientCommandSource> context) {
            return executeCommand(context, "Dry-streak reset", () -> ProfileDataHandler.instance().resetDryStreak());
        }

        private static int reload(CommandContext<FabricClientCommandSource> context) {
            return executeCommand(context, "Config/Stats Reloaded", () -> {
                ProfileDataHandler.instance().loadStats();
                AutoConfig.getConfigHolder(FOEConfig.class).load();
            });
        }

        private static int noCrew(CommandContext<FabricClientCommandSource> context) {
            return executeCommand(context, "Set to No Crew", () -> CrewHandler.instance().setNoCrew());
        }

        private static int fabledEnd(CommandContext<FabricClientCommandSource> context) {
            return executeCommand(context, "Fabled event ended (debug)", () -> EventHandler.instance().isFabledActive = false);
        }

        private static int resetTimer(CommandContext<FabricClientCommandSource> context) {
            return executeCommand(context, "Fish Timer reset", () -> ProfileDataHandler.instance().resetTimer());
        }

        private static int cancelImport(CommandContext<FabricClientCommandSource> context) {
            if(!ProfileDataHandler.instance().profileData.isStatsInitialized) {
                ProfileDataHandler.instance().profileData.isStatsInitialized = true;
                return sendFeedback(context, Text.literal("Removed import notification"));
            }
            return 1;
        }

        private static int highlightUser(CommandContext<FabricClientCommandSource> context) {
            PlayerListEntry playerListEntry = PlayerArgumentType.getPlayer(context, "username");
            if (playerListEntry != null && playerListEntry.getDisplayName() != null) {
                return executeCommand(context, List.of(
                        Text.literal("Highlighted "),
                        playerListEntry.getDisplayName(),
                        Text.literal(" for 5 minutes")
                ), () -> {
                    OtherPlayerHandler.instance().highlightedPlayer = playerListEntry;
                    OtherPlayerHandler.instance().highlightStartTime = System.currentTimeMillis();
                });
            }
            return sendFeedback(context, Text.literal("Could not find Player"));
        }

        private static int stopHighlight(CommandContext<FabricClientCommandSource> context) {
            return executeCommand(context, "Stopping Highlight", () -> {
                OtherPlayerHandler.instance().isHighlighted = false;
                OtherPlayerHandler.instance().highlightedPlayer = null;
            });
        }

        private static int immersionMode(CommandContext<FabricClientCommandSource> context) {
            return executeCommand(context, "Immersion Mode Toggled", () -> {
                FOEConfig config = FOEConfig.getConfig();
                config.fun.immersionMode = !config.fun.immersionMode;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
            });
        }

        private static int armorVisibility(CommandContext<FabricClientCommandSource> context) {
            return executeCommand(context, "Armor Visibility Toggled", () -> {
                FOEConfig config = FOEConfig.getConfig();
                config.fun.hideArmor = !config.fun.hideArmor;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
            });
        }

        private static int fishCounter(CommandContext<FabricClientCommandSource> context) {
            int totalCaught = ProfileDataHandler.instance().profileData.allFishCaughtCount;
            return executeCommand(context, "Total fish Caught: " + totalCaught, () -> {});
        }

        private static int dryStreak(CommandContext<FabricClientCommandSource> context) {
            String type = context.getArgument("type", String.class).toLowerCase();
            List<Text> breakdown = DrystreakTypesCommandHandler.getDryStreakBreakdown(type);
            return executeCommand(context, breakdown, () -> {});
        }

        // /foe backup - creates a timestamped backup of the stats file
        private static int backup(CommandContext<FabricClientCommandSource> context) {
            return executeCommand(context, "Stats backup created", () -> ProfileDataHandler.instance().createBackup());
        }

        private static int friends(CommandContext<FabricClientCommandSource> context) {
            String type = context.getArgument("type", String.class).toLowerCase();
            String username = hasArgument(context, "username")
                    ? context.getArgument("username", String.class)
                    : null;
            List<Text> response = FriendsCommandHandler.getFriendsCommandResponse(type, username);
            return executeCommand(context, response, () -> {});
        }
    }

    private static boolean hasArgument(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getNodes().stream().anyMatch(node -> node.getNode().getName().equals(name));
    }

    //region Command Builder
    private static LiteralArgumentBuilder<FabricClientCommandSource> command(String command) {
        return ClientCommandManager.literal(command);
    }

    private static int executeCommand(CommandContext<FabricClientCommandSource> context, List<Text> feedback, ExecuteCallback executeCallback) {
        return executeCommand(context, TextHelper.concat(feedback.toArray(new Text[]{})), executeCallback);
    }

    private static int executeCommand(CommandContext<FabricClientCommandSource> context, String feedback, ExecuteCallback executeCallback) {
        return executeCommand(context, Text.literal(feedback), executeCallback);
    }

    private static int executeCommand(ExecuteCallback executeCallback) {
        executeCallback.execute();
        return 1;
    }

    private static int executeCommand(CommandContext<FabricClientCommandSource> context, Text feedback, ExecuteCallback executeCallback) {
        executeCallback.execute();
        return sendFeedback(context, feedback);
    }

    private static int sendFeedback(CommandContext<FabricClientCommandSource> context, Text feedback) {
        context.getSource().sendFeedback(
                TextHelper.concat(
                        Text.literal("FoE ").formatted(Formatting.DARK_GREEN, Formatting.BOLD),
                        Text.literal("» ").formatted(Formatting.DARK_GRAY),
                        feedback
                )

        );
        return 1;
    }

    private interface ExecuteCallback {
        void execute();
    }
    //endregion
}
