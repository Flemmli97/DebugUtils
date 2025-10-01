package io.github.flemmli97.debugutils.client;

import com.google.common.collect.ImmutableSet;
import io.github.flemmli97.debugutils.api.DebugRenderHolder;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.BeeDebugRenderer;
import net.minecraft.client.renderer.debug.BrainDebugRenderer;
import net.minecraft.client.renderer.debug.BreezeDebugRenderer;
import net.minecraft.client.renderer.debug.ChunkDebugRenderer;
import net.minecraft.client.renderer.debug.CollisionBoxRenderer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.debug.EntityBlockIntersectionDebugRenderer;
import net.minecraft.client.renderer.debug.GameEventListenerRenderer;
import net.minecraft.client.renderer.debug.GoalSelectorDebugRenderer;
import net.minecraft.client.renderer.debug.HeightMapRenderer;
import net.minecraft.client.renderer.debug.LightDebugRenderer;
import net.minecraft.client.renderer.debug.LightSectionDebugRenderer;
import net.minecraft.client.renderer.debug.NeighborsUpdateRenderer;
import net.minecraft.client.renderer.debug.PathfindingRenderer;
import net.minecraft.client.renderer.debug.PoiDebugRenderer;
import net.minecraft.client.renderer.debug.RaidDebugRenderer;
import net.minecraft.client.renderer.debug.RedstoneWireOrientationsRenderer;
import net.minecraft.client.renderer.debug.SolidFaceRenderer;
import net.minecraft.client.renderer.debug.StructureRenderer;
import net.minecraft.client.renderer.debug.SupportBlockRenderer;
import net.minecraft.client.renderer.debug.VillageSectionsDebugRenderer;
import net.minecraft.client.renderer.debug.WaterDebugRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.debug.DebugSubscription;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.world.level.LightLayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Client side toggles
 */
public class DebugRenderHandler {

    private static final Map<ResourceLocation, DebugRenderHolder> HANDLERS = new HashMap<>();
    private static final Set<ResourceLocation> CLIENT_HANDLERS = new HashSet<>();

    private static final Set<DebugSubscription<?>> ENABLED_SUBSCRIPTION = new HashSet<>();
    private static final Set<ResourceLocation> ENABLED = new HashSet<>();

    static {
        registerHandler(DebugSubscriptions.ENTITY_PATHS, new DebugRenderHolder(() -> SharedConstants.DEBUG_PATHFINDING, PathfindingRenderer::new));
        registerClientOnlyHandler(ResourceLocation.parse("debug/water"), new DebugRenderHolder(() -> SharedConstants.DEBUG_WATER, WaterDebugRenderer::new));
        registerClientOnlyHandler(ResourceLocation.parse("debug/heightmap"), new DebugRenderHolder(() -> SharedConstants.DEBUG_HEIGHTMAP, HeightMapRenderer::new));
        registerClientOnlyHandler(ResourceLocation.parse("debug/collision"), new DebugRenderHolder(() -> SharedConstants.DEBUG_COLLISION, CollisionBoxRenderer::new));
        registerClientOnlyHandler(ResourceLocation.parse("debug/support_blocks"), new DebugRenderHolder(() -> SharedConstants.DEBUG_SUPPORT_BLOCKS, SupportBlockRenderer::new));
        registerHandler(DebugSubscriptions.NEIGHBOR_UPDATES, new DebugRenderHolder(() -> SharedConstants.DEBUG_NEIGHBORSUPDATE, NeighborsUpdateRenderer::new));
        registerHandler(DebugSubscriptions.REDSTONE_WIRE_ORIENTATIONS, new DebugRenderHolder(() -> SharedConstants.DEBUG_EXPERIMENTAL_REDSTONEWIRE_UPDATE_ORDER, RedstoneWireOrientationsRenderer::new));
        registerHandler(DebugSubscriptions.STRUCTURES, new DebugRenderHolder(() -> SharedConstants.DEBUG_STRUCTURES, StructureRenderer::new));
        registerClientOnlyHandler(ResourceLocation.parse("debug/light"), new DebugRenderHolder(() -> SharedConstants.DEBUG_LIGHT, LightDebugRenderer::new));
        registerClientOnlyHandler(ResourceLocation.parse("debug/solid_faces"), new DebugRenderHolder(() -> SharedConstants.DEBUG_SOLID_FACE, SolidFaceRenderer::new));
        registerHandler(DebugSubscriptions.VILLAGE_SECTIONS, new DebugRenderHolder(() -> SharedConstants.DEBUG_VILLAGE_SECTIONS, VillageSectionsDebugRenderer::new));
        registerHandler(DebugSubscriptions.BRAINS, new DebugRenderHolder(() -> SharedConstants.DEBUG_BRAIN, BrainDebugRenderer::new));
        registerHandler(DebugSubscriptions.POIS, new DebugRenderHolder(() -> SharedConstants.DEBUG_POI, minecraft -> new PoiDebugRenderer(new BrainDebugRenderer(minecraft))));
        registerHandler(DebugSubscriptions.BEES, new DebugRenderHolder(() -> SharedConstants.DEBUG_BEES, BeeDebugRenderer::new));
        registerHandler(DebugSubscriptions.RAIDS, new DebugRenderHolder(() -> SharedConstants.DEBUG_RAIDS, RaidDebugRenderer::new));
        registerHandler(DebugSubscriptions.GOAL_SELECTORS, new DebugRenderHolder(() -> SharedConstants.DEBUG_GOAL_SELECTOR, GoalSelectorDebugRenderer::new));
        registerClientOnlyHandler(ResourceLocation.parse("debug/chunk"), new DebugRenderHolder(() -> SharedConstants.DEBUG_CHUNKS, ChunkDebugRenderer::new));
        registerHandler(DebugSubscriptions.GAME_EVENT_LISTENERS, new DebugRenderHolder(() -> SharedConstants.DEBUG_GAME_EVENT_LISTENERS, GameEventListenerRenderer::new));
        registerHandler(DebugSubscriptions.GAME_EVENTS, new DebugRenderHolder(() -> SharedConstants.DEBUG_GAME_EVENT_LISTENERS, mc -> HANDLERS.get(BuiltInRegistries.DEBUG_SUBSCRIPTION.getKey(DebugSubscriptions.GAME_EVENT_LISTENERS)).factory().apply(mc)));
        registerClientOnlyHandler(ResourceLocation.parse("debug/sky_light_sections"), new DebugRenderHolder(() -> SharedConstants.DEBUG_SKY_LIGHT_SECTIONS, minecraft -> new LightSectionDebugRenderer(minecraft, LightLayer.SKY)));
        registerHandler(DebugSubscriptions.BREEZES, new DebugRenderHolder(() -> SharedConstants.DEBUG_BREEZE_MOB, BreezeDebugRenderer::new));
        registerClientOnlyHandler(ResourceLocation.parse("debug/entity_block_intersection"), new DebugRenderHolder(() -> SharedConstants.DEBUG_ENTITY_BLOCK_INTERSECTION, EntityBlockIntersectionDebugRenderer::new));
    }

