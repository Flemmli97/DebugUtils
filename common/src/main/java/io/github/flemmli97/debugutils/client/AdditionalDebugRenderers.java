package io.github.flemmli97.debugutils.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.flemmli97.debugutils.DebugUtils;
import io.github.flemmli97.debugutils.client.spawnchunks.SpawnChunkRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class AdditionalDebugRenderers {

    private static final Map<ResourceLocation, DebugRenderer.SimpleDebugRenderer> RENDERERS = new HashMap<>();

    public static void init() {
        register(new ResourceLocation(DebugUtils.MODID, "spawn_chunks"), SpawnChunkRenderer.INSTANCE);
    }

    public static synchronized void register(ResourceLocation res, DebugRenderer.SimpleDebugRenderer renderer) {
        if (RENDERERS.containsKey(res))
            throw new IllegalArgumentException("A renderer with id" + res + " is already registered");
        RENDERERS.put(res, renderer);
    }

    public static void render(PoseStack poseStack, MultiBufferSource bufferSource, double camX, double camY, double camZ) {
        RENDERERS.values().forEach(r -> r.render(poseStack, bufferSource, camX, camY, camZ));
    }

    public static void clearRenderers() {
        RENDERERS.values().forEach(DebugRenderer.SimpleDebugRenderer::clear);
    }
}
