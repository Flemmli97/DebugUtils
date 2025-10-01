package io.github.flemmli97.debugutils.utils;

import net.minecraft.util.debug.DebugSubscription;

import java.util.List;

public interface PlayerDebugToggle {

    void debugutils$toggle(List<DebugSubscription<?>> subscription, boolean enabled);
}
