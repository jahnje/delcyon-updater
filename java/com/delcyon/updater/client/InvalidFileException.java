package com.delcyon.updater.client;


public class InvalidFileException extends Exception {

	private InstallAction action;

	/**
	 * @param action
	 */
	public InvalidFileException(InstallAction action) {
		super("Downloaded file "+action.getFileName()+" failed verification.");
		this.action = action;
	}

	
	public final InstallAction getAction() {
		return action;
	}
	
	
}
