package io.github.flemmli97.debugutils.mixin;

import io.github.flemmli97.debugutils.client.DebugRenderHandler;
import io.github.flemmli97.debugutils.client.DebugRendererModifier;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin implements DebugRendererModifier {

    @Shadow
    @Final
    private List<DebugRenderer.SimpleDebugRenderer> opaqueRenderers;
    @Shadow
    @Final
    private List<DebugRenderer.SimpleDebugRenderer> translucentRenderers;

    @Inject(method = "refreshRendererList", at = @At("RETURN"))
    private void doDebugRenderers(CallbackInfo ci) {
        DebugRenderHandler.recompute((DebugRenderer) (Object) this);
    }

    @Override
    public void debugutils$update(DebugRenderer.SimpleDebugRenderer renderer, boolean add, boolean transparent) {
        if (transparent) {
            if (add) {
                if (!this.translucentRenderers.contains(renderer)) {
                    this.translucentRenderers.add(renderer);
                }
            } else {
                this.translucentRenderers.remove(renderer);
            }
        } else {
            if (add) {
                if (!this.opaqueRenderers.contains(renderer)) {
                    this.opaqueRenderers.add(renderer);
                }
            } else {
                this.opaqueRenderers.remove(renderer);
            }
        }
    }
}
