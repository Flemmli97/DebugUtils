package io.github.flemmli97.debugutils;

import io.github.flemmli97.debugutils.network.Packet;
import net.minecraft.server.level.ServerPlayer;

public interface Network {

    Network INSTANCE = DebugUtils.getPlatformInstance(Network.class,
            "io.github.flemmli97.debugutils.fabric.NetworkImpl",
            "io.github.flemmli97.debugutils.forge.NetworkImpl");

    void sendToClient(Packet message, ServerPlayer player);
}
