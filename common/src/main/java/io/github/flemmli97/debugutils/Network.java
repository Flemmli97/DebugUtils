package io.github.flemmli97.debugutils;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public interface Network {

    Network INSTANCE = DebugUtils.getPlatformInstance(Network.class,
            "io.github.flemmli97.debugutils.fabric.NetworkImpl",
            "io.github.flemmli97.debugutils.neoforge.NetworkImpl");

    void sendToClient(CustomPacketPayload pkt, ServerPlayer player);
}
