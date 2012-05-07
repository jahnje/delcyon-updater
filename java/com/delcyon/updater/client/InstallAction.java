package com.delcyon.updater.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.w3c.dom.Element;


public class InstallAction {
	
	public static final int CREATE_APPDIR = 6;	
	public static final int UPDATE = 3;
	public static final int DELETE = 2;	
	public static final int INSERT = 1;
	public static final int IGNORE = 0;
	
	
	
	private static final String DONE = "Done";
	private static final String WAITING = "Update Needed";
	private static final String RUNNING = "Running";
	private static final String DOWNLOADING = "Downloading";
	private static final String VERIFYING = "Verifying";
	private static final String INSTALLING = "Installing";
	public static final String STATUS_PROPERTY = "status";
	public static final String PROGRESS_PROPERTY = "status";
	

	private String systemFileName = null;
	private URL jarFileURL;
	private int actionToTake;
	private String name = "";
	private long size = 1L;
	private String status = WAITING;
	private Exception exception = null;
	private Long progress = 0L;
	
	private String md5sum = null;
	private String tempFileName;
	private String finalOutputfileName;
	
	
	public InstallAction(int actionToTake, URL jarFileURL, String systemFileName,long size,String name,String md5Sum) {		
		this.actionToTake = actionToTake;
		this.jarFileURL = jarFileURL;
		this.systemFileName = systemFileName;
		this.size = size;
		this.name = name;
		this.md5sum = md5Sum;		
	}

	public InstallAction(int actionToTake, String executableFileName,String name,String applicationDirectory,String md5Sum) {		
		this.actionToTake = actionToTake;
		this.systemFileName = executableFileName;
		this.size = 1;
		this.name = name;
		
		this.md5sum = md5Sum;
	}
	
	public void downloadAction() throws FileNotFoundException,IOException, NoSuchAlgorithmException, InvalidFileException{
		setStatus(RUNNING);
		
		switch (actionToTake) {
			case DELETE :
				getSystemFile().delete();				
				break;
			case UPDATE :
				downloadResource();			
				break;
			case INSERT :
				downloadResource();
				break;
			case CREATE_APPDIR :
				createApplicationDirectory(systemFileName);
				break;
			
			default :
				break;
		}
		setProgress(size);
		setStatus(DONE);		
	}

	

	public final Exception getException() {
		return exception;
	}


	private void setStatus(String status){
		String oldStatus = this.status;
		this.status = status;
		
	}
	
	private void setProgress(Long progress){		
		Long oldProgress = this.progress;
		this.progress = progress;
	}
	
	/**
	 * @return
	 */
	private File getSystemFile() {
		File file = new File(systemFileName);
		return file;
	}
	
	
	public final long getSize() {
		return size;
	}

	/**
	 * @throws IOException 
	 * 
	 */
	public void installAction() throws IOException {
		setStatus(INSTALLING);
		if (actionToTake == UPDATE){
			getSystemFile().delete();
		}
		
		if (finalOutputfileName != null) {
			new File(tempFileName).renameTo(new File(finalOutputfileName));
//			FileOutputStream fileOutputStream = new FileOutputStream(finalOutputfileName);
//			long totalDataRead = 0;
//			if (jarFileURL != null) {
//				InputStream inputStream = new FileInputStream(tempFileName);
//
//				System.out.print("writing " + tempFileName + " ...");
//				byte[] inputBuffer = new byte[1024];
//				int value = 0;
//				while (true) {
//					value = inputStream.read(inputBuffer);
//					totalDataRead += value;
//					if (value < 0) {
//						break;
//					}
//					fileOutputStream.write(inputBuffer, 0, value);
//					setProgress(totalDataRead);
//				}
//
//				inputStream.close();
//			}
//			fileOutputStream.flush();
//			fileOutputStream.close();
		}
		setStatus(DONE);
	}
	
