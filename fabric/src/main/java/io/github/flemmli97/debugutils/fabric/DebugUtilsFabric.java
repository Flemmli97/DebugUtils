package io.github.flemmli97.debugutils.fabric;

import io.github.flemmli97.debugutils.Commands;
import io.github.flemmli97.debugutils.DebugToggles;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class DebugUtilsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, reg, dedicated) -> Commands.register(dispatcher)));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> DebugToggles.onLogin(handler.getPlayer()));
    }
}
