package io.github.flemmli97.debugutils.api;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.ChunkCullingDebugRenderer;
import net.minecraft.client.renderer.debug.DebugRenderer;

import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Holder to create toggleable DebugRenderers
 *
 * @param alreadyEnabled For modded ones simply return false. This is used in vanilla to not add renderers that are already enabled
 * @param factory        Factory to create the renderer. The instance is cached
 * @param translucent    Indicate whether this renderer counts as translucent or not.
 *                       In vanilla only {@link ChunkCullingDebugRenderer} is counted as translucent
 */
public record DebugRenderHolder(BooleanSupplier alreadyEnabled,
                                Function<Minecraft, DebugRenderer.SimpleDebugRenderer> factory, boolean translucent) {

    public DebugRenderHolder(BooleanSupplier alreadyEnabled, Function<Minecraft, DebugRenderer.SimpleDebugRenderer> factory, boolean translucent) {
        this.alreadyEnabled = alreadyEnabled;
        this.factory = Util.memoize(factory);
        this.translucent = translucent;
    }

    public DebugRenderHolder(BooleanSupplier alreadyEnabled, Function<Minecraft, DebugRenderer.SimpleDebugRenderer> factory) {
        this(alreadyEnabled, factory, false);
    }

    public DebugRenderHolder(BooleanSupplier alreadyEnabled, Supplier<DebugRenderer.SimpleDebugRenderer> factory) {
        this(alreadyEnabled, Util.memoize(mc -> factory.get()), false);
    }
}
