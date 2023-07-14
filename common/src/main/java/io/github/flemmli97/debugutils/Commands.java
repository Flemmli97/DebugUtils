package io.github.flemmli97.debugutils;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class Commands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(net.minecraft.commands.Commands.literal(DebugUtils.MODID).requires(src -> src.hasPermission(2))
                .then(net.minecraft.commands.Commands.argument("module", ResourceLocationArgument.id()).suggests(Commands::getToggles)
                        .executes(Commands::toggleAll)));
        //.then(net.minecraft.commands.Commands.argument("player", EntityArgument.players())
        //        .executes(Commands::toggle))));
    }

    private static int toggleAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return toggleGeneric(context, context.getSource().getServer().getPlayerList().getPlayers(), false);
    }

    //Unused atm. No good way of ensuring client-server has same toggle values except making it more complicated than it needs to be
    //This is a debugging mod.
    private static int toggle(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return toggleGeneric(context, EntityArgument.getPlayers(context, "player"), true);
    }

    private static int toggleGeneric(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players, boolean showPlayers) throws CommandSyntaxException {
        ResourceLocation id = ResourceLocationArgument.getId(context, "module");
        if (id.equals(DebugToggles.ALL)) {
            DebugToggles.toggleAllOff(players);
            context.getSource().sendSuccess(()->Component.literal("Turned all debugging features off"), true);
            return players.size();
        }
        DebugToggles.ResourcedToggle t = DebugToggles.get(id);
        if (t != null) {
            boolean on = t.toggleFor(players);
            String txt = "Turned " + id + (on ? " on" : " off");
            if (showPlayers)
                txt += " for " + players.stream().map(p -> p.getGameProfile().getName()).toList();
            Component comp = Component.literal(txt);
            context.getSource().sendSuccess(()->comp, true);
            return players.size();
        }
        context.getSource().sendFailure(Component.literal("No such toggle " + id));
        return 0;
    }

    private static CompletableFuture<Suggestions> getToggles(CommandContext<CommandSourceStack> context, SuggestionsBuilder build) {
        return SharedSuggestionProvider.suggest(Stream.concat(Stream.of(DebugToggles.ALL.toString()), DebugToggles.getRegistered().stream().map(ResourceLocation::toString)).toList(), build);
    }
}
