package org.mcupdater.model;

public class Submodule extends GenericModule {

	public Submodule(GenericModule base) {
		super(base.getName(), base.getId(), base.getPrioritizedUrls(), base.getDepends(), base.getRequired(), base.getModType(), base.getJarOrder(), base.getKeepMeta(), base.getInRoot(), base.getIsDefault(), base.getMD5(), base.getSide().toString(), base.getPath(), base.getMeta(), base.getLaunchArgs(), base.getJreArgs(), base.getParent());
	}

	@Override
	public String getFriendlyName() {
		return "Submodule: " + getName();
	}

	@Override
	public String toString() {
		return getFriendlyName();
	}
}
