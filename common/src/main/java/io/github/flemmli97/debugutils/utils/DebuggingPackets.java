package io.github.flemmli97.debugutils.utils;

import io.github.flemmli97.debugutils.DebugToggles;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSourceType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class DebuggingPackets {

    public static void sendPoiPacketsForChunk(ServerLevel level, ChunkPos chunkPos) {
        if (DebugToggles.DEBUG_POI.get())
            level.getPoiManager().getInChunk(t -> true, chunkPos, PoiManager.Occupancy.ANY)
                    .forEach(p -> sendPoiAddedPacket(level, p.getPos(), p.getPoiType()));
    }

    public static void sendPoiAddedPacket(ServerLevel level, BlockPos pos, PoiType type) {
        if (DebugToggles.DEBUG_POI.get() && !level.isClientSide) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeBlockPos(pos);
            buf.writeUtf(type.getName());
            buf.writeInt(type.getMaxTickets());
            ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(ClientboundCustomPayloadPacket.DEBUG_POI_ADDED_PACKET, buf);
            sendToAll(packet, level);
        }
    }

    public static void sendPoiRemovedPacket(ServerLevel level, BlockPos pos) {
        if (DebugToggles.DEBUG_POI.get() && !level.isClientSide) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeBlockPos(pos);
            ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(ClientboundCustomPayloadPacket.DEBUG_POI_REMOVED_PACKET, buf);
            sendToAll(packet, level);
        }
    }

    @SuppressWarnings("deprecation")
    public static void sendPoiTicketCountPacket(ServerLevel level, BlockPos pos) {
        if (DebugToggles.DEBUG_POI.get() && !level.isClientSide) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeBlockPos(pos);
            buf.writeInt(level.getPoiManager().getFreeTickets(pos));
            ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(ClientboundCustomPayloadPacket.DEBUG_POI_TICKET_COUNT_PACKET, buf);
            sendToAll(packet, level);
        }
    }

    public static void sendBlockUpdatePacket(Level level, BlockPos pos) {
        if (DebugToggles.DEBUG_NEIGHBORSUPDATES.get() && !level.isClientSide) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeVarLong(level.getGameTime());
            buf.writeBlockPos(pos);
            ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(ClientboundCustomPayloadPacket.DEBUG_NEIGHBORSUPDATE_PACKET, buf);
            sendToAll(packet, (ServerLevel) level);
        }
    }

    /**
     * Vanilla only calls this during chunk generation which uses a {@link WorldGenRegion}.
     * Note the playerlist from WorldGenRegion is always empty so the ServerLevel is got instead.
     * Adding {@link ServerLevel} check too in case some mod want to send it
     */
    @SuppressWarnings("deprecation")
    public static void sendStructurePacket(WorldGenLevel level, StructureStart structureStart) {
        if (!DebugToggles.DEBUG_STRUCTURES.get())
            return;
        ServerLevel serverLevel = null;
        if (level instanceof WorldGenRegion region) {
            if (region.getLevel().players().isEmpty())
                return;
            serverLevel = region.getLevel();
        } else if (level instanceof ServerLevel) {
            serverLevel = (ServerLevel) level;
        }
        if (serverLevel != null) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeResourceLocation(serverLevel.dimension().location());
            writeBBToBuffer(structureStart.getBoundingBox(), buf);
            buf.writeInt(structureStart.getPieces().size());
            structureStart.getPieces().forEach(piece -> {
                writeBBToBuffer(piece.getBoundingBox(), buf);
                buf.writeBoolean(piece.getGenDepth() == 0); //Assume its this? Since the boolean is used as start piece on the handler
            });
            ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(ClientboundCustomPayloadPacket.DEBUG_STRUCTURES_PACKET, buf);
            sendToAll(packet, serverLevel);
        }
    }

    private static void writeBBToBuffer(BoundingBox box, FriendlyByteBuf buf) {
        buf.writeInt(box.minX());
        buf.writeInt(box.minY());
        buf.writeInt(box.minZ());
        buf.writeInt(box.maxX());
        buf.writeInt(box.maxY());
        buf.writeInt(box.maxZ());
    }

    public static void sendPathfindingPacket(Level level, Mob mob, @Nullable Path path, float maxDistanceToWaypoint) {
        if (DebugToggles.DEBUG_PATHS.get() && !level.isClientSide && path != null) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeInt(mob.getId());
            buf.writeFloat(maxDistanceToWaypoint);
            path.writeToStream(buf);
            ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(ClientboundCustomPayloadPacket.DEBUG_PATHFINDING_PACKET, buf);
            sendToAll(packet, (ServerLevel) level);
        }
    }

    public static void sendGoalPacket(Level level, Mob mob, GoalSelector goalSelector) {
        if (DebugToggles.DEBUG_GOALS.get() && !level.isClientSide) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeBlockPos(mob.blockPosition());
            buf.writeInt(mob.getId());
            buf.writeInt(goalSelector.getAvailableGoals().size());
            goalSelector.getAvailableGoals().forEach(w -> {
                buf.writeInt(w.getPriority());
                buf.writeBoolean(w.isRunning());
                buf.writeUtf(w.getGoal().toString());
            });
            ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(ClientboundCustomPayloadPacket.DEBUG_GOAL_SELECTOR, buf);
            sendToAll(packet, (ServerLevel) level);
        }
    }

    public static void sendRaids(ServerLevel level, Collection<Raid> raids) {
        if (DebugToggles.DEBUG_RAIDS.get()) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeInt(raids.size());
            raids.forEach(raid -> buf.writeBlockPos(raid.getCenter()));
            ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(ClientboundCustomPayloadPacket.DEBUG_RAIDS, buf);
            sendToAll(packet, level);
        }
    }

    public static void sendBrainPacket(LivingEntity entity) {
        if (DebugToggles.DEBUG_BRAINS.get() && !entity.level.isClientSide) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            Vec3 pos = entity.position();
            buf.writeDouble(pos.x());
            buf.writeDouble(pos.y());
            buf.writeDouble(pos.z());
            buf.writeUUID(entity.getUUID());
            buf.writeInt(entity.getId());
            buf.writeUtf(entity.getName().getString());
            if (entity instanceof Villager villager) {
                buf.writeUtf(villager.getVillagerData().getProfession().getName());
                buf.writeInt(villager.getVillagerXp());
            } else {
                buf.writeUtf("");
                buf.writeInt(0);
            }
            buf.writeFloat(entity.getHealth());
            buf.writeFloat(entity.getMaxHealth());

            DebugPackets.writeBrain(entity, buf);
            ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(ClientboundCustomPayloadPacket.DEBUG_BRAIN, buf);
            sendToAll(packet, (ServerLevel) entity.level);
        }
    }

    public static void sendBeeInfo(Bee bee) {
        if (DebugToggles.DEBUG_BEES.get() && !bee.level.isClientSide) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            Vec3 pos = bee.position();
            buf.writeDouble(pos.x());
            buf.writeDouble(pos.y());
            buf.writeDouble(pos.z());
            buf.writeUUID(bee.getUUID());
            buf.writeInt(bee.getId());

            if (bee.hasHive()) {
                buf.writeBoolean(true);
                buf.writeBlockPos(bee.getHivePos());
            } else
                buf.writeBoolean(false);

            if (bee.hasSavedFlowerPos()) {
                buf.writeBoolean(true);
                buf.writeBlockPos(bee.getSavedFlowerPos());
            } else
                buf.writeBoolean(false);

            buf.writeInt(bee.getTravellingTicks());
            //I have no idea why the client reads wrong data about it...
            //commented out for now. pathing is also already debugged
            //Path path = bee.getNavigation().getPath();
            //buf.writeBoolean(path != null);
            //if (path != null) {
            //    buf.writeBoolean(true);
            //    path.writeToStream(buf);
            //} else
            buf.writeBoolean(false);

            buf.writeInt(bee.getGoalSelector().getAvailableGoals().size());
            bee.getGoalSelector().getAvailableGoals().forEach(w -> buf.writeUtf(w.getGoal().toString()));

            buf.writeInt(bee.getBlacklistedHives().size());
            bee.getBlacklistedHives().forEach(h -> buf.writeUtf(h.toShortString()));

            ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(ClientboundCustomPayloadPacket.DEBUG_BEE, buf);
            sendToAll(packet, (ServerLevel) bee.level);
        }
    }

    public static void sendGameEventInfo(Level level, GameEvent gameEvent, BlockPos pos) {
        if (DebugToggles.DEBUG_GAME_EVENT.get() && !level.isClientSide) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeResourceLocation(Registry.GAME_EVENT.getKey(gameEvent));
            buf.writeBlockPos(pos);
            ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(ClientboundCustomPayloadPacket.DEBUG_GAME_EVENT, buf);
            sendToAll(packet, (ServerLevel) level);
        }
    }

    public static void sendGameEventListenerInfo(Level level, GameEventListener gameEventListener) {
        if (DebugToggles.DEBUG_GAME_EVENT_LISTENER.get() && !level.isClientSide) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            PositionSourceType.toNetwork(gameEventListener.getListenerSource(), buf);
            buf.writeInt(gameEventListener.getListenerRadius());
            ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(ClientboundCustomPayloadPacket.DEBUG_GAME_EVENT_LISTENER, buf);
            sendToAll(packet, (ServerLevel) level);
        }
    }

    public static void sendHiveInfo(Level level, BlockPos pos, BlockState blockState, BeehiveBlockEntity hiveBlockEntity) {
        if (DebugToggles.DEBUG_BEE_HIVES.get() && !level.isClientSide) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeBlockPos(pos);
            buf.writeUtf(Registry.BLOCK_ENTITY_TYPE.getKey(hiveBlockEntity.getType()).toString());
            buf.writeInt(hiveBlockEntity.getOccupantCount());
            buf.writeInt(blockState.getValue(BeehiveBlock.HONEY_LEVEL));
            buf.writeBoolean(hiveBlockEntity.isSedated());
            ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(ClientboundCustomPayloadPacket.DEBUG_HIVE, buf);
            sendToAll(packet, (ServerLevel) level);
        }
    }

    private static void sendToAll(Packet<?> pkt, ServerLevel level) {
        level.players().forEach(p -> p.connection.send(pkt));
    }
}
