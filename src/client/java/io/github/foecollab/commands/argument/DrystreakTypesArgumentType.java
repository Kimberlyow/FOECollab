package io.github.foecollab.commands.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;

import java.util.concurrent.CompletableFuture;

public class DrystreakTypesArgumentType implements ArgumentType<String> {

    private DrystreakTypesArgumentType() {
    }

    public static DrystreakTypesArgumentType getDrystreakTypesArgumentType() {
        return new DrystreakTypesArgumentType();
    }

    @Override
    public String parse(final StringReader reader) throws CommandSyntaxException {
        return reader.readString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(
                new String[]{
                        "all", // everything
                        // sizes
                        "sizes", // all sizes
                        "baby",
                        "juvenile",
                        "adult",
                        "large",
                        "gigantic",
                        // rarities
                        "rarities", // all rarities
                        "common",
                        "rare",
                        "epic",
                        "legendary",
                        "mythical",
                        // variants
                        "variants", // all variants
                        "albino",
                        "melanistic",
                        "trophy",
                        "fabled",
                        // special types
                        // "event",
                        // pets & stuff
                        "shard",
                        "pet",
                        "infusioncapsule",
                        "lightningbottle"
                },
                builder
        );
    }
}
