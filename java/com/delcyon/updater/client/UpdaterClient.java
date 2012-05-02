package com.delcyon.updater.client;

public class UpdaterClient
{
	public static final int UPDATES_NOT_INSTALLED = 0;
	public static final int UPDATES_INSTALLED = 1;
	public static final String CHECK_FOR_UPDATES_COMMAND = "checkForUpdatesCommand";
	public static final String CLOSE_COMMAND = "closeCommand";
	public static final String UPDATE_COMMAND = "updateCommand";
	public static final int HAS_UPDATES = 2;

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
		installModel = new InstallModel(UpdaterClient.class.getClassLoader().getResource(InstallModel.DESCRIPTOR_DOCUMENT_NAME));
		try
		{
			System.out.println("Checking for updates");
			installModel.checkForUpdates();
			System.out.println("Processing updates");
			installModel.processUpdates();
			System.out.println("Processing done");
		} catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

}
