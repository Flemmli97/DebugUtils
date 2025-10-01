package io.github.flemmli97.debugutils.neoforge;

import io.github.flemmli97.debugutils.client.ClientDebugCommands;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

public class DebugUtilsClient {

    public static void registerClientCommand(RegisterClientCommandsEvent event) {
        ClientDebugCommands.register(event.getDispatcher(),
                Commands::literal, Commands::argument, (ctx, comp) -> ctx.sendSuccess(() -> comp, true));
    }
}
