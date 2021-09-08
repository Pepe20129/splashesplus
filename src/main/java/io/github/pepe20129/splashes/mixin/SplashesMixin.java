package io.github.pepe20129.splashes.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import net.minecraft.client.util.Session;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
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
			JSONObject timeSplashes;
			try {
				normalSplashes = jsonData.getJSONArray("normal_splashes");
			} catch (org.json.JSONException jsonException) {
				normalSplashes = new JSONArray();
			}
			try {
				timeSplashes = jsonData.getJSONObject("time_splashes");
			} catch (org.json.JSONException jsonException) {
				timeSplashes = new JSONObject();
			}
			List<String> list = new ArrayList<>();
			for (int i=0; i<normalSplashes.length(); i++) {
				list.add(normalSplashes.getString(i));
			}

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			Iterator<String> keys = timeSplashes.keys();
			while (keys.hasNext()) {
				String currentDynamicKey = keys.next();
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
				Pattern currentDynamicKeyPattern = Pattern.compile(currentDynamicKey);
				if (currentDynamicKeyPattern.matcher(now).find())
					return parse(timeSplashes.getString(currentDynamicKey));
			}

			if (list.isEmpty())
				return null;
			Random RANDOM = new Random();
			boolean isYou;
			try {
				isYou = jsonData.getBoolean("is_you");
			} catch (org.json.JSONException jsonException) {
				isYou = false;
			}
			if (this.session != null && RANDOM.nextInt(list.size()) == 42 && isYou)
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
        input = input.replace("%playerName", this.session.getUsername());
        input = input.replace("%playername", this.session.getUsername().toLowerCase(Locale.ROOT));
        input = input.replace("%PLAYERNAME", this.session.getUsername().toUpperCase(Locale.ROOT));
        input = input.replace("%Y", Integer.toString(calendar.get(Calendar.YEAR)));
        input = input.replace("%y", Integer.toString(calendar.get(Calendar.YEAR)).substring(2, 4));
        int month = calendar.get(Calendar.MONTH) + 1;
        String monthS = Integer.toString(month);
        if (month < 10)
            monthS = "0" + month;
        input = input.replace("%m", monthS);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String dayS = Integer.toString(day);
        if (day < 10)
            dayS = "0" + day;
        input = input.replace("%d", dayS);
        input = input.replace("%F", calendar.get(Calendar.YEAR) + "-" + monthS + "-" + dayS);
	    return input;
    }
}