package net.thenoppy12.ngroklan;

import com.github.alexdlaird.ngrok.NgrokClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.thenoppy12.ngroklan.config.NLanConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;

@Environment(EnvType.CLIENT)
public class NgrokLan implements ModInitializer {
	public static final String MODID = "NgrokLAN";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	public static NgrokClient ngrokClient;

	@Override
	public void onInitialize() {
		LOGGER.info("- Reforked to 1.16.5 by thenoppy12 (.thenoppy12#0)");
		LOGGER.info("- Loaded");

		AutoConfig.register(NLanConfig.class, JanksonConfigSerializer::new);
	}


}
