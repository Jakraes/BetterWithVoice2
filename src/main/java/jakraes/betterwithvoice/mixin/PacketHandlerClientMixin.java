package jakraes.betterwithvoice.mixin;

import jakraes.betterwithvoice.BetterWithVoice;
import jakraes.betterwithvoice.misc.ClientThread;
import net.minecraft.client.Minecraft;
import net.minecraft.client.net.handler.PacketHandlerClient;
import net.minecraft.core.net.packet.PacketLogin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(value = PacketHandlerClient.class, remap = false)
public class PacketHandlerClientMixin {
	private String host;
	private int port;

	@Inject(method = "<init>", at = @At("TAIL"))
	public void packetHandlerClientConstructor(Minecraft minecraft, String host, int port, CallbackInfo ci) {
		this.host = host;
		this.port = port;
	}

	@Inject(method = "handleLogin", at = @At("TAIL"))
	public void handleLogin(PacketLogin loginPacket, CallbackInfo ci) {
		try {
			new Thread(new ClientThread(host, port + 1)).start();
		} catch (IOException e) {
			BetterWithVoice.LOGGER.error("Failed to launch client thread");
			throw new RuntimeException(e);
		}
	}
}
