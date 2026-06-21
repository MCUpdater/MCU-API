package org.mcupdater.loaders;

import org.mcupdater.model.v2.ModSide;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

public interface ILoader {

	boolean install(Path installPath, ModSide side);

	Map<String,String> getClasspathEntries(File instancePath);

	String getArguments(File instancePath);

	String getJVMArguments(File instancePath);

	String getMainClassClient();
}
