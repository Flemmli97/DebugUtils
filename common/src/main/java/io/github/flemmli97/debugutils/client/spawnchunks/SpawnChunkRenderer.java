package io.github.flemmli97.debugutils.client.spawnchunks;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.flemmli97.debugutils.client.RenderBools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class SpawnChunkRenderer implements DebugRenderer.SimpleDebugRenderer {

    public static final SpawnChunkRenderer INSTANCE = new SpawnChunkRenderer();

    private static final BufferBuilder QUADS = new BufferBuilder(256);
    private static final BufferBuilder LINES = new BufferBuilder(256);

    private int spawnTicketLevel = 11;

    public void updateSpawnChunk(int ticketLevel) {
        this.spawnTicketLevel = ticketLevel;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, double camX, double camY, double camZ) {
        if (!RenderBools.DEBUG_SPAWN_CHUNK)
            return;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || Minecraft.getInstance().cameraEntity == null)
            return;
        Vec3 viewPos = Minecraft.getInstance().cameraEntity.position();
        BlockPos spawnPos = level.getSharedSpawnPos();
        int range = 34 - this.spawnTicketLevel;

        int minX = SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(spawnPos.getX()));
        int minZ = SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(spawnPos.getZ()));
        AABB spawnChunkAABB = new AABB(minX, level.getMinBuildHeight(), minZ, minX + 16, level.getMaxBuildHeight(), minZ + 16)
                .move(-camX, -camY, -camZ);

        QUADS.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        LINES.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);

        float renderDistance = Minecraft.getInstance().gameRenderer.getRenderDistance() + 16;

        if (viewPos.distanceToSqr(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5) < renderDistance * renderDistance)
            this.renderBox(QUADS, new AABB(spawnPos).move(-camX, -camY, -camZ).inflate(-0.0001), 220 / 255f, 100 / 255f, 100 / 255f, 0.5f);

        this.renderBorder(renderDistance, spawnChunkAABB, SectionPos.sectionToBlockCoord(range),
                QUADS, LINES, 220 / 255f, 100 / 255f, 100 / 255f);

        this.renderBorder(renderDistance, spawnChunkAABB, SectionPos.sectionToBlockCoord(range - 2),
                QUADS, LINES, 20 / 255f, 170 / 255f, 10 / 255f);

        QUADS.end();
        BufferUploader.end(QUADS);

        RenderSystem.disableBlend();
        RenderSystem.lineWidth(3.0F);
        LINES.end();
        BufferUploader.end(LINES);
        RenderSystem.lineWidth(1.0F);

        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(true);
    }

    private void renderBorder(float renderDistance, AABB base, double range, VertexConsumer quads, VertexConsumer lines, float red, float green, float blue) {
        AABB aabb = base.inflate(range, 0, range);
        List<Direction> tooFarAway = new ArrayList<>();
        double dXMin = Math.abs(aabb.minX);
        double dXMax = Math.abs(aabb.maxX);
        double dZMin = Math.abs(aabb.minZ);
        double dZMax = Math.abs(aabb.maxZ);
        if (dZMin > renderDistance || aabb.minX > renderDistance || aabb.maxX < -renderDistance)
            tooFarAway.add(Direction.NORTH);
        if (dXMax > renderDistance || aabb.minZ > renderDistance || aabb.maxZ < -renderDistance)
            tooFarAway.add(Direction.EAST);
        if (dZMax > renderDistance || aabb.minX > renderDistance || aabb.maxX < -renderDistance)
            tooFarAway.add(Direction.SOUTH);
        if (dXMin > renderDistance || aabb.minZ > renderDistance || aabb.maxZ < -renderDistance)
            tooFarAway.add(Direction.WEST);
        this.renderWall(quads, aabb, tooFarAway, red, green, blue, 0.2f);
        this.renderLines(lines, aabb, tooFarAway, red, green, blue);
    }

    @Override
    public void clear() {
        this.spawnTicketLevel = 11;
    }

    private void renderBox(VertexConsumer consumer, AABB aabb, float red, float green, float blue, float alpha) {
        float minX = (float) aabb.minX;
        float minY = (float) aabb.minY;
        float minZ = (float) aabb.minZ;
        float maxX = (float) aabb.maxX;
        float maxY = (float) aabb.maxY;
        float maxZ = (float) aabb.maxZ;

        consumer.vertex(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
        consumer.vertex(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        consumer.vertex(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        consumer.vertex(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();

        consumer.vertex(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
        consumer.vertex(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        consumer.vertex(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        consumer.vertex(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();

        consumer.vertex(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
        consumer.vertex(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
        consumer.vertex(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
        consumer.vertex(minX, minY, minZ).color(red, green, blue, alpha).endVertex();

        consumer.vertex(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        consumer.vertex(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        consumer.vertex(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        consumer.vertex(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();

        consumer.vertex(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
        consumer.vertex(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        consumer.vertex(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        consumer.vertex(minX, minY, minZ).color(red, green, blue, alpha).endVertex();

        consumer.vertex(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
        consumer.vertex(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        consumer.vertex(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        consumer.vertex(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
    }

    private void renderWall(VertexConsumer consumer, AABB aabb, List<Direction> tooFar, float red, float green, float blue, float alpha) {
        float minX = (float) aabb.minX;
        float minY = (float) aabb.minY;
        float minZ = (float) aabb.minZ;
        float maxX = (float) aabb.maxX;
        float maxY = (float) aabb.maxY;
        float maxZ = (float) aabb.maxZ;

        boolean renderNorth = !tooFar.contains(Direction.NORTH);
        boolean renderEast = !tooFar.contains(Direction.EAST);
        boolean renderSouth = !tooFar.contains(Direction.SOUTH);
        boolean renderWest = !tooFar.contains(Direction.WEST);

        if (renderWest) {
            consumer.vertex(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
            consumer.vertex(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
            consumer.vertex(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            consumer.vertex(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
        }

        if (renderEast) {
            consumer.vertex(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
            consumer.vertex(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            consumer.vertex(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
            consumer.vertex(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
        }

        if (renderNorth) {
            consumer.vertex(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
            consumer.vertex(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
            consumer.vertex(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
            consumer.vertex(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
        }

        if (renderSouth) {
            consumer.vertex(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            consumer.vertex(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            consumer.vertex(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
            consumer.vertex(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        }
    }

    private void renderLines(VertexConsumer lines, AABB aabb, List<Direction> tooFar, float red, float green, float blue) {
        float minX = (float) aabb.minX;
        float minY = (float) aabb.minY;
        float minZ = (float) aabb.minZ;
        float maxX = (float) aabb.maxX;
        float maxY = (float) aabb.maxY;
        float maxZ = (float) aabb.maxZ;
        boolean renderNorth = !tooFar.contains(Direction.NORTH);
        boolean renderEast = !tooFar.contains(Direction.EAST);
        boolean renderSouth = !tooFar.contains(Direction.SOUTH);
        boolean renderWest = !tooFar.contains(Direction.WEST);
        for (float z = minZ; z < aabb.maxZ; z += 16) {
            if (renderWest) {
                lines.vertex(minX, minY, z).color(red, green, blue, 1).endVertex();
                lines.vertex(minX, maxY, z).color(red, green, blue, 1).endVertex();
            }

            if (renderEast) {
                lines.vertex(maxX, minY, z).color(red, green, blue, 1).endVertex();
                lines.vertex(maxX, maxY, z).color(red, green, blue, 1).endVertex();
            }
        }

        for (float x = minX + 16; x < aabb.maxX; x += 16) {
            if (renderNorth) {
                lines.vertex(x, minY, minZ).color(red, green, blue, 1).endVertex();
                lines.vertex(x, maxY, minZ).color(red, green, blue, 1).endVertex();
            }

            if (renderSouth) {
                lines.vertex(x, minY, maxZ).color(red, green, blue, 1).endVertex();
                lines.vertex(x, maxY, maxZ).color(red, green, blue, 1).endVertex();
            }
        }

        for (float y = minY; y < aabb.maxY; y += 16) {
            if (renderWest) {
                lines.vertex(minX, y, minZ).color(red, green, blue, 1).endVertex();
                lines.vertex(minX, y, maxZ).color(red, green, blue, 1).endVertex();
            }

            if (renderEast) {
                lines.vertex(maxX, y, minZ).color(red, green, blue, 1).endVertex();
                lines.vertex(maxX, y, maxZ).color(red, green, blue, 1).endVertex();
            }

            if (renderNorth) {
                lines.vertex(minX, y, minZ).color(red, green, blue, 1).endVertex();
                lines.vertex(maxX, y, minZ).color(red, green, blue, 1).endVertex();
            }

            if (renderSouth) {
                lines.vertex(minX, y, maxZ).color(red, green, blue, 1).endVertex();
                lines.vertex(maxX, y, maxZ).color(red, green, blue, 1).endVertex();
            }
        }
    }
}
