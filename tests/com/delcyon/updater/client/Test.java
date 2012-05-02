package com.delcyon.updater.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;


public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 0){
			
			while(true){
				try {
					Thread.sleep(200);
				}
				catch (InterruptedException e) {
					
					e.printStackTrace();
				}
				System.out.println(".");
			}
		}
		else {
			ProcessBuilder processBuilder = new ProcessBuilder("java","com.delcyon.updater.client.Test","blah");
			processBuilder.redirectErrorStream(true);
			
			processBuilder.directory(new File("build"));
//			System.out.println(processBuilder.directory().getAbsolutePath());
//			System.out.println(processBuilder.environment());
			
			File fileToReplace = new File("build\\com\\delcyon\\updater\\client\\Test.class");
			File fileToReplaceWith = new File("build\\Test.class");
			
			fileToReplace.delete();
			fileToReplace = new File("build\\com\\delcyon\\updater\\client\\Test.class");
			try {
				FileOutputStream fileOutputStream = new FileOutputStream(fileToReplace);
				FileInputStream fileInputStream = new FileInputStream(fileToReplaceWith);
				while(fileInputStream.available() > 0){
					fileOutputStream.write(fileInputStream.read());
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				Process process = processBuilder.start();				
				InputStream inputStream = process.getInputStream();
				Thread.sleep(2000);
				while(inputStream.available() > 0){
					System.out.append((char) inputStream.read());
				}
				Thread.sleep(15000);	
			}
			catch (Exception e) {
				
				e.printStackTrace();
			}
			System.out.println("killing init startup app");
			Runtime.getRuntime().exit(0);
			System.exit(0);
		}

	}

}
