package io.github.flemmli97.debugutils.forge;

import io.github.flemmli97.debugutils.DebugUtils;
import io.github.flemmli97.debugutils.network.PacketRegistrar;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PacketHandler {

    private static final SimpleChannel DISPATCHER =
            NetworkRegistry.ChannelBuilder.named(new ResourceLocation(DebugUtils.MODID, "packets"))
                    .clientAcceptedVersions(a -> true)
                    .serverAcceptedVersions(a -> true)
                    .networkProtocolVersion(() -> "v1.0").simpleChannel();

    public static void register() {
        PacketRegistrar.registerClientPackets(new PacketRegistrar.ClientPacketRegister() {
            @Override
            public <P> void registerMessage(int index, ResourceLocation id, Class<P> clss, BiConsumer<P, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, P> decoder, Consumer<P> handler) {
                DISPATCHER.registerMessage(index, clss, encoder, decoder, handlerClient(handler), Optional.of(NetworkDirection.PLAY_TO_CLIENT));
            }
        }, 0);
    }

    public static <T> void sendToClient(T message, ServerPlayer player) {
        DISPATCHER.sendTo(message, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    private static <T> BiConsumer<T, Supplier<NetworkEvent.Context>> handlerClient(Consumer<T> handler) {
        return (p, ctx) -> {
            ctx.get().enqueueWork(() -> handler.accept(p));
            ctx.get().setPacketHandled(true);
        };
    }
}
