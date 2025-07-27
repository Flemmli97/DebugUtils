package io.github.flemmli97.debugutils.neoforge.data;

import io.github.flemmli97.debugutils.DebugUtils;
import net.minecraft.data.DataGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = DebugUtils.MODID)
public class DataEvent {

    @SubscribeEvent
    public static void data(GatherDataEvent.Client event) {
        DataGenerator data = event.getGenerator();
        data.addProvider(true, new Lang(data.getPackOutput()));
    }

}
