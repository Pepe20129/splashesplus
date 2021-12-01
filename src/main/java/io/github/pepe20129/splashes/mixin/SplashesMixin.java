package io.github.pepe20129.splashes.mixin;

import net.minecraft.MinecraftVersion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import net.minecraft.client.util.Session;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@Mixin(SplashTextResourceSupplier.class)
public class SplashesMixin {
	@Shadow @Final private Session session;
	/**
	 * @author Pepe20129/Pablo#1981
	 *
	 * @reason Complete overhaul to the splash code
	 */
	@Overwrite
	@Nullable
	public String get() {
		try {
			StringWriter writer = new StringWriter();
			Resource resource = MinecraftClient.getInstance().getResourceManager().getResource(new Identifier("texts/splashes.json"));
			IOUtils.copy(resource.getInputStream(), writer, StandardCharsets.UTF_8);
			String rawData = writer.toString();
			JSONObject jsonData = new JSONObject(rawData);
			JSONArray normalSplashes;

			try {
				normalSplashes = jsonData.getJSONArray("normal_splashes");
			} catch (JSONException jsonException) {
				normalSplashes = new JSONArray();
			}

			JSONArray conditionalSplashes;
			try {
				conditionalSplashes = jsonData.getJSONArray("conditional_splashes");
			} catch (JSONException jsonException) {
				conditionalSplashes = new JSONArray();
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

			Random RANDOM = new Random();
			JSONObject currentSpecialSplash;
			for (int i = 0; i< conditionalSplashes.length(); i+=1) {
				currentSpecialSplash = conditionalSplashes.getJSONObject(i);
				String time;
				try {
					time = currentSpecialSplash.getString("time");
				} catch (JSONException jsonException) {
					time = ".*";
				}
				Pattern timePattern = Pattern.compile(time);

				String mcVersion;
				try {
					mcVersion = currentSpecialSplash.getString("minecraft_version");
				} catch (JSONException jsonException) {
					mcVersion = ".*";
				}
				Pattern mcVersionPattern = Pattern.compile(mcVersion);

				String username;
				try {
					username = currentSpecialSplash.getString("username");
				} catch (JSONException jsonException) {
					username = ".*";
				}
				Pattern usernamePattern = Pattern.compile(username);

				float chance;
				try {
					chance = currentSpecialSplash.getFloat("chance");
				} catch (JSONException jsonException) {
					chance = 1;
				}

				if (timePattern.matcher(now).find() && mcVersionPattern.matcher(MinecraftVersion.CURRENT.getName()).find() && usernamePattern.matcher(this.session.getUsername()).find() && RANDOM.nextFloat() < chance)
					return parse(currentSpecialSplash.getString("splash"));
			}

			List<String> list = new ArrayList<>();
			for (int i=0;i<normalSplashes.length();i++) {
				list.add(normalSplashes.getString(i));
			}

			if (list.isEmpty())
				return null;

			boolean isYou;
			try {
				isYou = jsonData.getBoolean("is_you");
			} catch (JSONException jsonException) {
				isYou = false;
			}

			if (isYou && this.session != null && RANDOM.nextInt(list.size()) == 42)
				return this.session.getUsername().toUpperCase(Locale.ROOT) + " IS YOU";

			String selected = list.get(RANDOM.nextInt(list.size()));
			while (selected.equals("This message will never appear on the splash screen, isn't that weird?"))
				selected = list.get(RANDOM.nextInt(list.size()));
			return parse(selected);
		} catch (IOException ioException) {
			return null;
		}
	}

	public String parse(String input) {
		if (input.isEmpty())
			return null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		input = input.replace("%playerName", this.session.getUsername());
		input = input.replace("%playername", this.session.getUsername().toLowerCase(Locale.ROOT));
		input = input.replace("%PLAYERNAME", this.session.getUsername().toUpperCase(Locale.ROOT));
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

	public String intTo2DigitString(int input) {
		String output = Integer.toString(input);
		if (input < 10)
			return "0" + output;
		return output;
	}
}