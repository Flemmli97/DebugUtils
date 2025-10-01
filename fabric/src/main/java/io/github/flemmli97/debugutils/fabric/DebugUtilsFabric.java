package io.github.flemmli97.debugutils.fabric;

import io.github.flemmli97.debugutils.DebugCommands;
import io.github.flemmli97.debugutils.network.S2CDebugToggle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;

public class DebugUtilsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        registerPackets();
        CommandRegistrationCallback.EVENT.register(((dispatcher, reg, dedicated) -> DebugCommands.register(dispatcher, reg)));
    }

    public static void registerPackets() {
        PayloadTypeRegistry.playS2C().register(S2CDebugToggle.TYPE, S2CDebugToggle.STREAM_CODEC);
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientPlayNetworking.registerGlobalReceiver(S2CDebugToggle.TYPE, (pkt, ctx) -> S2CDebugToggle.handle(pkt));
        }
    }
}
