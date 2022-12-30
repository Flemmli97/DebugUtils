package io.github.flemmli97.debugutils.mixin;

import io.github.flemmli97.debugutils.utils.DebuggingPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(DebugPackets.class)
public class DebugPacketsMixin {

    @Inject(method = "sendPoiPacketsForChunk", at = @At("HEAD"))
    private static void debug_utils_debugPOI(ServerLevel level, ChunkPos chunkPos, CallbackInfo info) {
        DebuggingPackets.sendPoiPacketsForChunk(level, chunkPos);
    }

    @Inject(method = "sendPoiAddedPacket", at = @At("HEAD"))
    private static void debug_utils_poiAddedPacket(ServerLevel level, BlockPos pos, CallbackInfo info) {
        level.getPoiManager().getType(pos)
                .ifPresent(t -> DebuggingPackets.sendPoiAddedPacket(level, pos, t));
    }

    @Inject(method = "sendPoiRemovedPacket", at = @At("HEAD"))
    private static void debug_utils_poiRemovedPacket(ServerLevel level, BlockPos pos, CallbackInfo info) {
        DebuggingPackets.sendPoiRemovedPacket(level, pos);
    }

    @Inject(method = "sendPoiTicketCountPacket", at = @At("HEAD"))
    private static void debug_utils_poiTicketCountPacket(ServerLevel level, BlockPos pos, CallbackInfo info) {
        DebuggingPackets.sendPoiTicketCountPacket(level, pos);
    }

    @Inject(method = "sendPathFindingPacket", at = @At("HEAD"))
    private static void debug_utils_pathfinding(Level level, Mob mob, @Nullable Path path, float maxDistanceToWaypoint, CallbackInfo info) {
        DebuggingPackets.sendPathfindingPacket(level, mob, path, maxDistanceToWaypoint);
    }

    @Inject(method = "sendNeighborsUpdatePacket", at = @At("HEAD"))
    private static void debug_utils_neighborUpdate(Level level, BlockPos pos, CallbackInfo info) {
        DebuggingPackets.sendBlockUpdatePacket(level, pos);
    }

    @Inject(method = "sendStructurePacket", at = @At("HEAD"))
    private static void debug_utils_structurePkt(WorldGenLevel level, StructureStart structureStart, CallbackInfo info) {
        DebuggingPackets.sendStructurePacket(level, structureStart);
    }

    @Inject(method = "sendGoalSelector", at = @At("HEAD"))
    private static void debug_utils_goalSelector(Level level, Mob mob, GoalSelector goalSelector, CallbackInfo info) {
        DebuggingPackets.sendGoalPacket(level, mob, goalSelector);
    }

    @Inject(method = "sendRaids", at = @At("HEAD"))
    private static void debug_utils_Raids(ServerLevel level, Collection<Raid> raids, CallbackInfo info) {
        DebuggingPackets.sendRaids(level, raids);
    }

    @Inject(method = "sendEntityBrain", at = @At("HEAD"))
    private static void debug_utils_Brain(LivingEntity livingEntity, CallbackInfo info) {
        DebuggingPackets.sendBrainPacket(livingEntity);
    }

    @Inject(method = "sendBeeInfo", at = @At("HEAD"))
    private static void debug_utils_Bees(Bee bee, CallbackInfo info) {
        DebuggingPackets.sendBeeInfo(bee);
    }

    @Inject(method = "sendGameEventInfo", at = @At("HEAD"))
    private static void debug_utils_gameEvent(Level level, GameEvent gameEvent, BlockPos pos, CallbackInfo info) {
        DebuggingPackets.sendGameEventInfo(level, gameEvent, pos);
    }

    @Inject(method = "sendGameEventListenerInfo", at = @At("HEAD"))
    private static void debug_utils_gameEventListener(Level level, GameEventListener gameEventListener, CallbackInfo info) {
        DebuggingPackets.sendGameEventListenerInfo(level, gameEventListener);
    }

    @Inject(method = "sendHiveInfo", at = @At("HEAD"))
    private static void debug_utils_hive(Level level, BlockPos pos, BlockState blockState, BeehiveBlockEntity hiveBlockEntity, CallbackInfo info) {
        DebuggingPackets.sendHiveInfo(level, pos, blockState, hiveBlockEntity);
    }
}
