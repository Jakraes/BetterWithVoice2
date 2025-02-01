package jakraes.betterwithvoice.mixin;

import jakraes.betterwithvoice.client.Client;
import net.minecraft.client.input.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerInput.class, remap = false)
public class PlayerInputMixin {
	private static final int V_KEY = 47;

	@Inject(method = "keyEvent", at = @At("HEAD"))
	public void keyEvent(int keyCode, boolean pressed, CallbackInfo ci) {
		if (keyCode != V_KEY) return;

		if (pressed && !Client.getInstance().isRecording()) {
			Client.getInstance().startRecording();
		}
		else if (!pressed && Client.getInstance().isRecording()) {
			Client.getInstance().stopRecording();
		}
	}
}
