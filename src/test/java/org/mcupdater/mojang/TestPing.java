package org.mcupdater.mojang;

import org.mcupdater.util.ServerStatus;
import org.mcupdater.util.ServerStatusV2;

import java.net.InetSocketAddress;

/**
 * Created by sbarbour on 8/18/17.
 */
public class TestPing {
	public static void main(String[] args) {
		try {
			//ServerStatus status = ServerStatus.getStatus("173.183.121.152");
			ServerStatusV2 status = new ServerStatusV2();
			status.setAddress(new InetSocketAddress("173.183.121.152",25565));
			ServerStatusV2.StatusResponse response = status.fetchData();
			System.out.print(response.getDescription() + "(" + response.getPlayers().getOnline() + "/" + response.getPlayers().getMax() + ")");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
