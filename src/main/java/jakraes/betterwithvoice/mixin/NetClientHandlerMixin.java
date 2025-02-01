package jakraes.betterwithvoice.mixin;

import jakraes.betterwithvoice.client.Client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.net.handler.NetClientHandler;
import net.minecraft.core.net.packet.Packet1Login;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NetClientHandler.class, remap = false)
public class NetClientHandlerMixin {
	@Unique
	private String host;
	@Unique
	private int port;

	@Inject(method = "<init>", at = @At("TAIL"))
	public void NetClientHandler(Minecraft minecraft, String host, int port, CallbackInfo ci) {
		this.host = host;
		this.port = port;
	}

	@Inject(method = "handleLogin", at = @At("TAIL"))
	public void handleLogin(Packet1Login packet1login, CallbackInfo ci) {
		Client.getInstance().connect(host, port + 1);
		Client.getInstance().startAudio();
	}
}
