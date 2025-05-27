package io.github.flemmli97.debugutils.mixin;

import io.github.flemmli97.debugutils.Network;
import io.github.flemmli97.debugutils.network.S2CSpawnChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Shadow
    @NotNull
    public abstract MinecraftServer getServer();

    @Inject(method = "setDefaultSpawnPos", at = @At("RETURN"))
    private void onSpawnSet(BlockPos pos, float angle, CallbackInfo ci) {
        this.getServer().getPlayerList().getPlayers().forEach(p ->
                Network.INSTANCE.sendToClient(new S2CSpawnChunk((ServerLevel) (Object) this), p));
    }
}
