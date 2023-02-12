package io.github.flemmli97.debugutils.mixin;

import io.github.flemmli97.debugutils.utils.DistanceManagerTicketGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DistanceManager.class)
public abstract class DistanceManagerMixin implements DistanceManagerTicketGetter {

    @Override
    public int debugUtils$getTicketLevel(TicketType<?> type, BlockPos pos) {
        return this.debugUtils$getTicketLevel(type, new ChunkPos(pos));
    }

    @Override
    public int debugUtils$getTicketLevel(TicketType<?> type, ChunkPos pos) {
        SortedArraySet<Ticket<?>> sortedArraySet = this.getTickets(pos.toLong());
        int ticketLevel = 0;
        for (Ticket<?> ticket : sortedArraySet)
            if (ticket.getType() == TicketType.START && ticket.getTicketLevel() > ticketLevel)
                ticketLevel = ticket.getTicketLevel();
        return ticketLevel;
    }

    @Shadow
    abstract SortedArraySet<Ticket<?>> getTickets(long l);
}
