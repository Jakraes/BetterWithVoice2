package jakraes.betterwithvoice.mixin;

import jakraes.betterwithvoice.BetterWithVoice;
import jakraes.betterwithvoice.misc.ServerThread;
import net.minecraft.core.net.PropertyManager;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;

@Mixin(value = MinecraftServer.class, remap = false)
public class MinecraftServerMixin {
	@Shadow
	public PropertyManager propertyManager;


	@Inject(method = "startServer", at = @At("TAIL"))
	public void startServer(CallbackInfoReturnable<Boolean> cir) {
		int port = propertyManager.getIntProperty("server-port", 25565);

		try {
			new Thread(new ServerThread(port + 1)).start();
		} catch (IOException e) {
			BetterWithVoice.LOGGER.error("Failed to start server thread on {}.", port + 1);
			throw new RuntimeException(e);
		}
	}
}
