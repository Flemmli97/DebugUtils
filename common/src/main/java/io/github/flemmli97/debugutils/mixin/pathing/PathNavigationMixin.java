package io.github.flemmli97.debugutils.mixin.pathing;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.flemmli97.debugutils.utils.PathFindDebugData;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PathNavigation.class)
public abstract class PathNavigationMixin {

    @Shadow
    @Final
    private PathFinder pathFinder;

    @ModifyExpressionValue(method = "createPath(Ljava/util/Set;IZIF)Lnet/minecraft/world/level/pathfinder/Path;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/pathfinder/PathFinder;findPath(Lnet/minecraft/world/level/PathNavigationRegion;Lnet/minecraft/world/entity/Mob;Ljava/util/Set;FIF)Lnet/minecraft/world/level/pathfinder/Path;"))
    private Path injectDebugData(Path original) {
        Path.DebugData data = ((PathFindDebugData) this.pathFinder).getLastData();
        if (data != null)
            ((PathAccessor) original).debugData(data.openSet(), data.closedSet(), data.targetNodes());
        return original;
    }
}
