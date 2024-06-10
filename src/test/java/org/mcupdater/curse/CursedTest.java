package org.mcupdater.curse;

import org.mcupdater.skynet.SkynetApiV1;
import org.mcupdater.util.MCUpdater;

import java.io.File;
import java.io.IOException;

public class CursedTest {

	public static void main(String[] args) throws IOException, InterruptedException {
		MCUpdater.getInstance(new File("c:\\temp\\MCU-CurseTest"));
		System.out.println(SkynetApiV1.lookupCF(5071019));
	}
}
