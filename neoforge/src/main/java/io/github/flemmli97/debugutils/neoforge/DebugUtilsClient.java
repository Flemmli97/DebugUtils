package io.github.flemmli97.debugutils.neoforge;

import io.github.flemmli97.debugutils.client.RenderBools;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

public class DebugUtilsClient {

    public static void disconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        RenderBools.onDisconnect();
    }
}
