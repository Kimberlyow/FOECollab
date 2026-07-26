package io.github.foecollab.commands.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;

import java.util.concurrent.CompletableFuture;

public class FriendsCommandArgumentType implements ArgumentType<String> {

    private FriendsCommandArgumentType() {
    }

    public static FriendsCommandArgumentType getFriendsCommandArgumentType() {
        return new FriendsCommandArgumentType();
    }

    @Override
    public String parse(final StringReader reader) throws CommandSyntaxException {
        return reader.readString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(
                new String[]{
                        "add",
                        "remove",
                        "list"
                },
                builder
        );
    }
}
