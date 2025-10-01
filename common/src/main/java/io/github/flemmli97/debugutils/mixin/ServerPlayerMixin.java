package io.github.flemmli97.debugutils.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.flemmli97.debugutils.Network;
import io.github.flemmli97.debugutils.network.S2CDebugToggle;
import io.github.flemmli97.debugutils.utils.PlayerDebugToggle;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.debug.DebugSubscription;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements PlayerDebugToggle {

    @Unique
    private final Set<DebugSubscription<?>> debugutils$activeDebugSubscriptions = new HashSet<>();

    @ModifyReturnValue(method = "debugSubscriptions", at = @At("RETURN"))
    private Set<DebugSubscription<?>> debug(Set<DebugSubscription<?>> original) {
        if (this.debugutils$activeDebugSubscriptions.isEmpty())
            return original;
        Set<DebugSubscription<?>> set = new HashSet<>(original);
        set.addAll(this.debugutils$activeDebugSubscriptions);
        return set;
    }

    @Override
    public void debugutils$toggle(List<DebugSubscription<?>> subscription, boolean enabled) {
        if (enabled) {
            this.debugutils$activeDebugSubscriptions.addAll(subscription);
            Network.INSTANCE.sendToClient(new S2CDebugToggle(subscription, true), (ServerPlayer) (Object) this);
        } else {
            subscription.forEach(this.debugutils$activeDebugSubscriptions::remove);
            Network.INSTANCE.sendToClient(new S2CDebugToggle(subscription, false), (ServerPlayer) (Object) this);
        }
    }
}
