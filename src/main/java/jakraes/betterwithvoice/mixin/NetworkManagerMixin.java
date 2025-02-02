package jakraes.betterwithvoice.mixin;

import jakraes.betterwithvoice.client.Client;
import net.minecraft.core.net.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import turniplabs.halplibe.HalpLibe;

@Mixin(value = NetworkManager.class, remap = false)
public class NetworkManagerMixin {
	@Inject(method = "networkShutdown", at = @At("TAIL"))
	public void networkShutdown(String s, Object[] aobj, CallbackInfo ci) {
		if (HalpLibe.isClient) {
			Client.getInstance().disconnect();
		}
	}
}
