package io.github.flemmli97.debugutils.fabric;

import io.github.flemmli97.debugutils.client.ClientDebugCommands;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class DebugUtilsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                ClientDebugCommands.register(dispatcher, ClientCommandManager::literal, ClientCommandManager::argument, FabricClientCommandSource::sendFeedback));
    }
}
