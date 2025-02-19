package io.github.flemmli97.debugutils.client.spawnchunks;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.flemmli97.debugutils.client.RenderBools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.CoreShaders;
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

    private static final ByteBufferBuilder QUADS = new ByteBufferBuilder(256);
    private static final ByteBufferBuilder LINES = new ByteBufferBuilder(256);

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
        AABB spawnChunkAABB = new AABB(minX, level.getMinY(), minZ, minX + 16, level.getMaxY(), minZ + 16)
                .move(-camX, -camY, -camZ);

        BufferBuilder quads = new BufferBuilder(QUADS, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        BufferBuilder lines = new BufferBuilder(LINES, VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        RenderSystem.setShader(CoreShaders.POSITION_COLOR);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);

        float renderDistance = Minecraft.getInstance().gameRenderer.getRenderDistance() + 16;

        if (viewPos.distanceToSqr(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5) < renderDistance * renderDistance)
            this.renderBox(quads, new AABB(spawnPos).move(-camX, -camY, -camZ).inflate(-0.0001), 220 / 255f, 100 / 255f, 100 / 255f, 0.5f);

        this.renderBorder(renderDistance, spawnChunkAABB, SectionPos.sectionToBlockCoord(range),
                quads, lines, 220 / 255f, 100 / 255f, 100 / 255f);

        this.renderBorder(renderDistance, spawnChunkAABB, SectionPos.sectionToBlockCoord(range - 2),
                quads, lines, 20 / 255f, 170 / 255f, 10 / 255f);

        MeshData quadData = quads.build();
        if (quadData != null)
            BufferUploader.drawWithShader(quadData);

        RenderSystem.disableBlend();
        RenderSystem.lineWidth(3.0F);
        MeshData lineData = lines.build();
        if (lineData != null)
            BufferUploader.drawWithShader(lineData);
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
                lines.addVertex(minX, minY, z).setColor(red, green, blue, 1);
                lines.addVertex(minX, maxY, z).setColor(red, green, blue, 1);
            }

            if (renderEast) {
                lines.addVertex(maxX, minY, z).setColor(red, green, blue, 1);
                lines.addVertex(maxX, maxY, z).setColor(red, green, blue, 1);
            }
        }

        for (float x = minX + 16; x < aabb.maxX; x += 16) {
            if (renderNorth) {
                lines.addVertex(x, minY, minZ).setColor(red, green, blue, 1);
                lines.addVertex(x, maxY, minZ).setColor(red, green, blue, 1);
            }

            if (renderSouth) {
                lines.addVertex(x, minY, maxZ).setColor(red, green, blue, 1);
                lines.addVertex(x, maxY, maxZ).setColor(red, green, blue, 1);
            }
        }

        for (float y = minY; y < aabb.maxY; y += 16) {
            if (renderWest) {
                lines.addVertex(minX, y, minZ).setColor(red, green, blue, 1);
                lines.addVertex(minX, y, maxZ).setColor(red, green, blue, 1);
            }

            if (renderEast) {
                lines.addVertex(maxX, y, minZ).setColor(red, green, blue, 1);
                lines.addVertex(maxX, y, maxZ).setColor(red, green, blue, 1);
            }

            if (renderNorth) {
                lines.addVertex(minX, y, minZ).setColor(red, green, blue, 1);
                lines.addVertex(maxX, y, minZ).setColor(red, green, blue, 1);
            }

            if (renderSouth) {
                lines.addVertex(minX, y, maxZ).setColor(red, green, blue, 1);
                lines.addVertex(maxX, y, maxZ).setColor(red, green, blue, 1);
            }
        }
    }
}
