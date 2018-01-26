package org.mcupdater.mojang;

import org.mcupdater.util.ServerStatus;

/**
 * Created by sbarbour on 8/18/17.
 */
public class TestPing {
	public static void main(String[] args) {
		try {
			ServerStatus status = ServerStatus.getStatus("mc.imaginescape.tk");
			System.out.print(status.getMOTD() + "(" + status.getPlayers() + "/" + status.getMaxPlayers() + ")");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
