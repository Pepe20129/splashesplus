package io.github.pepe20129.splashes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SplashesPlus {
	public static final String MODID = "splashesplus";
	
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	
	public static JsonArray normalSplashes;
	
	public static JsonArray conditionalSplashes;

	public static void onInitializeClient() {
		ResourceManagerHelper clientResources = ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES);
		
		clientResources.registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public void reload(ResourceManager manager) {
				normalSplashes = new JsonArray();
				conditionalSplashes = new JsonArray();

				manager.findResources("texts/splashes", path -> path.getPath().endsWith(".json")).forEach(
					(key, value) -> {
						try (InputStream inputStream = value.getInputStream()) {
							String rawData = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
							inputStream.close();
							JsonObject jsonData = JsonParser.parseString(rawData).getAsJsonObject();

							try {
								SplashesPlus.normalSplashes.addAll(jsonData.get("normal_splashes").getAsJsonArray());
							} catch (NullPointerException ignored) {}

							try {
								SplashesPlus.conditionalSplashes.addAll(jsonData.get("conditional_splashes").getAsJsonArray());
							} catch (NullPointerException ignored) {}
						} catch (Exception exception) {
							LOGGER.error("Error occurred while loading splashes file: \"" + key.toString() + "\"", exception);
						}
					}
				);
			}
			
			@Override
			public Identifier getFabricId() {
				return new Identifier(MODID, "splashes");
			}
		});
	}
}