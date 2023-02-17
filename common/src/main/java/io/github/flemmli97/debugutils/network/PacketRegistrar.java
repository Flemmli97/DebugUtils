package io.github.flemmli97.debugutils.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class PacketRegistrar {

    public static int registerClientPackets(ClientPacketRegister register, int id) {
        register.registerMessage(id++, S2CDebugToggle.ID, S2CDebugToggle.class, S2CDebugToggle::write, S2CDebugToggle::read, S2CDebugToggle::handle);
        register.registerMessage(id++, S2CSpawnChunk.ID, S2CSpawnChunk.class, S2CSpawnChunk::write, S2CSpawnChunk::read, S2CSpawnChunk::handle);
        return id;
    }

    public interface ClientPacketRegister {
        <P> void registerMessage(int index, ResourceLocation id, Class<P> clss, BiConsumer<P, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, P> decoder, Consumer<P> handler);
    }
}
