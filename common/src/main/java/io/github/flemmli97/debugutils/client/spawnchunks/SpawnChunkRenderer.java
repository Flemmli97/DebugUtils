package io.github.flemmli97.debugutils.client.spawnchunks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.flemmli97.debugutils.client.RenderBools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SpawnChunkRenderer implements DebugRenderer.SimpleDebugRenderer {

    public static final SpawnChunkRenderer INSTANCE = new SpawnChunkRenderer();

    private int spawnTicketLevel = 2;

    public void updateSpawnChunk(int ticketLevel) {
        this.spawnTicketLevel = ticketLevel;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, double camX, double camY, double camZ) {
        if (!RenderBools.DEBUG_SPAWN_CHUNK || !(bufferSource instanceof MultiBufferSource.BufferSource source))
            return;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || Minecraft.getInstance().cameraEntity == null)
            return;
        Vec3 viewPos = Minecraft.getInstance().cameraEntity.position();
        BlockPos spawnPos = level.getSharedSpawnPos();
        int minX = SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(spawnPos.getX()));
        int minZ = SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(spawnPos.getZ()));
        AABB spawnChunkAABB = new AABB(minX, level.getMinY(), minZ, minX + 16, level.getMaxY(), minZ + 16)
                .move(-camX, -camY, -camZ);

        VertexConsumer quads = source.getBuffer(RenderType.debugQuads());

        float renderDistance = Minecraft.getInstance().gameRenderer.getRenderDistance() + 16;

        if (viewPos.distanceToSqr(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5) < renderDistance * renderDistance)
            this.renderBox(quads, new AABB(spawnPos).move(-camX, -camY, -camZ).inflate(-0.0001), 220 / 255f, 100 / 255f, 100 / 255f, 0.5f);

        if (this.spawnTicketLevel < 0) {
            source.endBatch();
            return;
        }

        List<Consumer<VertexConsumer>> deferredLines = new ArrayList<>();
        this.renderBorder(renderDistance, spawnChunkAABB, SectionPos.sectionToBlockCoord(ChunkLevel.byStatus(FullChunkStatus.FULL) - this.spawnTicketLevel),
                quads, 220 / 255f, 100 / 255f, 100 / 255f, deferredLines);

        this.renderBorder(renderDistance, spawnChunkAABB, SectionPos.sectionToBlockCoord(ChunkLevel.byStatus(FullChunkStatus.BLOCK_TICKING) - this.spawnTicketLevel),
                quads, 230 / 255f, 230 / 255f, 30 / 255f, deferredLines);

        this.renderBorder(renderDistance, spawnChunkAABB, SectionPos.sectionToBlockCoord(ChunkLevel.byStatus(FullChunkStatus.ENTITY_TICKING) - this.spawnTicketLevel),
                quads, 20 / 255f, 170 / 255f, 10 / 255f, deferredLines);

        source.endBatch();
        VertexConsumer lines = source.getBuffer(RenderType.debugLine(3));
        deferredLines.forEach(c -> c.accept(lines));
        source.endBatch();
    }

    private void renderBorder(float renderDistance, AABB base, double range, VertexConsumer quads,
                              float red, float green, float blue, List<Consumer<VertexConsumer>> deferredLines) {
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
        AABB lineBB = aabb.inflate(0.001);
        deferredLines.add(lines -> this.renderLines(lines, lineBB, tooFarAway, red, green, blue));
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

        consumer.addVertex(minX, minY, minZ).setColor(red, green, blue, alpha);
        consumer.addVertex(minX, minY, maxZ).setColor(red, green, blue, alpha);
        consumer.addVertex(minX, maxY, maxZ).setColor(red, green, blue, alpha);
        consumer.addVertex(minX, maxY, minZ).setColor(red, green, blue, alpha);

        consumer.addVertex(maxX, maxY, minZ).setColor(red, green, blue, alpha);
        consumer.addVertex(maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        consumer.addVertex(maxX, minY, maxZ).setColor(red, green, blue, alpha);
        consumer.addVertex(maxX, minY, minZ).setColor(red, green, blue, alpha);

        consumer.addVertex(minX, maxY, minZ).setColor(red, green, blue, alpha);
        consumer.addVertex(maxX, maxY, minZ).setColor(red, green, blue, alpha);
        consumer.addVertex(maxX, minY, minZ).setColor(red, green, blue, alpha);
        consumer.addVertex(minX, minY, minZ).setColor(red, green, blue, alpha);

        consumer.addVertex(maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        consumer.addVertex(minX, maxY, maxZ).setColor(red, green, blue, alpha);
        consumer.addVertex(minX, minY, maxZ).setColor(red, green, blue, alpha);
        consumer.addVertex(maxX, minY, maxZ).setColor(red, green, blue, alpha);

        consumer.addVertex(maxX, minY, minZ).setColor(red, green, blue, alpha);
        consumer.addVertex(maxX, minY, maxZ).setColor(red, green, blue, alpha);
        consumer.addVertex(minX, minY, maxZ).setColor(red, green, blue, alpha);
        consumer.addVertex(minX, minY, minZ).setColor(red, green, blue, alpha);

        consumer.addVertex(minX, maxY, minZ).setColor(red, green, blue, alpha);
        consumer.addVertex(minX, maxY, maxZ).setColor(red, green, blue, alpha);
        consumer.addVertex(maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        consumer.addVertex(maxX, maxY, minZ).setColor(red, green, blue, alpha);
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
            consumer.addVertex(minX, minY, minZ).setColor(red, green, blue, alpha);
            consumer.addVertex(minX, minY, maxZ).setColor(red, green, blue, alpha);
            consumer.addVertex(minX, maxY, maxZ).setColor(red, green, blue, alpha);
            consumer.addVertex(minX, maxY, minZ).setColor(red, green, blue, alpha);
        }

        if (renderEast) {
            consumer.addVertex(maxX, maxY, minZ).setColor(red, green, blue, alpha);
            consumer.addVertex(maxX, maxY, maxZ).setColor(red, green, blue, alpha);
            consumer.addVertex(maxX, minY, maxZ).setColor(red, green, blue, alpha);
            consumer.addVertex(maxX, minY, minZ).setColor(red, green, blue, alpha);
        }

        if (renderNorth) {
            consumer.addVertex(minX, maxY, minZ).setColor(red, green, blue, alpha);
            consumer.addVertex(maxX, maxY, minZ).setColor(red, green, blue, alpha);
            consumer.addVertex(maxX, minY, minZ).setColor(red, green, blue, alpha);
            consumer.addVertex(minX, minY, minZ).setColor(red, green, blue, alpha);
        }

        if (renderSouth) {
            consumer.addVertex(maxX, maxY, maxZ).setColor(red, green, blue, alpha);
            consumer.addVertex(minX, maxY, maxZ).setColor(red, green, blue, alpha);
            consumer.addVertex(minX, minY, maxZ).setColor(red, green, blue, alpha);
            consumer.addVertex(maxX, minY, maxZ).setColor(red, green, blue, alpha);
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
                lines.addVertex(minX, minY, z).setNormal(0, 1, 0).setColor(red, green, blue, 1);
                lines.addVertex(minX, maxY, z).setNormal(0, 1, 0).setColor(red, green, blue, 1);
            }

            if (renderEast) {
                lines.addVertex(maxX, minY, z).setNormal(0, 1, 0).setColor(red, green, blue, 1);
                lines.addVertex(maxX, maxY, z).setNormal(0, 1, 0).setColor(red, green, blue, 1);
            }
        }

        for (float x = minX + 16; x < aabb.maxX; x += 16) {
            if (renderNorth) {
                lines.addVertex(x, minY, minZ).setNormal(0, 1, 0).setColor(red, green, blue, 1);
                lines.addVertex(x, maxY, minZ).setNormal(0, 1, 0).setColor(red, green, blue, 1);
            }

            if (renderSouth) {
                lines.addVertex(x, minY, maxZ).setNormal(0, 1, 0).setColor(red, green, blue, 1);
                lines.addVertex(x, maxY, maxZ).setNormal(0, 1, 0).setColor(red, green, blue, 1);
            }
        }

        for (float y = minY; y < aabb.maxY; y += 16) {
            if (renderWest) {
                lines.addVertex(minX, y, minZ).setNormal(0, 1, 0).setColor(red, green, blue, 1);
                lines.addVertex(minX, y, maxZ).setNormal(0, 1, 0).setColor(red, green, blue, 1);
            }

            if (renderEast) {
                lines.addVertex(maxX, y, minZ).setNormal(0, 1, 0).setColor(red, green, blue, 1);
                lines.addVertex(maxX, y, maxZ).setNormal(0, 1, 0).setColor(red, green, blue, 1);
            }

            if (renderNorth) {
                lines.addVertex(minX, y, minZ).setNormal(0, 1, 0).setColor(red, green, blue, 1);
                lines.addVertex(maxX, y, minZ).setNormal(0, 1, 0).setColor(red, green, blue, 1);
            }

            if (renderSouth) {
                lines.addVertex(minX, y, maxZ).setNormal(0, 1, 0).setColor(red, green, blue, 1);
                lines.addVertex(maxX, y, maxZ).setNormal(0, 1, 0).setColor(red, green, blue, 1);
            }
        }
    }
}
