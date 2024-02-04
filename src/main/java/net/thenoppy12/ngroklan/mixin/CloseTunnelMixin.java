package net.thenoppy12.ngroklan.mixin;


import net.thenoppy12.ngroklan.NgrokLan;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class CloseTunnelMixin {
    @Inject(at = @At("TAIL"), method = "shutdown")
    private void afterShutdownServer(CallbackInfo info) {
        NgrokLan.LOGGER.info("Closing Lan!");
        NgrokLan.ngrokClient.kill();
    }

}
