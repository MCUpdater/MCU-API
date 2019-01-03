package org.mcupdater.api;

import org.mcupdater.MCUApp;

import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version {
	public static final int MAJOR_VERSION;
	public static final int MINOR_VERSION;
	public static final int BUILD_VERSION;
	public static final String BUILD_BRANCH;
	public static final String BUILD_LABEL;
	static {
		Properties prop = new Properties();
		try {
			prop.load(Version.class.getResourceAsStream("/org/mcupdater/api/version.properties"));
		} catch (IOException e) {
		}
		int major;
		int minor;
		int build;
		String branch;
		try {
			major = Integer.valueOf(prop.getProperty("major", "0"));
			minor = Integer.valueOf(prop.getProperty("minor", "0"));
			build = Integer.valueOf(prop.getProperty("build_version", "0"));
			branch = prop.getProperty("git_branch", "unknown");
		} catch (Exception e) {
			major = 3;
			minor = 4;
			build = 999;
			branch = "develop";
		}
		MAJOR_VERSION = major;
		MINOR_VERSION = minor;
		BUILD_VERSION = build;
		BUILD_BRANCH = branch;
		if( BUILD_BRANCH.equals("unknown") || BUILD_BRANCH.equals("master") ) {
			BUILD_LABEL = "";
		} else {
			BUILD_LABEL = " ("+BUILD_BRANCH+")";
		}
	}
	
	public static final String API_VERSION = MAJOR_VERSION + "." + MINOR_VERSION;
	public static final String VERSION = "v"+MAJOR_VERSION+"."+MINOR_VERSION+"."+BUILD_VERSION;
	
	public static boolean isVersionOld(String packVersion) {
		if( packVersion == null ) return false;	// can't check anything if they don't tell us
		String parts[] = packVersion.split("\\.");
		try {
			int mcuParts[] = { MAJOR_VERSION, MINOR_VERSION, BUILD_VERSION };
			for( int q = 0; q < mcuParts.length && q < parts.length; ++q ) {
				int packPart = Integer.valueOf(parts[q]);
				if( packPart > mcuParts[q] ) return true;
				if( packPart < mcuParts[q] ) return false; // Since we check major, then minor, then build, if the required value < current value, we can stop checking.
			}
			return false;
		} catch( NumberFormatException e ) {
			log("Got non-numerical pack format version '"+packVersion+"'");
		} catch( ArrayIndexOutOfBoundsException e ) {
			log("Got malformed pack format version '"+packVersion+"'");
		}
		return false;
	}

	public static boolean fuzzyMatch(String version1, String version2) {
		return version1.equals(version2) || version1.startsWith(version2) || version2.startsWith(version1);
	}

	public static boolean requestedFeatureLevel(String packVersion, String featureLevelVersion) {
		String packParts[] = packVersion.split("\\.");
		String featureParts[] = featureLevelVersion.split("\\.");
		try {
			for (int q = 0; q < featureParts.length; ++q ) {
				if (Integer.valueOf(packParts[q]) > Integer.valueOf(featureParts[q])) return true;
				if (Integer.valueOf(packParts[q]) < Integer.valueOf(featureParts[q])) return false;
			}
			return true;
		} catch( NumberFormatException e ) {
			// try as a snapshot before throwing the exception
			if (isSnapshot(packVersion)) {
				final SnapshotVersion ss = new SnapshotVersion(packVersion);
				final String releaseFamily = ss.toReleaseFamily();
				log("Got snapshot pack version '"+packVersion+"', parsed as "+ss+", treating like release version '"+releaseFamily+"'");
				return requestedFeatureLevel(releaseFamily, featureLevelVersion);
			} else {
				log("Got non-numerical pack format version '" + packVersion + "'");
			}
		} catch( ArrayIndexOutOfBoundsException e ) {
			log("Got malformed pack format version '"+packVersion+"'");
		}
		return false;
	}
	
	public static boolean isMasterBranch() {
		return BUILD_BRANCH.equals("master");
	}
	public static boolean isDevBranch() {
		return BUILD_BRANCH.equals("develop");
	}
	
	// for error logging support
	public static void setApp( MCUApp app ) {
		_app = app;
	}
	private static MCUApp _app;
	private static void log(String msg) {
		if( _app != null ) {
			_app.log(msg);
		} else {
			System.out.println(msg);
		}
	}

	// snapshot handling ////////////////////////////////////////////////////

	private static final String snapshotRegex = "^(?<year>\\d+)w(?<week>\\d+)(?<build>[a-z])$";
	public static boolean isSnapshot(String version) {
		return version.matches(snapshotRegex);
	}

	public static class SnapshotVersion {
		public final int YEAR;
		public final int WEEK;
		public final char BUILD;

		public SnapshotVersion(String version) {
			Pattern p = Pattern.compile(snapshotRegex);
			Matcher m = p.matcher(version);

			if (m.find()) {
				YEAR = Integer.parseInt(m.group("year"));
				WEEK = Integer.parseInt(m.group("week"));
				BUILD = m.group("build").charAt(0);
			} else {
				throw new IllegalArgumentException();
			}
		}

		public String toString() {
			// NB: this isn't right because mojang 0-pads their weeks
			return YEAR+"w"+WEEK+BUILD;
		}

		// TODO: make this prettier
		public String toReleaseFamily() {
			if( YEAR >= 18 && WEEK >= 43 ) {
				// 18w43a = 1.14
				return "1.14";
			} else if( YEAR >= 17 && WEEK >= 43 ) {
				// 17w43a = 1.13
				return "1.13";
			} else if( YEAR >= 17 && WEEK >= 6 ) {
				// 17w06a = 1.12
				return "1.12";
			} else if( YEAR >= 16 && WEEK >= 32 ) {
				// 16w32a = 1.11
				return "1.11";
			} else if( YEAR >= 16 && WEEK >= 20 ) {
				// 16w20a = 1.10
				return "1.10";
			} else if( YEAR >= 15 && WEEK >= 31 ) {
				// 15w31a = 1.9
				return "1.9";
			} else if( YEAR >= 14 && WEEK >= 2 ) {
				// 14w02a = 1.8
				return "1.8";
			} else if( YEAR >= 13 && WEEK >= 36 ) {
				// 13w36a = 1.7
				return "1.7";
			} else if( YEAR >= 13 && WEEK >= 16 ) {
				// 13w16a = 1.6
				return "1.6";
			} else if( YEAR >= 13 && WEEK >= 1 ) {
				// 13w01a = 1.5
				return "1.5";
			} else if( YEAR >= 12 && WEEK >= 32 ) {
				// 12w32a = 1.4
				return "1.4";
			} else if( YEAR >= 12 && WEEK >= 15 ) {
				// 12w15a = 1.3
				return "1.3";
			} else if( YEAR >= 12 && WEEK >= 3 ) {
				// 12w03a = 1.2
				return "1.2";
			} else if( YEAR >= 11 && WEEK >= 47 ) {
				// 11w47a = 1.1
				return "1.1";
			} else {
				return "1.0";
			}
		}
	}
}
