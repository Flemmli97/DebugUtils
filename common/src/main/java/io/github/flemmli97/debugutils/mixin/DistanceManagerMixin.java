package io.github.flemmli97.debugutils.mixin;

import io.github.flemmli97.debugutils.utils.DistanceManagerTicketGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.TicketStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(DistanceManager.class)
public abstract class DistanceManagerMixin implements DistanceManagerTicketGetter {

    @Shadow
    @Final
    TicketStorage ticketStorage;

    @Override
    public int debugUtils$getTicketLevel(TicketType type, BlockPos pos) {
        return this.debugUtils$getTicketLevel(type, new ChunkPos(pos));
    }

    @Override
    public int debugUtils$getTicketLevel(TicketType type, ChunkPos pos) {
        List<Ticket> sortedArraySet = this.ticketStorage.getTickets(pos.toLong());
        int ticketLevel = -1;
        for (Ticket ticket : sortedArraySet)
            if (ticket.getType() == TicketType.START && ticket.getTicketLevel() > ticketLevel)
                ticketLevel = ticket.getTicketLevel();
        return ticketLevel;
    }
}
