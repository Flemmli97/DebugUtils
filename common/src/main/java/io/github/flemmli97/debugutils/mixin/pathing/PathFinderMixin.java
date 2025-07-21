package io.github.flemmli97.debugutils.mixin.pathing;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.flemmli97.debugutils.DebugToggles;
import io.github.flemmli97.debugutils.utils.PathFindDebugData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.BinaryHeap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.Target;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Sets data necessary for the path debug packets
 */
@Mixin(PathFinder.class)
public abstract class PathFinderMixin implements PathFindDebugData {

    @Final
    @Shadow
    private BinaryHeap openSet;
    @Shadow
    @Final
    private Node[] neighbors;

    @Unique
    private final Set<Node> debugutils$closedSet = new HashSet<>();
    @Unique
    private Path.DebugData debugutils$lastData;

    @Inject(method = "findPath(Lnet/minecraft/world/level/PathNavigationRegion;Lnet/minecraft/world/entity/Mob;Ljava/util/Set;FIF)Lnet/minecraft/world/level/pathfinder/Path;",
            at = @At("HEAD"))
    private void clearDebugs(PathNavigationRegion region, Mob mob, Set<BlockPos> targetPositions, float maxRange, int accuracy, float searchDepthMultiplier, CallbackInfoReturnable<Path> info) {
        this.debugutils$lastData = null;
    }

    @ModifyExpressionValue(method = "findPath(Lnet/minecraft/world/level/pathfinder/Node;Ljava/util/Map;FIF)Lnet/minecraft/world/level/pathfinder/Path;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/pathfinder/NodeEvaluator;getNeighbors([Lnet/minecraft/world/level/pathfinder/Node;Lnet/minecraft/world/level/pathfinder/Node;)I"))
    private int cacheNodes(int amount) {
        if (DebugToggles.DEBUG_PATHS.get()) {
            // Dont think there is a better way to do this
            this.debugutils$closedSet.addAll(Arrays.asList(this.neighbors).subList(0, amount));
        }
        return amount;
    }

    @Inject(method = "findPath(Lnet/minecraft/world/level/pathfinder/Node;Ljava/util/Map;FIF)Lnet/minecraft/world/level/pathfinder/Path;",
            at = @At("TAIL"))
    private void setPathDebugs(Node node, Map<Target, BlockPos> targetPos, float maxRange, int accuracy, float searchDepthMultiplier, CallbackInfoReturnable<Path> info) {
        if (DebugToggles.DEBUG_PATHS.get()) {
            List<Node> closed = new ArrayList<>();
            for (Node check : this.debugutils$closedSet) {
                if (!check.inOpenSet())
                    closed.add(check);
            }
            this.debugutils$closedSet.clear();
            this.debugutils$lastData = new Path.DebugData(this.openSet.getHeap(), closed.toArray(Node[]::new), targetPos.keySet());
        }
    }

    @Override
    public Path.DebugData debugutils$getLastData() {
        return this.debugutils$lastData;
    }
}
