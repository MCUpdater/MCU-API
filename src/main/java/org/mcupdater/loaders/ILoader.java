package org.mcupdater.loaders;

import org.mcupdater.model.ModSide;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public interface ILoader {

	boolean install(Path installPath, ModSide side);

	List<String> getClasspathEntries(File instancePath);

	String getArguments(File instancePath);

	String getMainClassClient();
}
