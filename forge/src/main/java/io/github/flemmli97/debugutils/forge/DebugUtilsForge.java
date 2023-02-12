package io.github.flemmli97.debugutils.forge;

import io.github.flemmli97.debugutils.Commands;
import io.github.flemmli97.debugutils.DebugToggles;
import io.github.flemmli97.debugutils.DebugUtils;
import io.github.flemmli97.debugutils.client.AdditionalDebugRenderers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(DebugUtils.MODID)
public class DebugUtilsForge {

    public DebugUtilsForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::common);
        MinecraftForge.EVENT_BUS.addListener(this::command);
        MinecraftForge.EVENT_BUS.addListener(this::joinServer);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.addListener(DebugUtilsClient::disconnect);
            AdditionalDebugRenderers.init();
        }
    }

    public void common(FMLCommonSetupEvent event) {
        PacketHandler.register();
    }

    public void command(RegisterCommandsEvent event) {
        Commands.register(event.getDispatcher());
    }

    public void joinServer(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer serverPlayer)
            DebugToggles.onLogin(serverPlayer);
    }
}
