package io.github.flemmli97.debugutils.api;

import io.github.flemmli97.debugutils.client.DebugRenderHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.debug.DebugSubscription;

public interface RegisterDebugRenderers {

    /**
     * Register a custom debug renderer with given subscription.
     */
    static void registerCustomDebugRenderer(DebugSubscription<?> subscription, DebugRenderHolder renderer) {
        DebugRenderHandler.registerHandler(subscription, renderer);
    }

    /**
     * Register a custom debug renderer with given id.
     * This version registers a client side only one that does not require data from the server.
     */
    static void registerCustomDebugRenderer(ResourceLocation id, DebugRenderHolder renderer) {
        DebugRenderHandler.registerClientOnlyHandler(id, renderer);
    }
}
