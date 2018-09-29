package org.mcupdater.mojang;

import com.google.gson.JsonElement;

import java.util.List;

public class Arguments {
	private List<JsonElement> game;
	private List<JsonElement> jvm;

	public List<JsonElement> getGame() {
		return game;
	}

	public List<JsonElement> getJvm() {
		return jvm;
	}
}
