package io.github.flemmli97.debugutils.utils;

import com.google.common.collect.Lists;
import io.github.flemmli97.debugutils.DebugToggles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BeeDebugPayload;
import net.minecraft.network.protocol.common.custom.BrainDebugPayload;
import net.minecraft.network.protocol.common.custom.BreezeDebugPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.GameEventDebugPayload;
import net.minecraft.network.protocol.common.custom.GameEventListenerDebugPayload;
import net.minecraft.network.protocol.common.custom.GoalDebugPayload;
import net.minecraft.network.protocol.common.custom.HiveDebugPayload;
import net.minecraft.network.protocol.common.custom.NeighborUpdatesDebugPayload;
import net.minecraft.network.protocol.common.custom.PathfindingDebugPayload;
import net.minecraft.network.protocol.common.custom.PoiAddedDebugPayload;
import net.minecraft.network.protocol.common.custom.PoiRemovedDebugPayload;
import net.minecraft.network.protocol.common.custom.PoiTicketCountDebugPayload;
import net.minecraft.network.protocol.common.custom.RaidsDebugPayload;
import net.minecraft.network.protocol.common.custom.StructuresDebugPayload;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DebuggingPackets {

    public static void sendPoiPacketsForChunk(ServerLevel level, ChunkPos chunkPos) {
        if (DebugToggles.DEBUG_POI.get())
            level.getPoiManager().getInChunk(t -> true, chunkPos, PoiManager.Occupancy.ANY)
                    .forEach(p -> sendPoiAddedPacket(level, p.getPos(), p.getPoiType()));
    }

    @SuppressWarnings("deprecation")
    public static void sendPoiAddedPacket(ServerLevel level, BlockPos pos, Holder<PoiType> type) {
        if (DebugToggles.DEBUG_POI.get() && !level.isClientSide) {
            PoiAddedDebugPayload packet = new PoiAddedDebugPayload(pos, type.getRegisteredName(), level.getPoiManager().getFreeTickets(pos));
            sendToAll(packet, level, DebugToggles.DEBUG_POI);
        }
    }

    public static void sendPoiRemovedPacket(ServerLevel level, BlockPos pos) {
        if (DebugToggles.DEBUG_POI.get() && !level.isClientSide) {
            PoiRemovedDebugPayload packet = new PoiRemovedDebugPayload(pos);
            sendToAll(packet, level, DebugToggles.DEBUG_POI);
        }
    }

    @SuppressWarnings("deprecation")
    public static void sendPoiTicketCountPacket(ServerLevel level, BlockPos pos) {
        if (DebugToggles.DEBUG_POI.get() && !level.isClientSide) {
            PoiTicketCountDebugPayload packet = new PoiTicketCountDebugPayload(pos, level.getPoiManager().getFreeTickets(pos));
            sendToAll(packet, level, DebugToggles.DEBUG_POI);
        }
    }

    public static void sendBlockUpdatePacket(Level level, BlockPos pos) {
        if (DebugToggles.DEBUG_NEIGHBORSUPDATES.get() && !level.isClientSide) {
            NeighborUpdatesDebugPayload packet = new NeighborUpdatesDebugPayload(level.getGameTime(), pos);
            sendToAll(packet, (ServerLevel) level, DebugToggles.DEBUG_NEIGHBORSUPDATES);
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
            List<StructuresDebugPayload.PieceInfo> infos = new ArrayList<>();
            structureStart.getPieces().forEach(piece -> infos.add(new StructuresDebugPayload.PieceInfo(piece.getBoundingBox(), piece.getGenDepth() == 0)));
            StructuresDebugPayload packet = new StructuresDebugPayload(serverLevel.dimension(), structureStart.getBoundingBox(), infos);
            sendToAll(packet, serverLevel, DebugToggles.DEBUG_STRUCTURES);
        }
    }

    public static void sendPathfindingPacket(Level level, Mob mob, @Nullable Path path, float maxDistanceToWaypoint) {
        if (DebugToggles.DEBUG_PATHS.get() && !level.isClientSide && path != null && path.debugData() != null) {
            PathfindingDebugPayload packet = new PathfindingDebugPayload(mob.getId(), path, maxDistanceToWaypoint);
            sendToAll(packet, (ServerLevel) level, DebugToggles.DEBUG_PATHS);
        }
    }

    public static void sendGoalPacket(Level level, Mob mob, GoalSelector goalSelector) {
        if (DebugToggles.DEBUG_GOALS.get() && !level.isClientSide) {
            List<GoalDebugPayload.DebugGoal> goals = new ArrayList<>();
            goalSelector.getAvailableGoals().forEach(goal ->
                    goals.add(new GoalDebugPayload.DebugGoal(goal.getPriority(), goal.isRunning(), goal.getGoal().getClass().getSimpleName())));
            GoalDebugPayload packet = new GoalDebugPayload(mob.getId(), mob.blockPosition(), goals);
            sendToAll(packet, (ServerLevel) level, DebugToggles.DEBUG_GOALS);
        }
    }

    public static void sendRaids(ServerLevel level, Collection<Raid> raids) {
        if (DebugToggles.DEBUG_RAIDS.get()) {
            RaidsDebugPayload packet = new RaidsDebugPayload(raids.stream().map(Raid::getCenter).toList());
            sendToAll(packet, level, DebugToggles.DEBUG_RAIDS);
        }
    }

    @SuppressWarnings("deprecation")
    public static void sendBrainPacket(LivingEntity entity) {
        if (DebugToggles.DEBUG_BRAINS.get() && !entity.level().isClientSide) {
            Brain<?> brain = entity.getBrain();
            String profession = "";
            int xp = 0;
            String inventory = "";
            Path path = null;
            if (brain.hasMemoryValue(MemoryModuleType.PATH)) {
                path = brain.getMemory(MemoryModuleType.PATH).get();
                if (path.debugData() == null)
                    path = null;
            }
            boolean wantsGolem = false;
            int angerLevel = -1;
            if (entity instanceof Warden warden) {
                angerLevel = warden.getClientAngerLevel();
            }
            List<String> activities = brain.getActiveActivities().stream().map(Activity::getName).toList();
            List<String> behaviors = brain.getRunningBehaviors().stream().map(BehaviorControl::debugString).toList();
            List<String> memories = getMemoryDescriptions(entity, entity.level().getGameTime());
            List<String> gossips = List.of();
            Set<BlockPos> pois = Set.of();
            Set<BlockPos> potentialPois = Set.of();
            if (entity instanceof InventoryCarrier carrier) {
                inventory = carrier.getInventory().isEmpty() ? "" : carrier.getInventory().toString();
            }
            if (entity instanceof Villager villager) {
                profession = villager.getVillagerData().getProfession().name();
                xp = villager.getVillagerXp();
                wantsGolem = villager.wantsToSpawnGolem(entity.level().getGameTime());
                List<String> list = Lists.newArrayList();
                villager.getGossips().getGossipEntries().forEach((key, value) -> {
                    String string = DebugEntityNameGenerator.getEntityName(key);
                    value.forEach((gossipType, integer) -> list.add(string + ": " + gossipType + ": " + integer));
                });
                gossips = list;
                pois = Stream.of(MemoryModuleType.JOB_SITE, MemoryModuleType.HOME, MemoryModuleType.MEETING_POINT).map(brain::getMemory)
                        .flatMap(Optional::stream).map(GlobalPos::pos).collect(Collectors.toSet());
                potentialPois = brain.getMemory(MemoryModuleType.POTENTIAL_JOB_SITE)
                        .map(p -> Set.of(p.pos())).orElse(Set.of());
            }
            BrainDebugPayload.BrainDump dump = new BrainDebugPayload.BrainDump(entity.getUUID(), entity.getId(),
                    entity.getName().getString(), profession, xp, entity.getHealth(), entity.getMaxHealth(), entity.position(),
                    inventory, path, wantsGolem, angerLevel, activities, behaviors, memories, gossips, pois, potentialPois);
            BrainDebugPayload packet = new BrainDebugPayload(dump);
            sendToAll(packet, (ServerLevel) entity.level(), DebugToggles.DEBUG_BRAINS);
        }
    }

    @SuppressWarnings("deprecation")
    private static List<String> getMemoryDescriptions(LivingEntity entity, long gameTime) {
        Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> map = entity.getBrain().getMemories();
        List<String> list = Lists.newArrayList();
        for (Map.Entry<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> entry : map.entrySet()) {
            MemoryModuleType<?> memoryModuleType = entry.getKey();
            Optional<? extends ExpirableValue<?>> optional = entry.getValue();
            String string;
            if (optional.isPresent()) {
                ExpirableValue<?> expirableValue = optional.get();
                Object object = expirableValue.getValue();
                if (memoryModuleType == MemoryModuleType.HEARD_BELL_TIME) {
                    long l = gameTime - (Long) object;
                    string = l + " ticks ago";
                } else if (expirableValue.canExpire()) {
                    String desc = getShortDescription((ServerLevel) entity.level(), object);
                    string = desc + " (ttl: " + expirableValue.getTimeToLive() + ")";
                } else {
                    string = getShortDescription((ServerLevel) entity.level(), object);
                }
            } else {
                string = "-";
            }
            String id = BuiltInRegistries.MEMORY_MODULE_TYPE.getKey(memoryModuleType).getPath();
            list.add(id + ": " + string);
        }
        list.sort(String::compareTo);
        return list;
    }

    private static String getShortDescription(ServerLevel level, @Nullable Object object) {
        if (object == null) {
            return "-";
        } else if (object instanceof UUID) {
            return getShortDescription(level, level.getEntity((UUID) object));
        } else {
            Entity entity;
            if (object instanceof LivingEntity) {
                entity = (Entity) object;
                return DebugEntityNameGenerator.getEntityName(entity);
            } else if (object instanceof Nameable) {
                return ((Nameable) object).getName().getString();
            } else if (object instanceof WalkTarget) {
                return getShortDescription(level, ((WalkTarget) object).getTarget());
            } else if (object instanceof EntityTracker) {
                return getShortDescription(level, ((EntityTracker) object).getEntity());
            } else if (object instanceof GlobalPos) {
                return getShortDescription(level, ((GlobalPos) object).pos());
            } else if (object instanceof BlockPosTracker) {
                return getShortDescription(level, ((BlockPosTracker) object).currentBlockPosition());
            } else if (object instanceof DamageSource) {
                entity = ((DamageSource) object).getEntity();
                return entity == null ? object.toString() : getShortDescription(level, entity);
            } else if (!(object instanceof Collection<?> iterable)) {
                return object.toString();
            } else {
                List<String> list = Lists.newArrayList();
                iterable.forEach(o -> list.add(getShortDescription(level, o)));
                return list.toString();
            }
        }
    }

    public static void sendBeeInfo(Bee bee) {
        if (DebugToggles.DEBUG_BEES.get() && !bee.level().isClientSide) {
            BeeDebugPayload.BeeInfo info = new BeeDebugPayload.BeeInfo(
                    bee.getUUID(), bee.getId(), bee.position(), bee.getNavigation().getPath(), bee.getHivePos(),
                    bee.getSavedFlowerPos(), bee.getTravellingTicks(), bee.getGoalSelector().getAvailableGoals().stream().map(g -> g.getGoal().toString()).collect(Collectors.toSet()), bee.getBlacklistedHives()
            );
            BeeDebugPayload packet = new BeeDebugPayload(info);
            sendToAll(packet, (ServerLevel) bee.level(), DebugToggles.DEBUG_BEES);
        }
    }

    public static void sendBreezeInfo(Breeze breeze) {
        if (DebugToggles.DEBUG_BREEZE.get() && !breeze.level().isClientSide) {
            BreezeDebugPayload packet = new BreezeDebugPayload(new BreezeDebugPayload.BreezeInfo(breeze.getUUID(), breeze.getId(), breeze.getTarget() != null ?
                    breeze.getTarget().getId() : null, breeze.getBrain().getMemory(MemoryModuleType.BREEZE_JUMP_TARGET).orElse(null)));
            sendToAll(packet, (ServerLevel) breeze.level(), DebugToggles.DEBUG_BREEZE);
        }
    }

    public static void sendGameEventInfo(Level level, Holder<GameEvent> gameEvent, Vec3 pos) {
        if (DebugToggles.DEBUG_GAME_EVENT.get() && !level.isClientSide) {
            GameEventDebugPayload packet = new GameEventDebugPayload(gameEvent.unwrapKey().get(), pos);
            sendToAll(packet, (ServerLevel) level, DebugToggles.DEBUG_GAME_EVENT);
        }
    }

    public static void sendGameEventListenerInfo(Level level, GameEventListener gameEventListener) {
        if (DebugToggles.DEBUG_GAME_EVENT_LISTENER.get() && !level.isClientSide) {
            GameEventListenerDebugPayload packet = new GameEventListenerDebugPayload(gameEventListener.getListenerSource(), gameEventListener.getListenerRadius());
            sendToAll(packet, (ServerLevel) level, DebugToggles.DEBUG_GAME_EVENT_LISTENER);
        }
    }

    public static void sendHiveInfo(Level level, BlockPos pos, BlockState blockState, BeehiveBlockEntity hiveBlockEntity) {
        if (DebugToggles.DEBUG_BEE_HIVES.get() && !level.isClientSide) {
            HiveDebugPayload packet = new HiveDebugPayload(new HiveDebugPayload.HiveInfo(pos, BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(hiveBlockEntity.getType()).toString(),
                    hiveBlockEntity.getOccupantCount(), blockState.getValue(BeehiveBlock.HONEY_LEVEL), hiveBlockEntity.isSedated()));
            sendToAll(packet, (ServerLevel) level, DebugToggles.DEBUG_BEE_HIVES);
        }
    }

    private static void sendToAll(CustomPacketPayload pkt, ServerLevel level, DebugToggles.ResourcedToggle toggle) {
        level.players().forEach(p -> {
            if (DebugToggles.isEnabled(p, toggle))
                p.connection.send(new ClientboundCustomPayloadPacket(pkt));
        });
    }
}
