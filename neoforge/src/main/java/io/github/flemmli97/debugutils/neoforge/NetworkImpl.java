package io.github.flemmli97.debugutils.neoforge;

import io.github.flemmli97.debugutils.Network;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class NetworkImpl implements Network {

    @Override
    public void sendToClient(CustomPacketPayload message, ServerPlayer player) {
        player.connection.send(message);
    }
}
