package org.mcupdater.mojang;

import org.mcupdater.util.ServerStatusV2;

import java.net.InetSocketAddress;
import java.util.Comparator;

/**
 * Created by sbarbour on 8/18/17.
 */
public class TestPing {
	public static void main(String[] args) {
		try {
			//ServerStatus status = ServerStatus.getStatus("173.183.121.152");
			ServerStatusV2 status = new ServerStatusV2();
			String hostname = "imaginescape.tk"; // "50.92.234.84"
			int port = 25565;
			status.setAddress(new InetSocketAddress(hostname,port));
			ServerStatusV2.StatusResponse response = status.fetchData();
			System.out.printf("%s (%d/%d)\n",response.getDescription().getText(),response.getPlayers().getOnline(),response.getPlayers().getMax());
			response.getForgeData().getMods().stream().sorted((Comparator.comparing(ServerStatusV2.Mod::getModId))).forEach(mod -> {System.out.printf("%s (%s)\n",mod.getModId(),mod.getModmarker());});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
