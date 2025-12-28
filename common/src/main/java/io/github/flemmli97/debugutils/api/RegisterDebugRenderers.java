package io.github.flemmli97.debugutils.api;

import io.github.flemmli97.debugutils.client.DebugRenderHandler;
import net.minecraft.util.debug.DebugSubscription;

public interface RegisterDebugRenderers {

    /**
     * Register a custom debug renderer with given subscription.
     */
    static void registerCustomDebugRenderer(DebugSubscription<?> subscription, DebugRenderHolder renderer) {
        DebugRenderHandler.registerHandler(subscription, renderer);
    }
}
