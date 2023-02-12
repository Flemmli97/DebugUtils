package io.github.flemmli97.debugutils.fabric;

import io.github.flemmli97.debugutils.client.AdditionalDebugRenderers;
import io.github.flemmli97.debugutils.client.RenderBools;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;

public class DebugUtilsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPacketHandler.registerClientPackets();
        ClientLoginConnectionEvents.DISCONNECT.register((handler, client) -> RenderBools.onDisconnect());
        AdditionalDebugRenderers.init();
    }
}
