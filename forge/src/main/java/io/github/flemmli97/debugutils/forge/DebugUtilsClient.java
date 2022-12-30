package io.github.flemmli97.debugutils.forge;

import io.github.flemmli97.debugutils.client.RenderBools;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;

public class DebugUtilsClient {

    public static void disconnect(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        RenderBools.onDisconnet();
    }
}
