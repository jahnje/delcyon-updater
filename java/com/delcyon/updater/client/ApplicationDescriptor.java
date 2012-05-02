
package com.delcyon.updater.client;



public class ApplicationDescriptor {	
	
	private String name = null;
	private String installDirectory = null;
	private String executable = null;
	private String icon = null;
	private String ignoredDirectories = null;
	
	public final String getInstallDirectory() {
		return installDirectory;
	}
	
	public final void setInstallDirectory(String installDirectory) {
		this.installDirectory = installDirectory;
	}
	
	public final String getName() {
		return name;
	}
	
	public final void setName(String name) {
		this.name = name;
	}

	
	public final String getExecutable() {
		return executable;
	}

	
	public final void setExecutable(String executable) {
		this.executable = executable;
	}

	
	public final String getIcon() {
		return icon;
	}

	
	public final void setIcon(String icon) {
		this.icon = icon;
	}

	public final String getIgnoredDirectories()
	{
		return this.ignoredDirectories ;
	}

	public final void setIgnoredDirectories(String ignoredDirectories)
	{
		this.ignoredDirectories = ignoredDirectories;
	}
	
}
