package io.github.flemmli97.debugutils.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.flemmli97.debugutils.client.RenderBools;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.BeeDebugRenderer;
import net.minecraft.client.renderer.debug.BrainDebugRenderer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.debug.GameEventListenerRenderer;
import net.minecraft.client.renderer.debug.GoalSelectorDebugRenderer;
import net.minecraft.client.renderer.debug.PathfindingRenderer;
import net.minecraft.client.renderer.debug.RaidDebugRenderer;
import net.minecraft.client.renderer.debug.StructureRenderer;
import net.minecraft.client.renderer.debug.VillageSectionsDebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {

    @Shadow
    private PathfindingRenderer pathfindingRenderer;
    @Shadow
    private DebugRenderer.SimpleDebugRenderer waterDebugRenderer;
    @Shadow
    private DebugRenderer.SimpleDebugRenderer heightMapRenderer;
    @Shadow
    private DebugRenderer.SimpleDebugRenderer collisionBoxRenderer;
    @Shadow
    private DebugRenderer.SimpleDebugRenderer neighborsUpdateRenderer;
    @Shadow
    private StructureRenderer structureRenderer;
    @Shadow
    private DebugRenderer.SimpleDebugRenderer lightDebugRenderer;
    @Shadow
    private DebugRenderer.SimpleDebugRenderer worldGenAttemptRenderer; //Nothing for now cause there is no packet send for it
    @Shadow
    private DebugRenderer.SimpleDebugRenderer solidFaceRenderer;
    @Shadow
    private DebugRenderer.SimpleDebugRenderer chunkRenderer;
    @Shadow
    private BrainDebugRenderer brainDebugRenderer;
    @Shadow
    private VillageSectionsDebugRenderer villageSectionsDebugRenderer;
    @Shadow
    private BeeDebugRenderer beeDebugRenderer;
    @Shadow
    private RaidDebugRenderer raidDebugRenderer;
    @Shadow
    private GoalSelectorDebugRenderer goalSelectorRenderer;
    @Shadow
    private GameEventListenerRenderer gameEventListenerRenderer;

    @Inject(method = "render", at = @At("RETURN"))
    private void doDebugRenderers(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, double camX, double camY, double camZ, CallbackInfo info) {
        if (RenderBools.DEBUG_PATHS)
            this.pathfindingRenderer.render(poseStack, bufferSource, camX, camY, camZ);
        if (RenderBools.DEBUG_WATER)
            this.waterDebugRenderer.render(poseStack, bufferSource, camX, camY, camZ);
        if (RenderBools.DEBUG_HEIGHTMAP)
            this.heightMapRenderer.render(poseStack, bufferSource, camX, camY, camZ);
        if (RenderBools.DEBUG_COLLISION)
            this.collisionBoxRenderer.render(poseStack, bufferSource, camX, camY, camZ);
        if (RenderBools.DEBUG_BLOCKUPDATES)
            this.neighborsUpdateRenderer.render(poseStack, bufferSource, camX, camY, camZ);
        if (RenderBools.DEBUG_STRUCTURES)
            this.structureRenderer.render(poseStack, bufferSource, camX, camY, camZ);
        if (RenderBools.DEBUG_LIGHT)
            this.lightDebugRenderer.render(poseStack, bufferSource, camX, camY, camZ);
        if (RenderBools.DEBUG_SOLID_FACES)
            this.solidFaceRenderer.render(poseStack, bufferSource, camX, camY, camZ);
        if (RenderBools.DEBUG_CHUNK)
            this.chunkRenderer.render(poseStack, bufferSource, camX, camY, camZ);
        if (RenderBools.DEBUG_BRAIN || RenderBools.DEBUG_POI)
            this.brainDebugRenderer.render(poseStack, bufferSource, camX, camY, camZ);
        if (RenderBools.DEBUG_BEE || RenderBools.DEBUG_HIVE)
            this.beeDebugRenderer.render(poseStack, bufferSource, camX, camY, camZ);
        if (RenderBools.DEBUG_RAIDS)
            this.raidDebugRenderer.render(poseStack, bufferSource, camX, camY, camZ);
        if (RenderBools.DEBUG_GOALS)
            this.goalSelectorRenderer.render(poseStack, bufferSource, camX, camY, camZ);
        if (RenderBools.DEBUG_GAME_EVENT || RenderBools.DEBUG_GAME_EVENT_LISTENER)
            this.gameEventListenerRenderer.render(poseStack, bufferSource, camX, camY, camZ);
    }
}
