package com.delcyon.updater.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.updater.client.XPathFunctionResolverImpl.XPathMatchesFunction;
import com.delcyon.updater.client.XPathFunctionResolverImpl.XPathReplaceAllFunction;

public class InstallerBuildTask extends Task
{

	private Hashtable<String, FileDescriptor> fileDescriptorHashtabe = new Hashtable<String, FileDescriptor>();
	private String distributionFileName = null;
	private Vector<FileSet> filesSetVector = new Vector<FileSet>();
	private ApplicationDescriptor applicationDescriptor;
	
	@SuppressWarnings("unchecked")
	

	public void execute() throws BuildException
	{
		if (applicationDescriptor == null)
		{
			throw new BuildException("ApplicationDescriptor must be set");
		}
		else if (filesSetVector.size() == 0)
		{
			throw new BuildException("empty file set");
		}
		else if (distributionFileName == null)
		{
			throw new BuildException("distributionFileName not set");
		}
		try
		{
			Manifest manifest = new Manifest();
			manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
			manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "com.delcyon.updater.client.UpdaterClient");
			File distributionFile = new File(getProject().getBaseDir(),distributionFileName);
			System.out.println(new File(".").getCanonicalPath());
			if (distributionFile.exists() == false)
			{
				distributionFile.createNewFile();
			}
			FileOutputStream distributionFileOutputStream = new FileOutputStream(distributionFile, false);
			JarOutputStream distributionJarOutputStream = new JarOutputStream(distributionFileOutputStream, manifest);
			log("Storing class files");
			storeInstallerClassFiles(distributionJarOutputStream);
			log("create default entries");
			createDefaultEntries(applicationDescriptor, filesSetVector);
			log("Storing fileset");
			storeFileSet(filesSetVector, distributionJarOutputStream);
			log("Storing descriptor");
			storeDescriptorFile(distributionJarOutputStream);
			distributionJarOutputStream.flush();
			distributionJarOutputStream.close();
		} catch (Exception e)
		{
			BuildException buildException = new BuildException(e.getLocalizedMessage());
			buildException.setStackTrace(e.getStackTrace());
			throw buildException;
		}

	}

	/**
	 * @param applicationDescriptor2
	 * @param filesSetVector2
	 */
	private void createDefaultEntries(ApplicationDescriptor applicationDescriptor, Vector<FileSet> filesSetVector)
	{

		for (FileSet fileSet : filesSetVector)
		{
			DirectoryScanner directoryScanner = fileSet.getDirectoryScanner(getProject());
			String fileSetRelativeDir = fileSet.getDir(getProject()).getAbsolutePath();
			String[] fileNames = directoryScanner.getIncludedFiles();

			try
			{
				for (String fileName : fileNames)
				{
					FileDescriptor fileDescriptor = new FileDescriptor(new FileInputStream(fileSetRelativeDir + File.separator + fileName));
					fileDescriptor.setJarFileName(fileName);
					fileDescriptor.setSystemFileName(fileName);					
					log(fileName);
					fileDescriptorHashtabe.put(fileName, fileDescriptor);

				}
			} catch (FileNotFoundException fileNotFoundException)
			{
				log(fileNotFoundException.getLocalizedMessage());
			}
		}
	}

	/**
	 * @param filesSetVector2
	 * @param distributionJarOutputStream
	 * @throws IOException
	 */
	private void storeFileSet(Vector<FileSet> filesSetVector, JarOutputStream distributionJarOutputStream) throws IOException
	{
		Set<Entry<String, FileDescriptor>> fileDecriptorSet = fileDescriptorHashtabe.entrySet();
		for (Entry<String, FileDescriptor> entry : fileDecriptorSet)
		{
			ZipEntry zipEntry = new ZipEntry(entry.getValue().getJarFileName());
			distributionJarOutputStream.putNextEntry(zipEntry);
			copyInputStreamToOutputStream(entry.getValue().getData(), distributionJarOutputStream);
			distributionJarOutputStream.closeEntry();
		}

	}

	/**
	 * @param distributionJarOutputStream
	 * @throws IOException
	 */
	private void storeDescriptorFile(JarOutputStream distributionJarOutputStream) throws Exception
	{
		InputStream inputStream = null;
		log("Creating Dynamic Descriptor", Project.MSG_INFO);
		inputStream = createDynamicDescriptorInputStream();
		log("Creating Dynamic Descriptor.. done", Project.MSG_INFO);
		ZipEntry zipEntry = new ZipEntry("descriptor.xml");
		distributionJarOutputStream.putNextEntry(zipEntry);
		copyInputStreamToOutputStream(inputStream, distributionJarOutputStream);
		inputStream.close();
		distributionJarOutputStream.closeEntry();
	}

	/**
	 * @return
	 * @throws IOException
	 */
	private InputStream createDynamicDescriptorInputStream() throws Exception
	{

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(false);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.newDocument();

		Element applicationDescriptorElement = document.createElement("applicationDescriptor");
		document.appendChild(applicationDescriptorElement);
		applicationDescriptorElement.setAttribute("name", applicationDescriptor.getName());
		applicationDescriptorElement.setAttribute("applicationDirectory", applicationDescriptor.getApplicationDirectory());
		applicationDescriptorElement.setAttribute("executable", applicationDescriptor.getExecutable());
		applicationDescriptorElement.setAttribute("ignoredDirectories", applicationDescriptor.getIgnoredDirectories());
		if (applicationDescriptor.getInstallDocumentPath() != null)
		{
			applicationDescriptorElement.setAttribute("installDocumentPath", applicationDescriptor.getInstallDocumentPath());
		}
		if (applicationDescriptor.getIcon() != null)
		{
			applicationDescriptorElement.setAttribute("icon", applicationDescriptor.getIcon());
		}

		Set<Entry<String, FileDescriptor>> fileDecriptorSet = fileDescriptorHashtabe.entrySet();
		for (Entry<String, FileDescriptor> entry : fileDecriptorSet)
		{

			applicationDescriptorElement.appendChild(document.adoptNode(entry.getValue().getElement()));
		}

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		XMLUtils.dumpNode(document, buffer);
		return new ByteArrayInputStream(buffer.toByteArray());
	}

	@SuppressWarnings("unchecked")
	private void storeInstallerClassFiles(JarOutputStream distributionJarOutputStream) throws Exception
	{
	    
	    Class[] classes = getClasses(this.getClass().getPackage().getName());
	    
		for (Class classPath : classes)
		{
			String classPathString = classPath.getName().replaceAll("\\.", "/") + ".class";
			URL clientURL = InstallerBuildTask.class.getClassLoader().getResource(classPathString);
			ZipEntry zipEntry = new ZipEntry(classPathString);
			distributionJarOutputStream.putNextEntry(zipEntry);
			InputStream inputStream = clientURL.openStream();
			copyInputStreamToOutputStream(inputStream, distributionJarOutputStream);
			distributionJarOutputStream.closeEntry();
		}

	}

	private void copyInputStreamToOutputStream(InputStream inputStream, OutputStream outputStream) throws IOException
	{
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		while (true)
		{
			int value = inputStream.read();
			if (value < 0)
			{
				break;
			}
			buffer.write(value);
		}
		outputStream.write(buffer.toByteArray());
		outputStream.flush();
		buffer.close();
	}

	private void copyInputStreamToOutputStream(byte[] data, OutputStream outputStream) throws IOException
	{
		outputStream.write(data);
		outputStream.flush();

	}

	public final void setDistributionFileName(String distributionFileName)
	{
		this.distributionFileName = distributionFileName;
	}

	public void addFileset(FileSet fileset)
	{
		filesSetVector.add(fileset);
	}

	public void addConfiguredApplicationDescriptor(ApplicationDescriptor applicationDescriptor)
	{
		this.applicationDescriptor = applicationDescriptor;
	}

	/**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private Class[] getClasses(String packageName) throws ClassNotFoundException, IOException
    {    
//        log(InstallerBuildTask.class.getProtectionDomain().getCodeSource().getLocation().toString(),Project.MSG_INFO);
//        log(getClass().getProtectionDomain().getCodeSource().getLocation().toString(),Project.MSG_INFO);
//        
//        log("looking in: "+packageName, Project.MSG_INFO);
//        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//        
//        String path = packageName.replace('.', '/');
//        log("searching: "+path, Project.MSG_INFO);
//        Enumeration<URL> resources = classLoader.getResources(path);
//        List<File> dirs = new ArrayList<File>();
//        while (resources.hasMoreElements())
//        {
//            URL resource = resources.nextElement();
//            dirs.add(new File(resource.getFile()));
//           
//            log("found: "+resource, Project.MSG_INFO);
//        }
        ArrayList<Class> classes = new ArrayList<Class>();
        
        classes.add(CentralServicesClient.class);
        classes.add(CentralServicesClient.MD5FilterOutputStream.class);
        classes.add(CentralServicesClient.NullOutputStream.class);        
        classes.add(CSFilterOutputStream.class);
        classes.add(FileDescriptor.class);
        classes.add(FileUtility.class);        
        classes.add(InstallModel.class);
        classes.add(LeveledConsoleHandler.class);
        classes.add(LeveledConsoleHandler.Output.class);
        classes.add(ApplicationDescriptor.class);
        classes.add(UpdaterClient.class);
        classes.add(XMLUtils.class);
        classes.add(XPathFunctionResolverImpl.class);
        classes.add(XPathReplaceAllFunction.class);
        classes.add(XPathMatchesFunction.class);
//        for (File directory : dirs)
//        {
//            classes.addAll(findClasses(directory, packageName));
//        }
        log("found: "+classes.size()+" classes", Project.MSG_INFO);
        
        return classes.toArray(new Class[classes.size()]);
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) 
        {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) 
            {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } 
            else if (file.getName().endsWith(".class")) 
            {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

	
}
