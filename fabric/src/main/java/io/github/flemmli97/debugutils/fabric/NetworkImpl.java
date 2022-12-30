package io.github.flemmli97.debugutils.fabric;

import io.github.flemmli97.debugutils.Network;
import io.github.flemmli97.debugutils.network.Packet;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class NetworkImpl implements Network {
    @Override
    public void sendToClient(Packet message, ServerPlayer player) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        message.write(buf);
        ServerPlayNetworking.send(player, message.getID(), buf);
    }
}