    public static synchronized void registerHandler(DebugSubscription<?> subscription, DebugRenderHolder holder) {
        ResourceLocation id = BuiltInRegistries.DEBUG_SUBSCRIPTION.getKey(subscription);
        if (id == null) {
            throw new IllegalStateException("No such subscription registered" + subscription + "!");
        }
        register(id, holder, false);
    }

    public static synchronized void registerClientOnlyHandler(ResourceLocation id, DebugRenderHolder holder) {
        register(id, holder, true);
    }

    private static void register(ResourceLocation id, DebugRenderHolder holder, boolean clientOnly) {
        if (HANDLERS.containsKey(id)) {
            throw new IllegalStateException("Handler with given id " + id + " already registered!");
        }
        HANDLERS.put(id, holder);
        if (clientOnly) {
            CLIENT_HANDLERS.add(id);
        }
    }

    public static void recompute(DebugRenderer renderer) {
        HANDLERS.forEach((id, holder) -> {
            boolean enabled = ENABLED.contains(id);
            if (enabled && !holder.alreadyEnabled().getAsBoolean()) {
                ((DebugRendererModifier) renderer).debugutils$update(holder.factory().apply(Minecraft.getInstance()), true, holder.translucent());
            }
        });
    }

    public static Set<ResourceLocation> getClientHandlers() {
        return ImmutableSet.copyOf(CLIENT_HANDLERS);
    }

    public static Set<DebugSubscription<?>> getEnabledSubscription() {
        return ENABLED_SUBSCRIPTION;
    }

    public static void toggle(DebugSubscription<?> subscription, boolean enabled) {
        ResourceLocation id = BuiltInRegistries.DEBUG_SUBSCRIPTION.getKey(subscription);
        if (id != null) {
            toggle(id, enabled);
            if (enabled) {
                ENABLED_SUBSCRIPTION.add(subscription);
            } else {
                ENABLED_SUBSCRIPTION.remove(subscription);
            }
        }
    }

    public static void toggle(ResourceLocation id, boolean enabled) {
        DebugRenderHolder holder = HANDLERS.get(id);
        if (holder == null)
            return;
        if (enabled) {
            ENABLED.add(id);
        } else {
            ENABLED.remove(id);
        }
        if (!holder.alreadyEnabled().getAsBoolean()) {
            ((DebugRendererModifier) Minecraft.getInstance().levelRenderer.debugRenderer)
                    .debugutils$update(holder.factory().apply(Minecraft.getInstance()), enabled, holder.translucent());
        }
    }

    public static void toggleOff() {
        Set<ResourceLocation> enabled = new HashSet<>(ENABLED);
        enabled.forEach(id -> toggle(id, false));
        ENABLED_SUBSCRIPTION.clear();
    }

}
