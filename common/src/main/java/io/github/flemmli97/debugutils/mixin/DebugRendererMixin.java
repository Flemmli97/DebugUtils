package io.github.flemmli97.debugutils.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.flemmli97.debugutils.client.AdditionalDebugRenderers;
import io.github.flemmli97.debugutils.client.RenderBools;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.BeeDebugRenderer;
import net.minecraft.client.renderer.debug.BrainDebugRenderer;
import net.minecraft.client.renderer.debug.BreezeDebugRenderer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.debug.GameEventListenerRenderer;
import net.minecraft.client.renderer.debug.GoalSelectorDebugRenderer;
import net.minecraft.client.renderer.debug.NeighborsUpdateRenderer;
import net.minecraft.client.renderer.debug.PathfindingRenderer;
import net.minecraft.client.renderer.debug.RaidDebugRenderer;
import net.minecraft.client.renderer.debug.RedstoneWireOrientationsRenderer;
import net.minecraft.client.renderer.debug.StructureRenderer;
import net.minecraft.client.renderer.debug.VillageSectionsDebugRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {

    @Final
    @Shadow
    public PathfindingRenderer pathfindingRenderer;
    @Final
    @Shadow
    public DebugRenderer.SimpleDebugRenderer waterDebugRenderer;
    @Final
    @Shadow
    public DebugRenderer.SimpleDebugRenderer heightMapRenderer;
    @Final
    @Shadow
    public DebugRenderer.SimpleDebugRenderer collisionBoxRenderer;
    @Final
    @Shadow
    public DebugRenderer.SimpleDebugRenderer supportBlockRenderer;
    @Final
    @Shadow
    public NeighborsUpdateRenderer neighborsUpdateRenderer;
    /**
     * Not too sure how to handle this.
     * The packet is send via {@link net.minecraft.network.protocol.game.DebugPackets#sendWireUpdates}
     * Assume a place to send it would be {@link net.minecraft.world.level.redstone.RedstoneWireEvaluator#updatePowerStrength} or
     * {@link net.minecraft.world.level.block.RedStoneWireBlock#updatesOnShapeChange}
     */
    @Final
    @Shadow
    public RedstoneWireOrientationsRenderer redstoneWireOrientationsRenderer;
    @Final
    @Shadow
    public StructureRenderer structureRenderer;
    @Final
    @Shadow
    public DebugRenderer.SimpleDebugRenderer lightDebugRenderer;
    /**
     * Missing packet impl.
     * Relevant packet is {@link net.minecraft.network.protocol.common.custom.WorldGenAttemptDebugPayload}
     */
    @Final
    @Shadow
    public DebugRenderer.SimpleDebugRenderer worldGenAttemptRenderer;
    @Final
    @Shadow
    public DebugRenderer.SimpleDebugRenderer solidFaceRenderer;
    @Final
    @Shadow
    public DebugRenderer.SimpleDebugRenderer chunkRenderer;
    @Final
    @Shadow
    public BrainDebugRenderer brainDebugRenderer;
    /**
     * Missing packet.
     * Relevant packet is {@link net.minecraft.network.protocol.common.custom.VillageSectionsDebugPayload}
     */
    @Final
    @Shadow
    public VillageSectionsDebugRenderer villageSectionsDebugRenderer;
    @Final
    @Shadow
    public BeeDebugRenderer beeDebugRenderer;
    @Final
    @Shadow
    public RaidDebugRenderer raidDebugRenderer;
    @Final
    @Shadow
    public GoalSelectorDebugRenderer goalSelectorRenderer;
    @Final
    @Shadow
    public GameEventListenerRenderer gameEventListenerRenderer;
    @Final
    @Shadow
    public BreezeDebugRenderer breezeDebugRenderer;

    @Inject(method = "render", at = @At("RETURN"))
    private void doDebugRenderers(PoseStack poseStack, Frustum frustum, MultiBufferSource.BufferSource bufferSource, double camX, double camY, double camZ, CallbackInfo info) {
        if (RenderBools.DEBUG_PATHS)
            this.pathfindingRenderer.render(poseStack, bufferSource, camX, camY, camZ);
        if (RenderBools.DEBUG_WATER)
            this.waterDebugRenderer.render(poseStack, bufferSource, camX, camY, camZ);
        if (RenderBools.DEBUG_HEIGHTMAP)
            this.heightMapRenderer.render(poseStack, bufferSource, camX, camY, camZ);
        if (RenderBools.DEBUG_COLLISION)
            this.collisionBoxRenderer.render(poseStack, bufferSource, camX, camY, camZ);
        if (RenderBools.DEBUG_SUPPORT_BLOCKS)
            this.supportBlockRenderer.render(poseStack, bufferSource, camX, camY, camZ);
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
        if (RenderBools.DEBUG_BREEZE)
            this.breezeDebugRenderer.render(poseStack, bufferSource, camX, camY, camZ);
        AdditionalDebugRenderers.render(poseStack, bufferSource, camX, camY, camZ);
    }

    @Inject(method = "clear", at = @At("HEAD"))
    private void onClear(CallbackInfo info) {
        AdditionalDebugRenderers.clearRenderers();
    }
}
