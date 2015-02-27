
package com.delcyon.updater.client;

import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;


public class RequiredLibrariesBuildTask extends Task {
	private Vector<FileSet> filesSetVector = new Vector<FileSet>();
	private String propertyName = "required.libraries";
	private boolean verbose = false;
	private boolean useRelativePaths = false;
	
	public void execute() throws BuildException 
	{
		String requiredLibraryPropertyString = "";
		for (FileSet fileSet : filesSetVector) 
		{
			
			DirectoryScanner directoryScanner = fileSet.getDirectoryScanner(getProject());
			String directoryName = directoryScanner.getBasedir().getName();
			String[] fileNames = directoryScanner.getIncludedFiles();
			
			
			for (String fileName : fileNames) 
			{
				requiredLibraryPropertyString += (useRelativePaths ? "../"+directoryName+"/" : "")+fileName+" ";
				if (verbose == true){
					log("Adding: "+(useRelativePaths ? "../"+directoryName+"/" : "")+fileName);
				}
			}
		}
		if (verbose == true){
			log("Setting "+getPropertyName()+" to "+requiredLibraryPropertyString);
		}
		getProject().setProperty(getPropertyName(), requiredLibraryPropertyString);
	}
	
	public void addFileset(FileSet fileset) {
		filesSetVector.add(fileset);
    }

	
	public final String getPropertyName() {
		return propertyName;
	}

	
	public final void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	
	public final boolean isVerbose() {
		return verbose;
	}

	
	public final void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public final boolean isUseRelativePaths()
	{
		return useRelativePaths;
	}
	
	public final void setUseRelativePaths(boolean useRelativePaths)
	{
		this.useRelativePaths = useRelativePaths;
	}
	
}
