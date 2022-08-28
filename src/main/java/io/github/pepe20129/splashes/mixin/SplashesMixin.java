package io.github.pepe20129.splashes.mixin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import io.github.pepe20129.splashes.SplashesPlus;
import net.minecraft.MinecraftVersion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import net.minecraft.client.util.Session;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Mixin(SplashTextResourceSupplier.class)
public class SplashesMixin {
	@Shadow @Mutable @Final private List<String> splashTexts;
	@Shadow @Final private Session session;
	@Shadow @Final private static Random RANDOM;
	/**
	 * @author Pepe20129/Pablo#1981
	 *
	 * @reason Complete overhaul to the splash code
	 */
	@Inject(method = "get", at = @At("HEAD"), cancellable = true)
	public void get(CallbackInfoReturnable<String> cir) {
		if (SplashesPlus.normalSplashes == null) {
			SplashesPlus.LOGGER.info("Normal splashes not found");
			SplashesPlus.normalSplashes = new JsonArray();
		}

		if (SplashesPlus.conditionalSplashes == null) {
			SplashesPlus.LOGGER.info("Conditional splashes not found");
			SplashesPlus.conditionalSplashes = new JsonArray();
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		String now = calendar.get(Calendar.YEAR) + "-";
		if (calendar.get(Calendar.MONTH) + 1 < 10)
			now += "0";
		now += calendar.get(Calendar.MONTH) + 1 + "-";
		if (calendar.get(Calendar.DAY_OF_MONTH) < 10)
			now += "0";
		now += calendar.get(Calendar.DAY_OF_MONTH) + " ";
		if (calendar.get(Calendar.HOUR_OF_DAY) < 10)
			now += "0";
		now += calendar.get(Calendar.HOUR_OF_DAY) + ":";
		if (calendar.get(Calendar.MINUTE) < 10)
			now += "0";
		now += calendar.get(Calendar.MINUTE) + ":";
		if (calendar.get(Calendar.SECOND) < 10)
			now += "0";
		now += calendar.get(Calendar.SECOND);

		JsonObject currentConditionalSplash;
		for (JsonElement element : SplashesPlus.conditionalSplashes) {
			currentConditionalSplash = element.getAsJsonObject();
			String time;
			try {
				time = currentConditionalSplash.get("time").getAsString();
			} catch (Exception exception) {
				time = ".*";
			}
			Pattern timePattern = Pattern.compile(time);

			String minecraftVersion;
			try {
				minecraftVersion = currentConditionalSplash.get("minecraft_version").getAsString();
			} catch (Exception exception) {
				minecraftVersion = ".*";
			}
			Pattern minecraftVersionPattern = Pattern.compile(minecraftVersion);

			String username;
			try {
				username = currentConditionalSplash.get("username").getAsString();
			} catch (Exception exception) {
				username = ".*";
			}
			Pattern usernamePattern = Pattern.compile(username);

			float chance;
			try {
				chance = currentConditionalSplash.get("chance").getAsFloat();
			} catch (Exception exception) {
				chance = 1;
			}

			if (timePattern.matcher(now).find() &&
				minecraftVersionPattern.matcher(MinecraftVersion.CURRENT.getName()).find() &&
				usernamePattern.matcher(session.getUsername()).find() &&
				RANDOM.nextFloat() < chance) {
				cir.setReturnValue(parse(currentConditionalSplash.get("splash").getAsString()));
				return;
			}
		}

		List<String> normalSplashes = new Gson().fromJson(SplashesPlus.normalSplashes, new TypeToken<List<String>>() {}.getType());

		//to prevent Frame API from overriding the result when it shouldn't
		splashTexts = normalSplashes;

		if (normalSplashes.isEmpty()) {
			cir.setReturnValue(null);
			return;
		}

		boolean isYou;
		try {
			StringWriter writer = new StringWriter();
			Resource resource = MinecraftClient.getInstance().getResourceManager().getResource(new Identifier(SplashesPlus.MODID, "texts/splashes/minecraft.json")).get();
			IOUtils.copy(resource.getInputStream(), writer, StandardCharsets.UTF_8);
			String rawData = writer.toString();
			JsonObject jsonData = JsonParser.parseString(rawData).getAsJsonObject();
			isYou = jsonData.get("is_you").getAsBoolean();
		} catch (Exception exception) {
			isYou = false;
		}

		if (isYou && session != null && RANDOM.nextInt(normalSplashes.size()) == 42) {
			cir.setReturnValue(session.getUsername().toUpperCase(Locale.ROOT) + " IS YOU");
			return;
		}

		String selected = normalSplashes.get(RANDOM.nextInt(normalSplashes.size()));
		while (selected.equals("This message will never appear on the splash screen, isn't that weird?"))
			selected = normalSplashes.get(RANDOM.nextInt(normalSplashes.size()));
		cir.setReturnValue(parse(selected));
	}

	@Unique
	public String parse(String input) {
		if (input.isEmpty())
			return null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		input = input.replace("%playerName", session.getUsername());
		input = input.replace("%playername", session.getUsername().toLowerCase(Locale.ROOT));
		input = input.replace("%PLAYERNAME", session.getUsername().toUpperCase(Locale.ROOT));
		input = input.replace("%minecraftVersion", MinecraftVersion.CURRENT.getName());
		input = input.replace("%s", String.valueOf(Math.floor(calendar.getTimeInMillis()/1000)));
		input = input.replace("%F", "%Y-%m-%d");
		input = input.replace("%T", "%H:%M:%S");
		input = input.replace("%R", "%H:%M");
		input = input.replace("%Y", Integer.toString(calendar.get(Calendar.YEAR)));
		input = input.replace("%y", Integer.toString(calendar.get(Calendar.YEAR)).substring(2, 4));
		input = input.replace("%C", Integer.toString(calendar.get(Calendar.YEAR)).substring(0, 2));
		input = input.replace("%m", intTo2DigitString(calendar.get(Calendar.MONTH) + 1));
		input = input.replace("%d", intTo2DigitString(calendar.get(Calendar.DAY_OF_MONTH)));
		input = input.replace("%H", intTo2DigitString(calendar.get(Calendar.HOUR_OF_DAY)));
		input = input.replace("%M", intTo2DigitString(calendar.get(Calendar.MINUTE)));
		input = input.replace("%S", intTo2DigitString(calendar.get(Calendar.SECOND)));
		input = input.replace("%W", intTo2DigitString(calendar.get(Calendar.WEEK_OF_YEAR)));
		return input;
	}

	@Unique
	public String intTo2DigitString(int input) {
		String output = Integer.toString(input);
		if (input < 10)
			return "0" + output;
		return output;
	}
}