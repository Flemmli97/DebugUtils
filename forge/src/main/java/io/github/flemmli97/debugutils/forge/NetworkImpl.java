package io.github.flemmli97.debugutils.forge;

import io.github.flemmli97.debugutils.Network;
import io.github.flemmli97.debugutils.network.Packet;
import net.minecraft.server.level.ServerPlayer;

public class NetworkImpl implements Network {

    @Override
    public void sendToClient(Packet message, ServerPlayer player) {
        PacketHandler.sendToClient(message, player);
    }
}
