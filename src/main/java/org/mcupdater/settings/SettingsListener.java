package org.mcupdater.settings;

public interface SettingsListener
{
	void stateChanged(boolean newState);
	void settingsChanged(Settings newSettings);
}
