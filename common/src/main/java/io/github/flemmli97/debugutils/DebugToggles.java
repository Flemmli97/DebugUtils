package io.github.flemmli97.debugutils;

import io.github.flemmli97.debugutils.network.S2CDebugToggle;
import io.github.flemmli97.debugutils.network.S2CSpawnChunk;
import net.minecraft.network.protocol.common.custom.BeeDebugPayload;
import net.minecraft.network.protocol.common.custom.BrainDebugPayload;
import net.minecraft.network.protocol.common.custom.BreezeDebugPayload;
import net.minecraft.network.protocol.common.custom.GameEventDebugPayload;
import net.minecraft.network.protocol.common.custom.GameEventListenerDebugPayload;
import net.minecraft.network.protocol.common.custom.GoalDebugPayload;
import net.minecraft.network.protocol.common.custom.HiveDebugPayload;
import net.minecraft.network.protocol.common.custom.NeighborUpdatesDebugPayload;
import net.minecraft.network.protocol.common.custom.PathfindingDebugPayload;
import net.minecraft.network.protocol.common.custom.RaidsDebugPayload;
import net.minecraft.network.protocol.common.custom.StructuresDebugPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Serverside toggles
 */
public class DebugToggles {

    public static ResourceLocation ALL = ResourceLocation.parse("debug/all");

    private static final Map<ResourceLocation, ResourcedToggle> GETTER = new TreeMap<>();

    private static final Map<UUID, Set<ResourceLocation>> PLAYER_ENABLED = new HashMap<>();

    public static final ResourcedToggle DEBUG_POI = register(ResourceLocation.parse("debug/poi"));
    public static final ResourcedToggle DEBUG_NEIGHBORSUPDATES = register(NeighborUpdatesDebugPayload.TYPE.id());
    public static final ResourcedToggle DEBUG_STRUCTURES = register(StructuresDebugPayload.TYPE.id());
    public static final ResourcedToggle DEBUG_PATHS = register(PathfindingDebugPayload.TYPE.id());
    public static final ResourcedToggle DEBUG_GOALS = register(GoalDebugPayload.TYPE.id());
    public static final ResourcedToggle DEBUG_RAIDS = register(RaidsDebugPayload.TYPE.id());
    public static final ResourcedToggle DEBUG_BRAINS = register(BrainDebugPayload.TYPE.id());
    public static final ResourcedToggle DEBUG_BEES = register(BeeDebugPayload.TYPE.id());
    public static final ResourcedToggle DEBUG_BREEZE = register(BreezeDebugPayload.TYPE.id());
    public static final ResourcedToggle DEBUG_GAME_EVENT = register(GameEventDebugPayload.TYPE.id());
    public static final ResourcedToggle DEBUG_GAME_EVENT_LISTENER = register(GameEventListenerDebugPayload.TYPE.id());
    public static final ResourcedToggle DEBUG_BEE_HIVES = register(HiveDebugPayload.TYPE.id());

    public static final ResourcedToggle DEBUG_WATER = register(ResourceLocation.parse("debug/water"));
    public static final ResourcedToggle DEBUG_HEIGHTMAP = register(ResourceLocation.parse("debug/heightmap"));
    public static final ResourcedToggle DEBUG_COLLISION = register(ResourceLocation.parse("debug/collision"));
    public static final ResourcedToggle DEBUG_SUPPORT_BLOCKS = register(ResourceLocation.parse("debug/support_blocks"));
    public static final ResourcedToggle DEBUG_LIGHT = register(ResourceLocation.parse("debug/light"));
    public static final ResourcedToggle DEBUG_SOLID_FACES = register(ResourceLocation.parse("debug/solid_faces"));
    public static final ResourcedToggle DEBUG_CHUNK = register(ResourceLocation.parse("debug/chunk"));
    public static final ResourcedToggle DEBUG_SPAWN_CHUNK = register(new ResourcedToggle(ResourceLocation.parse("debug/spawn_chunk"), (b, players) -> players.forEach(p -> {
        S2CSpawnChunk pkt = new S2CSpawnChunk(p.serverLevel());
        Network.INSTANCE.sendToClient(pkt, p);
    })));

    public static ResourcedToggle register(ResourceLocation id) {
        return register(new ResourcedToggle(id));
    }

    public static synchronized ResourcedToggle register(ResourcedToggle toggle) {
        if (GETTER.containsKey(toggle.id))
            throw new IllegalArgumentException("A toggle with id" + toggle.id + " is already registered");
        GETTER.put(toggle.id, toggle);
        return toggle;
    }

    public static ResourcedToggle get(ResourceLocation id) {
        return GETTER.get(id);
    }

    public static Collection<ResourceLocation> getRegistered() {
        return GETTER.keySet();
    }

    public static void onLogout(ServerPlayer player) {
        PLAYER_ENABLED.remove(player.getUUID());
    }

    public static void disableAll(Collection<ServerPlayer> players) {
        GETTER.values().forEach(t -> {
            if (t.get()) {
                t.toggleFor(players, false);
            }
        });
    }

    public static boolean isEnabled(Player player, ResourcedToggle toggle) {
        Set<ResourceLocation> enabled = PLAYER_ENABLED.get(player.getUUID());
        return enabled != null && enabled.contains(toggle.id);
    }

    public static class ResourcedToggle {

        public final ResourceLocation id;
        private boolean on;
        private final BiConsumer<Boolean, Collection<ServerPlayer>> onToggle;

        public ResourcedToggle(ResourceLocation id) {
            this.id = id;
            this.onToggle = null;
        }

        public ResourcedToggle(ResourceLocation id, BiConsumer<Boolean, Collection<ServerPlayer>> onToggle) {
            this.id = id;
            this.onToggle = onToggle;
        }

        public void toggleFor(Collection<ServerPlayer> players, boolean toggle) {
            this.on = toggle;
            this.updateFor(players);
            if (!this.on)
                PLAYER_ENABLED.forEach((id, enabled) -> enabled.remove(this.id));
        }

        public void updateFor(Collection<ServerPlayer> players) {
            S2CDebugToggle pkt = new S2CDebugToggle(this.id, this.on);
            players.forEach(player -> {
                Set<ResourceLocation> enabled = PLAYER_ENABLED.computeIfAbsent(player.getUUID(), k -> new HashSet<>());
                if (this.on)
                    enabled.add(this.id);
                else {
                    enabled.remove(this.id);
                    if (enabled.isEmpty())
                        PLAYER_ENABLED.remove(player.getUUID());
                }
                Network.INSTANCE.sendToClient(pkt, player);
            });
            if (this.onToggle != null) {
                if (this.on)
                    players = players.stream().filter(p -> isEnabled(p, this)).toList();
                this.onToggle.accept(this.on, players);
            }
        }

        public boolean get() {
            return this.on;
        }
    }
}
