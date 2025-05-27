package io.github.flemmli97.debugutils.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

public interface DistanceManagerTicketGetter {

    int debugUtils$getTicketLevel(TicketType type, BlockPos pos);

    /**
     * @return The ticket level for the given chunk or -1 if not present.
     */
    int debugUtils$getTicketLevel(TicketType type, ChunkPos pos);

}
