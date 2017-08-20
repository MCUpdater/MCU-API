package org.mcupdater.mojang;

import org.mcupdater.util.MCUpdater;

public class TestLibs {
	public static void main(String[] args) {
		MCUpdater.getInstance();
		String ver = "1.12.1";
		MinecraftVersion mcVer = MinecraftVersion.loadVersion(ver);

		for (Library lib : mcVer.getLibraries()) {
			if (lib.validForOS()) {
				System.out.println("libraries/"+lib.getFilename());
			}
		}
	}
}
