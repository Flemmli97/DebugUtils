package io.github.flemmli97.debugutils.mixin;

import com.google.common.collect.Sets;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.flemmli97.debugutils.client.DebugRenderHandler;
import net.minecraft.client.multiplayer.ClientDebugSubscriber;
import net.minecraft.util.debug.DebugSubscription;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Set;

@Mixin(ClientDebugSubscriber.class)
public class ClientDebugSubscriberMixin {

    @ModifyReturnValue(method = "requestedSubscriptions", at = @At(value = "RETURN"))
    private Set<DebugSubscription<?>> injectToggleable(Set<DebugSubscription<?>> original) {
        original.addAll(DebugRenderHandler.getEnabledSubscription());
        return original;
    }

    @ModifyArg(method = "onSubscriptionsChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ServerboundDebugSubscriptionRequestPacket;<init>(Ljava/util/Set;)V"), index = 0)
    private Set<DebugSubscription<?>> updatePacketData(Set<DebugSubscription<?>> set) {
        return Sets.difference(set, DebugRenderHandler.getEnabledSubscription());
    }
}
