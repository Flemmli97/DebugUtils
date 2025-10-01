package io.github.flemmli97.debugutils.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.flemmli97.debugutils.DebugUtils;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClientDebugCommands {

    public static final SuggestionProvider<SharedSuggestionProvider> PROVIDER = SuggestionProviders.register(ResourceLocation.fromNamespaceAndPath(DebugUtils.MODID, "client_toggles"), ClientDebugCommands::getToggles);

    public static <T extends SharedSuggestionProvider> void register(CommandDispatcher<T> dispatcher,
                                                                     Function<String, LiteralArgumentBuilder<T>> literalBuilder,
                                                                     ArgumentBuildHelper<T> argumentBuilder,
                                                                     BiConsumer<T, Component> resultConsumer) {
        dispatcher.register(literalBuilder.apply(DebugUtils.MODID + "_client")
                .then(argumentBuilder.argument("module", ResourceLocationArgument.id()).suggests(SuggestionProviders.cast(PROVIDER))
                        .then(argumentBuilder.argument("on", BoolArgumentType.bool())
                                .executes(ctx -> ClientDebugCommands.toggle(ctx, resultConsumer))))
                .then(literalBuilder.apply("off").executes(ctx -> ClientDebugCommands.toggleOff(ctx, resultConsumer))));
    }

    private static <T extends SharedSuggestionProvider> int toggle(CommandContext<T> context, BiConsumer<T, Component> resultConsumer) {
        boolean on = BoolArgumentType.getBool(context, "on");
        ResourceLocation value = context.getArgument("module", ResourceLocation.class);
        DebugRenderHandler.toggle(value, on);
        resultConsumer.accept(context.getSource(), Component.translatable("debugutils.command.toggle." + (on ? "on" : "off") + ".self", value.toString()));
        return 1;
    }

    private static <T extends SharedSuggestionProvider> int toggleOff(CommandContext<T> context, BiConsumer<T, Component> resultConsumer) {
        DebugRenderHandler.toggleOff();
        resultConsumer.accept(context.getSource(), Component.translatable("debugutils.command.all.off.self"));
        return 1;
    }

    private static CompletableFuture<Suggestions> getToggles(CommandContext<?> context, SuggestionsBuilder build) {
        List<ResourceLocation> ids = new ArrayList<>(DebugRenderHandler.getClientHandlers());
        return suggestResource(ids, build);
    }

    private static void filterResources(Iterable<ResourceLocation> resources, String input, Consumer<ResourceLocation> resourceConsumer) {
        boolean bl = input.indexOf(58) > -1;
        for (ResourceLocation resource : resources) {
            if (bl) {
                String string = resource.toString();
                if (SharedSuggestionProvider.matchesSubStr(input, string)) {
                    resourceConsumer.accept(resource);
                }
            } else if (SharedSuggestionProvider.matchesSubStr(input, resource.getNamespace()) || resource.getNamespace().equals("minecraft") &&
                    (SharedSuggestionProvider.matchesSubStr(input, resource.getPath()) || SharedSuggestionProvider.matchesSubStr(input, resource.getPath().replace("debug/", "")))) {
                resourceConsumer.accept(resource);
            }
        }
    }

    private static CompletableFuture<Suggestions> suggestResource(Iterable<ResourceLocation> resources, SuggestionsBuilder builder) {
        String string = builder.getRemaining().toLowerCase(Locale.ROOT);
        filterResources(resources, string, res -> builder.suggest(res.toString()));
        return builder.buildFuture();
    }

    public interface ArgumentBuildHelper<P> {

        <T> RequiredArgumentBuilder<P, T> argument(String name, ArgumentType<T> type);
    }
}
