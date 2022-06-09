package io.github.pepe20129.splashes;

import net.fabricmc.api.ClientModInitializer;

public class FabricSplashesPlus implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		SplashesPlus.onInitializeClient();
	}
}