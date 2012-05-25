package com.delcyon.updater.client;

public class UpdaterClient
{
	public static final int UPDATES_NOT_INSTALLED = 0;
	public static final int UPDATES_INSTALLED = 1;
	public static final String CHECK_FOR_UPDATES_COMMAND = "checkForUpdatesCommand";
	public static final String CLOSE_COMMAND = "closeCommand";
	public static final String UPDATE_COMMAND = "updateCommand";
	public static final int HAS_UPDATES = 2;
	public static ClassLoader classLoader = null;

	
	public static void setClassLoader(ClassLoader classLoader)
	{
		UpdaterClient.classLoader = classLoader;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		new UpdaterClient().install();
	}

	private InstallModel installModel;

	private int status = UPDATES_NOT_INSTALLED;

	public UpdaterClient()
	{
	}

	public int install()
	{
		status = UPDATES_NOT_INSTALLED;
		runHeadlessInstall();
		return status;
	}

	/**
     * 
     */
	private void runHeadlessInstall()
	{
		if (UpdaterClient.classLoader == null)
		{
			System.out.println("set local classloader");
			UpdaterClient.classLoader = UpdaterClient.class.getClassLoader();
		}
		installModel = new InstallModel(UpdaterClient.classLoader.getResource(InstallModel.DESCRIPTOR_DOCUMENT_NAME));		
		try
		{
		    System.out.println("Processing updates");			
			installModel.checkForUpdates();			
			System.out.println("Processing done");
		} catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

}
