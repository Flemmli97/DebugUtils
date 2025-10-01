package io.github.flemmli97.debugutils.neoforge;

import io.github.flemmli97.debugutils.DebugCommands;
import io.github.flemmli97.debugutils.DebugUtils;
import io.github.flemmli97.debugutils.network.S2CDebugToggle;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(DebugUtils.MODID)
public class DebugUtilsNeoForge {

    public DebugUtilsNeoForge(IEventBus modBus) {
        modBus.addListener(this::registerPackets);
        NeoForge.EVENT_BUS.addListener(this::command);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            NeoForge.EVENT_BUS.addListener(DebugUtilsClient::registerClientCommand);
        }
    }

    public void command(RegisterCommandsEvent event) {
        DebugCommands.register(event.getDispatcher(), event.getBuildContext());
    }

    public void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(DebugUtils.MODID);
        registrar.playToClient(S2CDebugToggle.TYPE, S2CDebugToggle.STREAM_CODEC, (pkt, ctx) -> S2CDebugToggle.handle(pkt));
    }
}
