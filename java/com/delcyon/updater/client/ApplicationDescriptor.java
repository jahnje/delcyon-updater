
package com.delcyon.updater.client;



public class ApplicationDescriptor {	
	
	private String name = null;
	private String applicationDirectory = null;
	private String executable = null;
	private String icon = null;
	private String ignoredDirectories = null;
	private String installDocumentPath;
	
	public final String getApplicationDirectory() {
		return applicationDirectory;
	}
	
	public final void setApplicationDirectory(String applicationDirectory) {
		this.applicationDirectory = applicationDirectory;
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
	
	public final void setInstallDocumentPath(String installDocumentPath)
	{
		this.installDocumentPath = installDocumentPath;
	}
	
	public String getInstallDocumentPath()
	{
		return installDocumentPath;
	}
}
