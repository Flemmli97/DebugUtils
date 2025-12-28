package io.github.flemmli97.debugutils.client;

import io.github.flemmli97.debugutils.api.DebugRenderHolder;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.BeeDebugRenderer;
import net.minecraft.client.renderer.debug.BrainDebugRenderer;
import net.minecraft.client.renderer.debug.BreezeDebugRenderer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.debug.EntityBlockIntersectionDebugRenderer;
import net.minecraft.client.renderer.debug.GameEventListenerRenderer;
import net.minecraft.client.renderer.debug.GoalSelectorDebugRenderer;
import net.minecraft.client.renderer.debug.NeighborsUpdateRenderer;
import net.minecraft.client.renderer.debug.PathfindingRenderer;
import net.minecraft.client.renderer.debug.PoiDebugRenderer;
import net.minecraft.client.renderer.debug.RaidDebugRenderer;
import net.minecraft.client.renderer.debug.RedstoneWireOrientationsRenderer;
import net.minecraft.client.renderer.debug.StructureRenderer;
import net.minecraft.client.renderer.debug.VillageSectionsDebugRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.debug.DebugSubscription;
import net.minecraft.util.debug.DebugSubscriptions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Client side toggles
 */
public class DebugRenderHandler {

    private static final Map<DebugSubscription<?>, DebugRenderHolder> HANDLERS = new HashMap<>();

    private static final Set<DebugSubscription<?>> ENABLED_SUBSCRIPTION = new HashSet<>();

    static {
        registerHandler(DebugSubscriptions.ENTITY_PATHS, new DebugRenderHolder(() -> SharedConstants.DEBUG_PATHFINDING, PathfindingRenderer::new));
        registerHandler(DebugSubscriptions.NEIGHBOR_UPDATES, new DebugRenderHolder(() -> SharedConstants.DEBUG_NEIGHBORSUPDATE, NeighborsUpdateRenderer::new));
        registerHandler(DebugSubscriptions.REDSTONE_WIRE_ORIENTATIONS, new DebugRenderHolder(() -> SharedConstants.DEBUG_EXPERIMENTAL_REDSTONEWIRE_UPDATE_ORDER, RedstoneWireOrientationsRenderer::new));
        registerHandler(DebugSubscriptions.STRUCTURES, new DebugRenderHolder(() -> SharedConstants.DEBUG_STRUCTURES, StructureRenderer::new));
        registerHandler(DebugSubscriptions.VILLAGE_SECTIONS, new DebugRenderHolder(() -> SharedConstants.DEBUG_VILLAGE_SECTIONS, VillageSectionsDebugRenderer::new));
        registerHandler(DebugSubscriptions.BRAINS, new DebugRenderHolder(() -> SharedConstants.DEBUG_BRAIN, BrainDebugRenderer::new));
        registerHandler(DebugSubscriptions.POIS, new DebugRenderHolder(() -> SharedConstants.DEBUG_POI, minecraft -> new PoiDebugRenderer(new BrainDebugRenderer(minecraft))));
        registerHandler(DebugSubscriptions.BEES, new DebugRenderHolder(() -> SharedConstants.DEBUG_BEES, BeeDebugRenderer::new));
        registerHandler(DebugSubscriptions.RAIDS, new DebugRenderHolder(() -> SharedConstants.DEBUG_RAIDS, RaidDebugRenderer::new));
        registerHandler(DebugSubscriptions.GOAL_SELECTORS, new DebugRenderHolder(() -> SharedConstants.DEBUG_GOAL_SELECTOR, GoalSelectorDebugRenderer::new));
        registerHandler(DebugSubscriptions.GAME_EVENT_LISTENERS, new DebugRenderHolder(() -> SharedConstants.DEBUG_GAME_EVENT_LISTENERS, GameEventListenerRenderer::new));
        registerHandler(DebugSubscriptions.GAME_EVENTS, new DebugRenderHolder(() -> SharedConstants.DEBUG_GAME_EVENT_LISTENERS, mc -> HANDLERS.get(BuiltInRegistries.DEBUG_SUBSCRIPTION.getKey(DebugSubscriptions.GAME_EVENT_LISTENERS)).factory().apply(mc)));
        registerHandler(DebugSubscriptions.BREEZES, new DebugRenderHolder(() -> SharedConstants.DEBUG_BREEZE_MOB, BreezeDebugRenderer::new));
        registerHandler(DebugSubscriptions.ENTITY_BLOCK_INTERSECTIONS, new DebugRenderHolder(() -> SharedConstants.DEBUG_ENTITY_BLOCK_INTERSECTION, EntityBlockIntersectionDebugRenderer::new));
    }

    public static synchronized void registerHandler(DebugSubscription<?> subscription, DebugRenderHolder holder) {
        Identifier id = BuiltInRegistries.DEBUG_SUBSCRIPTION.getKey(subscription);
        if (id == null) {
            throw new IllegalStateException("No such subscription registered" + subscription + "!");
        }
        if (HANDLERS.containsKey(subscription)) {
            throw new IllegalStateException("Handler with given id " + BuiltInRegistries.DEBUG_SUBSCRIPTION.getKey(subscription) + " already registered!");
        }
        HANDLERS.put(subscription, holder);
    }

    public static void recompute(DebugRenderer renderer) {
        HANDLERS.forEach((id, holder) -> {
            boolean enabled = ENABLED_SUBSCRIPTION.contains(id);
            if (enabled && !holder.alreadyEnabled().getAsBoolean()) {
                ((DebugRendererModifier) renderer).debugutils$update(holder.factory().apply(Minecraft.getInstance()), true);
            }
        });
    }

    public static Set<DebugSubscription<?>> getEnabledSubscription() {
        return ENABLED_SUBSCRIPTION;
    }

    public static void toggle(DebugSubscription<?> subscription, boolean enabled) {
        DebugRenderHolder holder = HANDLERS.get(subscription);
        if (holder == null)
            return;
        if (enabled) {
            ENABLED_SUBSCRIPTION.add(subscription);
        } else {
            ENABLED_SUBSCRIPTION.remove(subscription);
        }
        if (!holder.alreadyEnabled().getAsBoolean()) {
            ((DebugRendererModifier) Minecraft.getInstance().levelRenderer.debugRenderer)
                    .debugutils$update(holder.factory().apply(Minecraft.getInstance()), enabled);
        }
    }
}
