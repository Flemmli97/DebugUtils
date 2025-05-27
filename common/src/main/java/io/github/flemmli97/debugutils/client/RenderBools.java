package io.github.flemmli97.debugutils.client;

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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Client side toggles
 */
public class RenderBools {

    public static final Map<ResourceLocation, Consumer<Boolean>> HANDLERS = new HashMap<>();

    static {
        HANDLERS.put(ResourceLocation.parse("debug/poi"), b -> DEBUG_POI = b);
        HANDLERS.put(NeighborUpdatesDebugPayload.TYPE.id(), b -> DEBUG_BLOCKUPDATES = b);
        HANDLERS.put(StructuresDebugPayload.TYPE.id(), b -> DEBUG_STRUCTURES = b);
        HANDLERS.put(PathfindingDebugPayload.TYPE.id(), b -> DEBUG_PATHS = b);
        HANDLERS.put(GoalDebugPayload.TYPE.id(), b -> DEBUG_GOALS = b);
        HANDLERS.put(RaidsDebugPayload.TYPE.id(), b -> DEBUG_RAIDS = b);
        HANDLERS.put(BrainDebugPayload.TYPE.id(), b -> DEBUG_BRAIN = b);
        HANDLERS.put(BeeDebugPayload.TYPE.id(), b -> DEBUG_BEE = b);
        HANDLERS.put(BreezeDebugPayload.TYPE.id(), b -> DEBUG_BREEZE = b);
        HANDLERS.put(GameEventDebugPayload.TYPE.id(), b -> DEBUG_GAME_EVENT = b);
        HANDLERS.put(GameEventListenerDebugPayload.TYPE.id(), b -> DEBUG_GAME_EVENT_LISTENER = b);
        HANDLERS.put(HiveDebugPayload.TYPE.id(), b -> DEBUG_HIVE = b);

        HANDLERS.put(ResourceLocation.parse("debug/water"), b -> DEBUG_WATER = b);
        HANDLERS.put(ResourceLocation.parse("debug/heightmap"), b -> DEBUG_HEIGHTMAP = b);
        HANDLERS.put(ResourceLocation.parse("debug/collision"), b -> DEBUG_COLLISION = b);
        HANDLERS.put(ResourceLocation.parse("debug/support_blocks"), b -> DEBUG_SUPPORT_BLOCKS = b);
        HANDLERS.put(ResourceLocation.parse("debug/light"), b -> DEBUG_LIGHT = b);
        HANDLERS.put(ResourceLocation.parse("debug/solid_faces"), b -> DEBUG_SOLID_FACES = b);
        HANDLERS.put(ResourceLocation.parse("debug/chunk"), b -> DEBUG_CHUNK = b);
        HANDLERS.put(ResourceLocation.parse("debug/spawn_chunk"), b -> DEBUG_SPAWN_CHUNK = b);
    }

    public static boolean DEBUG_POI;
    public static boolean DEBUG_BLOCKUPDATES;
    public static boolean DEBUG_STRUCTURES;
    public static boolean DEBUG_PATHS;
    public static boolean DEBUG_GOALS;
    public static boolean DEBUG_RAIDS;
    public static boolean DEBUG_BRAIN;
    public static boolean DEBUG_BEE;
    public static boolean DEBUG_BREEZE;
    public static boolean DEBUG_GAME_EVENT;
    public static boolean DEBUG_GAME_EVENT_LISTENER;
    public static boolean DEBUG_HIVE;

    //Below are those that dont need server data (aka no packets)
    public static boolean DEBUG_WATER;
    public static boolean DEBUG_HEIGHTMAP;
    public static boolean DEBUG_COLLISION;
    public static boolean DEBUG_SUPPORT_BLOCKS;
    public static boolean DEBUG_LIGHT;
    public static boolean DEBUG_SOLID_FACES;
    public static boolean DEBUG_CHUNK;
    public static boolean DEBUG_SPAWN_CHUNK;

    public static synchronized void registerClientHandler(ResourceLocation id, Consumer<Boolean> consumer) {
        if (HANDLERS.containsKey(id))
            throw new IllegalArgumentException("A toggle with id" + id + " is already registered");
        HANDLERS.put(id, consumer);
    }

    public static void onDisconnect() {
        HANDLERS.values().forEach(c -> c.accept(false));
    }
}
