package io.github.flemmli97.debugutils.network;

import io.github.flemmli97.debugutils.DebugUtils;
import io.github.flemmli97.debugutils.client.RenderBools;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public record S2CDebugToggle(ResourceLocation type, boolean on) implements Packet {

    public static final ResourceLocation ID = new ResourceLocation(DebugUtils.MODID, "s2c_debug_toggle");

    public static S2CDebugToggle read(FriendlyByteBuf buf) {
        return new S2CDebugToggle(buf.readResourceLocation(), buf.readBoolean());
    }

    public static void handle(S2CDebugToggle pkt) {
        Consumer<Boolean> c = RenderBools.HANDLERS.get(pkt.type);
        if (c != null)
            c.accept(pkt.on);
        else
            DebugUtils.LOGGER.error("Unkown debug toggle " + pkt.type);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.type);
        buf.writeBoolean(this.on);
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }
}
