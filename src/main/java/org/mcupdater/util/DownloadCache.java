package org.mcupdater.util;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class DownloadCache {
	private static DownloadCache instance;
	private final File dir;
	
	private DownloadCache(File dir) {
		this.dir = dir;
		this.dir.mkdirs();
	}
	
	public static void init(File dir) {
		if( instance != null ) {
			throw new IllegalArgumentException("Attempt to reinitialize download cache.");
		}
		MCUpdater.apiLogger.info("Initializing DownloadCache in "+dir);
		instance = new DownloadCache(dir);
	}
	
	public static File getDir() {
		instance.dir.mkdirs();
		return instance.dir;
	}
	
	public static boolean cacheFile(File file, String expectedMD5) {
		byte[] hash;
		try {
			InputStream is = new FileInputStream(file);
			hash = DigestUtils.md5(is);
			is.close();		
		} catch (FileNotFoundException e) {
			MCUpdater.apiLogger.log(Level.SEVERE, "File not found", e);
			return false;
		} catch (IOException e) {
			MCUpdater.apiLogger.log(Level.SEVERE, "I/O Error", e);
			return false;
		}
		String chksum = new String(Hex.encodeHex(hash));
		if( !chksum.equals(expectedMD5) ) {
			// checksums do not match, abort :)
			return false;
		}
		
		File destFile = getFile(chksum);
		if( !destFile.exists() ) {
			try {
				FileUtils.copyFile(file, destFile);
			} catch (IOException e) {
				MCUpdater.apiLogger.log(Level.SEVERE, "I/O Error", e);
				return false;
			}
		}
		
		return true;
	}
	
	public static File getFile(String chksum) {
		final File file = getDir().toPath().resolve(chksum + ".bin").toFile();
		return file;
	}

	public static void cull(Set<String> digests) {
		Path cache = instance.dir.toPath();
		PathWalker pathWalk = new PathWalker(digests);
		try {
			Files.walkFileTree(cache, pathWalk);
		} catch (IOException e) {
			MCUpdater.apiLogger.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	public static void purge() {
		Path cache = instance.dir.toPath();
		PathWalker pathWalk = new PathWalker(new HashSet<String>());
		try {
			Files.walkFileTree(cache, pathWalk);
		} catch (IOException e) {
			MCUpdater.apiLogger.log(Level.SEVERE, e.getMessage(), e);
		}

	}

	private static class PathWalker extends SimpleFileVisitor<Path> {
		private final Set<String> digests;

		public PathWalker(Set<String> digests) {
			this.digests = digests;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			String[] split = instance.dir.toPath().relativize(file).toString().split("\\.");
			if (!digests.contains(split[0])) {
				Files.delete(file);
				MCUpdater.apiLogger.info("Deleted " + file.toString());
			}
			return FileVisitResult.CONTINUE;
		}
	}
}
