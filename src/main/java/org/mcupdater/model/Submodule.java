package org.mcupdater.model;

import java.util.ArrayList;
import java.util.HashMap;

public class Submodule extends GenericModule {

	public Submodule(GenericModule base) {
		super(base.getName(), base.getId(), base.getPrioritizedUrls(), base.getCurseProject(), base.getFilesize(), base.getDepends(), base.getRequired(), base.getModType(), base.getJarOrder(), base.getKeepMeta(), base.getInRoot(), base.getIsDefault(), base.getMD5(), base.getSide().toString(), base.getPath(), base.getMeta(), base.getLaunchArgs(), base.getJreArgs(), base.getParent());
	}

	@Override
	public String getFriendlyName() {
		return "Submodule: " + getName();
	}

	@Override
	public String toString() {
		return getFriendlyName();
	}

	public static Submodule createBlankSubmodule() {
		return new Submodule(new GenericModule("New Submodule","submod", new ArrayList<PrioritizedURL>(),null,100000,"",true,ModType.Regular, 0, false, false, true, "", "BOTH", "", new HashMap<String, String>(), "", "", ""));
	}
}
