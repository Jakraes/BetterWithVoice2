package jakraes.betterwithvoice;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BetterWithVoice implements ModInitializer {
	public static final String MOD_ID = "BetterWithVoice";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("BetterWithVoice initialized.");
	}
}
