package io.github.flemmli97.debugutils.network;

import io.github.flemmli97.debugutils.DebugUtils;
import io.github.flemmli97.debugutils.client.spawnchunks.SpawnChunkRenderer;
import io.github.flemmli97.debugutils.utils.DistanceManagerTicketGetter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;

public class S2CSpawnChunk implements Packet {

    public static final ResourceLocation ID = new ResourceLocation(DebugUtils.MODID, "s2c_spawn_chunk");

    private final int ticketLevel;

    public S2CSpawnChunk(ServerLevel level) {
        this(((DistanceManagerTicketGetter) level.getChunkSource().chunkMap.getDistanceManager()).debugUtils$getTicketLevel(TicketType.START, level.getSharedSpawnPos()));
    }

    private S2CSpawnChunk(int ticketLevel) {
        this.ticketLevel = ticketLevel;
    }

    public static S2CSpawnChunk read(FriendlyByteBuf buf) {
        return new S2CSpawnChunk(buf.readInt());
    }

    public static void handle(S2CSpawnChunk pkt) {
        SpawnChunkRenderer.INSTANCE.updateSpawnChunk(pkt.ticketLevel);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.ticketLevel);
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }
}
