package io.github.flemmli97.debugutils.api;

import io.github.flemmli97.debugutils.DebugToggles;
import io.github.flemmli97.debugutils.client.AdditionalDebugRenderers;
import io.github.flemmli97.debugutils.client.RenderBools;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface RegisterDebugRenderers {

    /**
     * Register a custom debug renderer with given id
     */
    static void registerCustomDebugRenderer(ResourceLocation id, DebugRenderer.SimpleDebugRenderer renderer) {
        AdditionalDebugRenderers.register(id, renderer);
    }

    /**
     * Register a simple debug toggle with the given id
     * You don't need to do this, but then if you want a way of turning your debug renderer on/off you need to do it yourself
     */
    static DebugToggles.ResourcedToggle registerServerToggle(ResourceLocation id) {
        return DebugToggles.register(id);
    }

    /**
     * Register a simple debug toggle with the given id
     * You don't need to do this, but then if you want a way of turning your debug renderer on/off you need to do it yourself
     *
     * @param onToggle Something to run when the debug feature is toggled. E.g. if you want to send a packet
     */
    static DebugToggles.ResourcedToggle registerServerToggle(ResourceLocation id, BiConsumer<Boolean, Collection<ServerPlayer>> onToggle) {
        return DebugToggles.register(new DebugToggles.ResourcedToggle(id, onToggle));
    }

    /**
     * Register a toggle handler on the client side
     *
     * @param id       The id of the handler. This NEEDS to be the same as the toggle registered using {@link #registerServerToggle}
     * @param consumer A handler for the updated value. The boolean given here is the value send by the server
     */
    static void registerClientHandler(ResourceLocation id, Consumer<Boolean> consumer) {
        RenderBools.registerClientHandler(id, consumer);
    }
}
