package io.github.flemmli97.debugutils;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.debugutils.utils.PlayerDebugToggle;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.debug.DebugSubscription;

import java.util.Collection;
import java.util.List;

public class DebugCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(Commands.literal(DebugUtils.MODID)
                .then(Commands.argument("module", ResourceArgument.resource(buildContext, Registries.DEBUG_SUBSCRIPTION)).requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.argument("on", BoolArgumentType.bool())
                                .executes(DebugCommands::toggle)))
                .then(Commands.literal("player").requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.argument("module", ResourceArgument.resource(buildContext, Registries.DEBUG_SUBSCRIPTION))
                                .then(Commands.argument("players", EntityArgument.players())
                                        .then(Commands.argument("on", BoolArgumentType.bool())
                                                .executes(ctx -> toggleFor(ctx, EntityArgument.getPlayers(ctx, "players")))))))
                .then(Commands.literal("off").executes(DebugCommands::toggleOff)
                        .then(Commands.argument("players", EntityArgument.players())
                                .executes(ctx -> toggleOffFor(ctx, EntityArgument.getPlayers(ctx, "players"))))));
    }

    private static int toggle(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return toggleFor(context, context.getSource().getEntity() instanceof ServerPlayer serverPlayer ? List.of(serverPlayer) : List.of());
    }

    private static int toggleFor(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) throws CommandSyntaxException {
        boolean on = BoolArgumentType.getBool(context, "on");
        Holder.Reference<DebugSubscription<?>> value = ResourceArgument.getResource(context, "module", Registries.DEBUG_SUBSCRIPTION);
        for (ServerPlayer player : players) {
            ((PlayerDebugToggle) player).debugutils$toggle(List.of(value.value()), on);
        }
        String[] key = new String[]{"debugutils.command.toggle." + (on ? "on" : "off")};
        ServerPlayer sender = context.getSource().getPlayer();
        if (players.size() == 1 && players.stream().findFirst().map(p -> p.equals(sender)).orElse(false)) {
            key[0] += ".self";
        }
        context.getSource().sendSuccess(() -> Component.translatable(key[0], value.key().identifier().toString(), players.stream().map(p -> p.getGameProfile().name()).toList().toString()), true);
        return players.size();
    }

    private static int toggleOff(CommandContext<CommandSourceStack> context) {
        return toggleOffFor(context, context.getSource().getEntity() instanceof ServerPlayer serverPlayer ? List.of(serverPlayer) : List.of());
    }

    private static int toggleOffFor(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            ((PlayerDebugToggle) player).debugutils$toggle(BuiltInRegistries.DEBUG_SUBSCRIPTION.stream().toList(), false);
        }
        String[] key = new String[]{"debugutils.command.all.off"};
        ServerPlayer sender = context.getSource().getPlayer();
        if (players.size() == 1 && players.stream().findFirst().map(p -> p.equals(sender)).orElse(false)) {
            key[0] += ".self";
        }
        context.getSource().sendSuccess(() -> Component.translatable(key[0], players.stream().map(p -> p.getGameProfile().name()).toList().toString()), true);
        return players.size();
    }
}
