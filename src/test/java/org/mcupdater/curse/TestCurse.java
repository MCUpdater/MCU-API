package org.mcupdater.curse;

import org.mcupdater.model.CurseProject;
import org.mcupdater.util.CurseModCache;
import org.mcupdater.util.MCUpdater;

import java.io.File;

public class TestCurse {

	public static void main(String[] args) {
		MCUpdater.getInstance(new File("/home/smbarbour/.MCUpdater"));
		CurseProject cp = new CurseProject("Simply-Transmogrification","1.12.2");
		System.out.println(CurseModCache.fetchURL(cp));
	}
}
