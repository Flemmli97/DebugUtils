package io.github.flemmli97.debugutils.client;

import net.minecraft.client.renderer.debug.DebugRenderer;

public interface DebugRendererModifier {

    void debugutils$update(DebugRenderer.SimpleDebugRenderer renderer, boolean add);
}
