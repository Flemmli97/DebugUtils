package io.github.flemmli97.debugutils.utils;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * From {@link DebugPackets#writeBrain(LivingEntity, FriendlyByteBuf)}
 */
public class BrainUtils {

    @SuppressWarnings("deprecation")
    public static void writeBrain(LivingEntity livingEntity, FriendlyByteBuf buffer) {
        Brain<?> brain = livingEntity.getBrain();
        long l = livingEntity.level.getGameTime();
        if (livingEntity instanceof InventoryCarrier carrier) {
            Container container = carrier.getInventory();
            buffer.writeUtf(container.isEmpty() ? "" : container.toString());
        } else {
            buffer.writeUtf("");
        }

        if (brain.hasMemoryValue(MemoryModuleType.PATH)) {
            buffer.writeBoolean(true);
            Path path = brain.getMemory(MemoryModuleType.PATH).get();
            path.writeToStream(buffer);
        } else {
            buffer.writeBoolean(false);
        }

        if (livingEntity instanceof Villager villager) {
            buffer.writeBoolean(villager.wantsToSpawnGolem(l));
        } else {
            buffer.writeBoolean(false);
        }

        buffer.writeCollection(brain.getActiveActivities(), (friendlyByteBuf, activity) -> friendlyByteBuf.writeUtf(activity.getName()));
        Set<String> running = brain.getRunningBehaviors().stream().map(Behavior::toString).collect(Collectors.toSet());
        buffer.writeCollection(running, FriendlyByteBuf::writeUtf);
        buffer.writeCollection(getMemoryDescriptions(livingEntity, l), (friendlyByteBuf, string) -> {
            String string2 = StringUtil.truncateStringIfNecessary(string, 255, true);
            friendlyByteBuf.writeUtf(string2);
        });
        Set<BlockPos> pois = Stream.of(MemoryModuleType.JOB_SITE, MemoryModuleType.HOME, MemoryModuleType.MEETING_POINT)
                .map(brain::getMemory).flatMap(Optional::stream).map(GlobalPos::pos).collect(Collectors.toSet());
        if (!pois.isEmpty()) {
            buffer.writeCollection(pois, FriendlyByteBuf::writeBlockPos);
        } else {
            buffer.writeVarInt(0);
        }
        Set<BlockPos> jobs = Stream.of(MemoryModuleType.POTENTIAL_JOB_SITE).map(brain::getMemory).flatMap(Optional::stream).map(GlobalPos::pos).collect(Collectors.toSet());
        if (!jobs.isEmpty()) {
            buffer.writeCollection(jobs, FriendlyByteBuf::writeBlockPos);
        } else {
            buffer.writeVarInt(0);
        }

        if (livingEntity instanceof Villager villager) {
            Map<UUID, Object2IntMap<GossipType>> gossips = villager.getGossips().getGossipEntries();
            List<String> list = Lists.newArrayList();
            gossips.forEach((uUID, object2IntMap) -> {
                String string = DebugEntityNameGenerator.getEntityName(uUID);
                object2IntMap.forEach((gossipType, integer) -> list.add(string + ": " + gossipType + ": " + integer));
            });
            buffer.writeCollection(list, FriendlyByteBuf::writeUtf);
        } else {
            buffer.writeVarInt(0);
        }
    }

    @SuppressWarnings("deprecation")
    private static List<String> getMemoryDescriptions(LivingEntity livingEntity, long l) {
        Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> map = livingEntity.getBrain().getMemories();
        List<String> list = Lists.newArrayList();

        for (Map.Entry<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> entry : map.entrySet()) {
            MemoryModuleType<?> memoryModuleType = entry.getKey();
            Optional<? extends ExpirableValue<?>> val = entry.getValue();
            String string;
            if (val.isPresent()) {
                ExpirableValue<?> expirableValue = val.get();
                Object object = expirableValue.getValue();
                if (memoryModuleType == MemoryModuleType.HEARD_BELL_TIME) {
                    long m = l - (Long) object;
                    string = m + " ticks ago";
                } else if (expirableValue.canExpire()) {
                    String var10000 = getShortDescription((ServerLevel) livingEntity.level, object);
                    string = var10000 + " (ttl: " + expirableValue.getTimeToLive() + ")";
                } else {
                    string = getShortDescription((ServerLevel) livingEntity.level, object);
                }
            } else {
                string = "-";
            }

            String moduleID = Registry.MEMORY_MODULE_TYPE.getKey(memoryModuleType).getPath();
            list.add(moduleID + ": " + string);
        }

        list.sort(String::compareTo);
        return list;
    }

    private static String getShortDescription(ServerLevel serverLevel, @Nullable Object object) {
        if (object == null) {
            return "-";
        } else if (object instanceof UUID uuid) {
            return getShortDescription(serverLevel, serverLevel.getEntity(uuid));
        } else {
            if (object instanceof LivingEntity living) {
                return DebugEntityNameGenerator.getEntityName(living);
            } else if (object instanceof Nameable nameable) {
                return nameable.getName().getString();
            } else if (object instanceof WalkTarget target) {
                return getShortDescription(serverLevel, target.getTarget());
            } else if (object instanceof EntityTracker tracker) {
                return getShortDescription(serverLevel, tracker.getEntity());
            } else if (object instanceof GlobalPos pos) {
                return getShortDescription(serverLevel, pos.pos());
            } else if (object instanceof BlockPosTracker tracker) {
                return getShortDescription(serverLevel, tracker.currentBlockPosition());
            } else if (object instanceof EntityDamageSource damageSource) {
                Entity entity = damageSource.getEntity();
                return entity == null ? damageSource.toString() : getShortDescription(serverLevel, entity);
            } else if (!(object instanceof Collection<?> collection)) {
                return object.toString();
            } else {
                List<String> list = Lists.newArrayList();
                for (Object object2 : collection) {
                    list.add(getShortDescription(serverLevel, object2));
                }
                return list.toString();
            }
        }
    }
}
