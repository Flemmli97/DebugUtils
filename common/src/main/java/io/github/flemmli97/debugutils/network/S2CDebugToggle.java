package io.github.flemmli97.debugutils.network;

import io.github.flemmli97.debugutils.DebugUtils;
import io.github.flemmli97.debugutils.client.DebugRenderHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.util.debug.DebugSubscription;

import java.util.List;

public record S2CDebugToggle(List<DebugSubscription<?>> toggles, boolean on) implements CustomPacketPayload {

    public static final Type<S2CDebugToggle> TYPE = new Type<>(Identifier.fromNamespaceAndPath(DebugUtils.MODID, "s2c_debug_toggle"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CDebugToggle> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public S2CDebugToggle decode(RegistryFriendlyByteBuf buf) {
            return new S2CDebugToggle(ByteBufCodecs.<RegistryFriendlyByteBuf, DebugSubscription<?>>list()
                    .apply(ByteBufCodecs.registry(Registries.DEBUG_SUBSCRIPTION))
                    .decode(buf), buf.readBoolean());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, S2CDebugToggle pkt) {
            ByteBufCodecs.<RegistryFriendlyByteBuf, DebugSubscription<?>>list()
                    .apply(ByteBufCodecs.registry(Registries.DEBUG_SUBSCRIPTION))
                    .encode(buf, pkt.toggles);
            buf.writeBoolean(pkt.on);
        }
    };

    public static void handle(S2CDebugToggle pkt) {
        pkt.toggles.forEach(t -> DebugRenderHandler.toggle(t, pkt.on));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
