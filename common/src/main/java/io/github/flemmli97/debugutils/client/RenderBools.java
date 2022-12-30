package io.github.flemmli97.debugutils.client;

import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
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
        HANDLERS.put(new ResourceLocation("debug/poi"), b -> DEBUG_POI = b);
        HANDLERS.put(ClientboundCustomPayloadPacket.DEBUG_NEIGHBORSUPDATE_PACKET, b -> DEBUG_BLOCKUPDATES = b);
        HANDLERS.put(ClientboundCustomPayloadPacket.DEBUG_STRUCTURES_PACKET, b -> DEBUG_STRUCTURES = b);
        HANDLERS.put(ClientboundCustomPayloadPacket.DEBUG_PATHFINDING_PACKET, b -> DEBUG_PATHS = b);
        HANDLERS.put(ClientboundCustomPayloadPacket.DEBUG_GOAL_SELECTOR, b -> DEBUG_GOALS = b);
        HANDLERS.put(ClientboundCustomPayloadPacket.DEBUG_RAIDS, b -> DEBUG_RAIDS = b);
        HANDLERS.put(ClientboundCustomPayloadPacket.DEBUG_BRAIN, b -> DEBUG_BRAIN = b);
        HANDLERS.put(ClientboundCustomPayloadPacket.DEBUG_BEE, b -> DEBUG_BEE = b);
        HANDLERS.put(ClientboundCustomPayloadPacket.DEBUG_GAME_EVENT, b -> DEBUG_GAME_EVENT = b);
        HANDLERS.put(ClientboundCustomPayloadPacket.DEBUG_GAME_EVENT_LISTENER, b -> DEBUG_GAME_EVENT_LISTENER = b);
        HANDLERS.put(ClientboundCustomPayloadPacket.DEBUG_HIVE, b -> DEBUG_HIVE = b);

        HANDLERS.put(new ResourceLocation("debug/water"), b -> DEBUG_WATER = b);
        HANDLERS.put(new ResourceLocation("debug/heightmap"), b -> DEBUG_HEIGHTMAP = b);
        HANDLERS.put(new ResourceLocation("debug/collision"), b -> DEBUG_COLLISION = b);
        HANDLERS.put(new ResourceLocation("debug/light"), b -> DEBUG_LIGHT = b);
        HANDLERS.put(new ResourceLocation("debug/solid_faces"), b -> DEBUG_SOLID_FACES = b);
        HANDLERS.put(new ResourceLocation("debug/chunk"), b -> DEBUG_CHUNK = b);
    }

    public static boolean DEBUG_POI;
    public static boolean DEBUG_BLOCKUPDATES;
    public static boolean DEBUG_STRUCTURES;
    public static boolean DEBUG_PATHS;
    public static boolean DEBUG_GOALS;
    public static boolean DEBUG_RAIDS;
    public static boolean DEBUG_BRAIN;
    public static boolean DEBUG_BEE;
    public static boolean DEBUG_GAME_EVENT;
    public static boolean DEBUG_GAME_EVENT_LISTENER;
    public static boolean DEBUG_HIVE;

    //Below are those that dont need server data (aka no packets)
    public static boolean DEBUG_WATER;
    public static boolean DEBUG_HEIGHTMAP;
    public static boolean DEBUG_COLLISION;
    public static boolean DEBUG_LIGHT;
    public static boolean DEBUG_SOLID_FACES;
    public static boolean DEBUG_CHUNK;

    public static void onDisconnet() {
        HANDLERS.values().forEach(c -> c.accept(false));
    }
}