	public void cleanupAction() {
		if (tempFileName != null){
			File file = new File(tempFileName);
			file.delete();
		}
		setStatus(DONE);
	}
	private void downloadResource() throws FileNotFoundException,IOException, NoSuchAlgorithmException, InvalidFileException{
		finalOutputfileName = getSystemFile().getAbsolutePath().replaceAll("/", File.separator);
		tempFileName = finalOutputfileName+md5sum+"__";
		File systemFile = getSystemFile();
		insureDirectoryExists(systemFile.getParentFile());
		//check to see if the temp file already exists
		File tempFile = new File(tempFileName);
		if (tempFile.exists()){
			FileInputStream tempFileInputStream = new FileInputStream(tempFileName);
			//if it does validate it, and if it matches return finished
			if(verifyDownload(tempFileInputStream) == true){
				tempFileInputStream.close();
				setProgress(size);
				return;
			}
			//else delete it and get a new copy
			else {
				tempFileInputStream.close();
				tempFile.delete();
			}
		}
		FileOutputStream fileOutputStream = new FileOutputStream(tempFileName);
		long totalDataRead = 0;		
		if (jarFileURL != null){
			InputStream inputStream = jarFileURL.openStream();
			setStatus(DOWNLOADING);			
			System.out.print("writing "+tempFileName+" ...");
			byte[] inputBuffer = new byte[1024];
			int value = 0;
			while (true) {					
				value = inputStream.read(inputBuffer);
				totalDataRead += value;
				if (value < 0) {
					break;
				}
				fileOutputStream.write(inputBuffer,0,value);
				setProgress(totalDataRead);				
			}
									
			inputStream.close();		
		}		
		fileOutputStream.flush();
		fileOutputStream.close();
		FileInputStream tempFileInputStream = new FileInputStream(tempFileName);
		if(verifyDownload(tempFileInputStream) == false){
			tempFileInputStream.close();		
			throw new InvalidFileException(this);
		}
		tempFileInputStream.close();
		System.out.println("done.");
	}

	/**
	 * @param stream
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidFileException 
	 */
	private boolean verifyDownload(FileInputStream fileInputStream) throws IOException, NoSuchAlgorithmException, InvalidFileException {
		boolean isValid = false;
		setStatus(VERIFYING);
		MessageDigest messageDigest = MessageDigest.getInstance("MD5");		
			
		String fileMD5Sum = null;		
		//read in data from system file to md5 sum
		byte[] inputBuffer = new byte[1024];
		while (true) {					
			int value = fileInputStream.read(inputBuffer);
			if (value < 0) {
				break;
			}
			messageDigest.update(inputBuffer,0,value);
		}		
		fileInputStream.close();
		//convert the byte array from the md5 digest to a string in hex
		fileMD5Sum = new BigInteger(1,messageDigest.digest()).toString(16);
		if (md5sum != null){
			isValid = fileMD5Sum.equals(md5sum); 
			
		}
		else {
			isValid = true;
		}
		return isValid;
	}

	/**
	 * @param parentFile
	 */
	private void insureDirectoryExists(File parentFile) {
		//System.out.println(parentFile.getName());
		if (parentFile != null && parentFile.exists() == false){			
			System.out.println("creating "+parentFile.getAbsolutePath().replaceAll("/", File.separator));
			parentFile.mkdirs();
		}
		
	}
	
	public String getActionTypeName(){
		switch (actionToTake) {
			case DELETE :				
				return "Delete";				
			case UPDATE :
				return "Update";
			case INSERT :
				return "Create";
			case CREATE_APPDIR :
				return "Create";
		}
		return "";
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {		
		return getActionTypeName()+" "+systemFileName; 
	}


	/**
	 * @return
	 */
	public String getFileName() {
		return name;
	}


	/**
	 * @return
	 */
	public String getStatus() {
		return status;
	}
	
	private void createApplicationDirectory(String applicationDirectory){
		insureDirectoryExists(new File(applicationDirectory));
		setProgress(1L);
	}
	
	
	
	
	

	

	/**
	 * @return
	 */
	public long getProgress() {	
		return progress;
	}

	/**
	 * 
	 */
	

	

}

