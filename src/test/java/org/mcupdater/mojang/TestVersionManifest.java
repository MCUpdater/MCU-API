package org.mcupdater.mojang;

/**
 * Created by sbarbour on 2/11/17.
 */
public class TestVersionManifest {


	public static void main(String[] args) {
		try {
			VersionManifest manifest = VersionManifest.getCurrent(false);
			System.out.println("Release: " + manifest.getLatest().getRelease());
			System.out.println("Snapshot: " + manifest.getLatest().getSnapshot());
			for (VersionManifest.VersionInfo version : manifest.getVersions()) {
				System.out.println(version.getId() + " - " + version.getUrl());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
