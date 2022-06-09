package io.github.pepe20129.splashes;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class QuiltSplashesPlus implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		SplashesPlus.onInitializeClient();
	}
}