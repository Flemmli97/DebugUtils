package io.github.flemmli97.debugutils.mixin.pathing;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.BinaryHeap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.Target;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;
import java.util.Set;

/**
 * Sets data necessary for the path debug packets
 */
@Mixin(PathFinder.class)
public class PathFinderMixin {

    @Shadow
    private BinaryHeap openSet;

    @Inject(method = "findPath", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void setPathDebugs(PathNavigationRegion region, Mob mob, Set<BlockPos> targetPositions, float maxRange, int accuracy, float searchDepthMultiplier, CallbackInfoReturnable<Path> info,
                               Node start, Map<Target, BlockPos> map) {
        ((PathAccessor) info.getReturnValue()).debugData(this.openSet.getHeap(), new Node[0], map.keySet());
    }
}
