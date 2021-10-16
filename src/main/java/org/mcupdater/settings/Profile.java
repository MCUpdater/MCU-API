package org.mcupdater.settings;

import com.google.gson.*;
import org.mcupdater.MCUApp;
import org.mcupdater.model.JSON;

import java.lang.reflect.Type;

@JSON
public abstract class Profile {
	protected String style;
	protected String name;
	protected String uuid;
	protected String lastInstance;

	public abstract String getSessionKey(MCUApp caller) throws Exception;
	public abstract String getAuthAccessToken();

	public String getStyle() {
		return style;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastInstance() {
		return lastInstance;
	}

	public void setLastInstance(String lastInstance) {
		this.lastInstance = lastInstance;
	}

	public String getUUID() {
		return uuid;
	}

	public void setUUID(String uuid) { this.uuid = uuid; }

	@Override
	public String toString() { return this.getName(); }

	public abstract boolean refresh();

	public static class ProfileJsonHandler implements JsonDeserializer<Profile>, JsonSerializer<Profile> {

		@Override
		public Profile deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jsonObject = json.getAsJsonObject();
			switch (jsonObject.get("style").getAsString()) {
				case "Yggdrasil":
					return context.deserialize(jsonObject, YggdrasilProfile.class);
				case "MSA":
					return context.deserialize(jsonObject, MSAProfile.class);
				default:
					return context.deserialize(jsonObject, BasicProfile.class);
			}
		}

		@Override
		public JsonElement serialize(Profile src, Type typeOfSrc, JsonSerializationContext context) {
			switch (src.getStyle()) {
				case "Yggdrasil":
					return context.serialize(src, YggdrasilProfile.class);
				case "MSA":
					return context.serialize(src, MSAProfile.class);
				default:
					return context.serialize(src, BasicProfile.class);
			}
		}
	}
}
