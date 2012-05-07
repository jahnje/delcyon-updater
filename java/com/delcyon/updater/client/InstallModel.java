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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class InstallModel
{

	public static final String DESCRIPTOR_DOCUMENT_NAME = "descriptor.xml";
	public static final boolean isWindows = System.getProperty("os.name", "").trim().startsWith("Windows");

	

	public static String filesPath = "/applicationDescriptor/fileDescriptor";

	private int numberOfActionsToPerform = 0;
	private int currentActionNumber = 0;
	private String statusText = "";
	private boolean isDone = false;
	private URL url;
	private Document descriptorDocument;
	private String applicationDirectory;


	private Vector<InstallAction> installActionVector = new Vector<InstallAction>();

	
	private Exception exeception;
	private DocumentBuilder documentBuilder;
	private Document installDocument;
	private CentralServices centralServices;
	private Document statusDocument;
	private CentralServicesClient centralServicesClient;

	/**
	 * @param updatePath
	 */
	public InstallModel(URL url)
	{
		this.url = url;

	}

	public final URL getUrl()
	{
		return url;
	}

	/**
	 * @return the applicationDirectory
	 */
	public String getApplicationDirectory()
	{
		return applicationDirectory;
	}

	/**
	 * @param applicationDirectory
	 *            the applicationDirectory to set
	 */
	public void setApplicationDirectory(String applicationDirectory)
	{
		this.applicationDirectory = applicationDirectory;
	}

	private Document getDescriptorDocument() throws Exception
	{

		try
		{
			if (descriptorDocument == null)
			{
				DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
				documentBuilderFactory.setNamespaceAware(false);
				documentBuilder = documentBuilderFactory.newDocumentBuilder();
				URL descriptorURL = new URL(url.toExternalForm());
				descriptorDocument = documentBuilder.parse(descriptorURL.openStream());
				setApplicationDirectory(descriptorDocument.getDocumentElement().getAttribute("installDirectory"));
				if (descriptorDocument.getDocumentElement().hasAttribute("installDocumentPath"))
				{
					loadInstallDocument(descriptorDocument.getDocumentElement().getAttribute("installDocumentPath"));
				}
			}
		} catch (IOException e)
		{
			System.out.println(url);
			throw e;
		}
		return descriptorDocument;
	}

	public boolean hasUpdates()
	{
		return (installActionVector.size() > 0 || statusDocument != null);
	}

	
	public void checkForUpdates() throws Exception
	{
		
		getDescriptorDocument();		
		String ignoredDirectoryString = descriptorDocument.getDocumentElement().getAttribute("ignoredDirectories");
		String[] ignoredDirectoryStrings = ignoredDirectoryString.split(",");
		boolean foundOtherFiles = false;
		if (installDocument != null)
		{
			Element copyOtherFilesElement = (Element) XMLUtils.selectSingleNode(installDocument, "//CopyOtherFiles");
			if (copyOtherFilesElement != null)
			{
				foundOtherFiles = true;
				NodeList fileDecriptorNodes = XMLUtils.selectNodes(descriptorDocument, filesPath);
				for(int index = 0; index < fileDecriptorNodes.getLength(); index++)
				{
					
					Element fileDescriptorElement = (Element) fileDecriptorNodes.item(index);
					Element copyElement = copyOtherFilesElement.getOwnerDocument().createElement("copy");
					copyElement.setAttribute("dest", XMLUtils.evaluateXPathString(fileDescriptorElement, copyOtherFilesElement.getAttribute("dest")));
					copyElement.setAttribute("name", fileDescriptorElement.getAttribute("jarFileName"));			
					if (XMLUtils.selectSingleNode(installDocument, "//copy[@name = '"+fileDescriptorElement.getAttribute("jarFileName")+"']") != null)
					{						
						continue;
					}
					copyOtherFilesElement.getParentNode().insertBefore(copyElement, copyOtherFilesElement);
				}
				copyOtherFilesElement.getParentNode().removeChild(copyOtherFilesElement);
			}
			centralServicesClient = new CentralServicesClient();
			this.centralServices = new CentralServices(installDocument.getDocumentElement());
			centralServicesClient.setCentralServices(this.centralServices);
			Document requestDocument = CentralServicesClient.initDocument();
			requestDocument.getDocumentElement().setAttribute("RequestType", "STATUS_CHECK");
			CentralServicesRequest centralServicesRequest = new CentralServicesRequest(requestDocument.getDocumentElement(),centralServices);
			this.statusDocument = centralServicesRequest.processStatusCheck();
			
			

		}
		
		XMLUtils.dumpNode(installDocument, System.out);
		if (foundOtherFiles == false)
		{
			determineActions(applicationDirectory, XMLUtils.selectNodes(descriptorDocument, filesPath), Arrays.asList(ignoredDirectoryStrings));
		}
		
	}

	private void loadInstallDocument(String documentPath) throws Exception
	{
		try
		{
			if (installDocument == null)
			{
				DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
				documentBuilderFactory.setNamespaceAware(false);
				documentBuilder = documentBuilderFactory.newDocumentBuilder();
				URL descriptorURL = UpdaterClient.classLoader.getResource(documentPath);
				installDocument = documentBuilder.parse(descriptorURL.openStream());
				
			}
		} catch (IOException e)
		{
			System.out.println(url);
			throw e;
		}
		
	}

	private Vector<InstallAction> determineActions(String applicationDirectory, NodeList nodeList,List<String> ignoredDirectoryList) throws Exception
	{
		setNumberOfActionsToPerform(nodeList.getLength());
		// find all files on file system and create hastable of files keyed by
		// file names
		Hashtable<String, File> systemFileHastable = findAllFiles(applicationDirectory, null,ignoredDirectoryList);
		// System.out.println(systemFileHastable);
		// System.out.println(applicationDirectory);

		if (new File(applicationDirectory).exists() == false)
		{
			installActionVector.add(new InstallAction(InstallAction.CREATE_APPDIR, null, applicationDirectory, 1, "Application Directory", null));
		}

		
		
		// process all entries in document removing found files from hashtable
		// that require no change
		int currentFile = 0;
		for (int index = 0; index < nodeList.getLength(); index++)
		{

			Element fileElement = (Element) nodeList.item(index);
			String md5 = fileElement.getAttribute("md5");
			String key = (applicationDirectory + File.separator + fileElement.getAttribute("systemFileName"));
			String name = (fileElement.getAttribute("name"));
			String jarFileName = fileElement.getAttribute("jarFileName");
			Element copyElement = null;
			//skip any files that match a copy command, will deal with the later
			if (statusDocument != null)
			{
				copyElement = (Element) XMLUtils.selectSingleNode(statusDocument, "//copy[@name = '"+jarFileName+"']");
				if (copyElement != null)
				{
					continue;
				}
				
			}
			
			
			// System.out.println("--->"+url.getProtocol()+"<-----");
			URL jarFileURL = null;
			if (url.getProtocol().startsWith("jar"))
			{
				jarFileURL = UpdaterClient.classLoader.getResource(jarFileName);
			}
			else
			{
				jarFileURL = new URL(url.toExternalForm().replaceAll(DESCRIPTOR_DOCUMENT_NAME, "") + "/" + jarFileName);
			}
			// System.out.println(jarFileURL);
			
			long size = 0;
			try
			{
				size = Long.parseLong(fileElement.getAttribute("size"));
			} catch (Exception exception)
			{
				// if there isn't a size attribute, we should ignore things and
				// leave size set to zero
			}

			if (File.separatorChar == '\\')
			{
				key = key.replaceAll("/", "\\\\");
			}

			if (systemFileHastable.containsKey(key))
			{
				// System.out.println("checking need for update on "+key);
				systemFileHastable.remove(key);
				if (filesAreTheSame(key, md5) == true)
				{
					//no action needed
				}
				else
				{
					installActionVector.add(new InstallAction(InstallAction.UPDATE, jarFileURL, key, size, name, md5));
				}
			}
			else
			{
				installActionVector.add(new InstallAction(InstallAction.INSERT, jarFileURL, key, size, name, md5));
			}
			currentFile++;
		}
		Enumeration<File> fileEnumeration = systemFileHastable.elements();
		while (fileEnumeration.hasMoreElements())
		{
			File file = fileEnumeration.nextElement();
			String path = file.getAbsolutePath();
			installActionVector.add(new InstallAction(InstallAction.DELETE, null, path, 1, file.getName(), null));
		}
		setNumberOfActionsToPerform(installActionVector.size());
		// XXX installView.setActions(installActionVector);
	

		return installActionVector;
	}


	
	public void processUpdates() throws FileNotFoundException, IOException
	{
		try
		{
			performInstallActions(installActionVector);
			installActionVector.clear();				

		} catch (Exception e)
		{
			this.exeception = e;
			e.printStackTrace();

		}

	}

	public Exception getException()
	{
		return exeception;
	}

	

	private void performInstallActions(Vector<InstallAction> installActionVector) throws FileNotFoundException, IOException, NoSuchAlgorithmException, InvalidFileException
	{
		
		
		for (InstallAction action : installActionVector)
		{		
			action.downloadAction();
		}
		
		for (InstallAction action : installActionVector)
		{		
			action.installAction();
		}
		
		for (InstallAction action : installActionVector)
		{		
			action.cleanupAction();
		}

	}

	/**
	 * @param systemFile
	 * @param jarFileName
	 * @return boolean
	 * @throws NoSuchAlgorithmException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private boolean filesAreTheSame(String systemFile, String jarFileMD5Sum) throws NoSuchAlgorithmException, FileNotFoundException, IOException
	{
		boolean filesAreTheSame = false;
		MessageDigest messageDigest = MessageDigest.getInstance("MD5");
		InputStream inputStream = new FileInputStream(systemFile);
		String fileMD5Sum = null;
		// read in data from system file to md5 sum
		byte[] inputBuffer = new byte[1024];
		while (true)
		{
			int value = inputStream.read(inputBuffer);
			if (value < 0)
			{
				break;
			}
			messageDigest.update(inputBuffer, 0, value);
		}
		inputStream.close();
		// convert the byte array from the md5 digest to a string in hex
		fileMD5Sum = new BigInteger(1, messageDigest.digest()).toString(16);
		filesAreTheSame = fileMD5Sum.equals(jarFileMD5Sum);
		// System.out.println(fileMD5Sum);
		// System.out.println(jarFileMD5Sum);
		return filesAreTheSame;
	}


	private Hashtable<String, File> findAllFiles(String applicationDirectory, Hashtable<String, File> fileHashtable,List<String> ignoredDirectoryVector)
	{
		if (fileHashtable == null)
		{
			fileHashtable = new Hashtable<String, File>();
		}
		File rootDirectory = new File(applicationDirectory);
		File[] filesInRootDirectory = rootDirectory.listFiles();
		if (filesInRootDirectory != null)
		{
			for (File file : filesInRootDirectory)
			{
				// if it's a directory then recurse into it.
				if (file.isDirectory())
				{
					if (ignoredDirectoryVector.contains(file.getName()))
					{
						System.out.println("ignoring "+file.getName());
						continue;
					}
					findAllFiles(applicationDirectory + File.separator + file.getName(), fileHashtable,ignoredDirectoryVector);
				}
				// else store it in the directory
				else
				{
					// skip log files for deletetion
					if ((file.getName().endsWith(".lck") || file.getName().indexOf(".log") >= 0 || file.getName().endsWith(DESCRIPTOR_DOCUMENT_NAME) || file.getName().matches("dumpfile-\\d*\\.zip")) == false)
					{
						fileHashtable.put(applicationDirectory + File.separator + file.getName(), file);
					}
				}
			}
		}
		return fileHashtable;

	}

	private void writeDescriptorFile(Document descriptorDocument, String applicationDirectory) throws Exception
	{
		File descriptorFile = new File(applicationDirectory + File.separator + DESCRIPTOR_DOCUMENT_NAME);
		if (descriptorFile.exists())
		{
			descriptorFile.delete();
		}
		FileOutputStream fileOutputStream = new FileOutputStream(descriptorFile);
		XMLUtils.dumpNode(descriptorDocument, fileOutputStream);

		fileOutputStream.flush();
		fileOutputStream.close();
	}

	public void changeStatus(String statusText, int currentActionNumber, boolean isDone)
	{
		this.statusText = statusText;
		this.currentActionNumber = currentActionNumber;
		this.isDone = isDone;
	}

	public final void setNumberOfActionsToPerform(int numberOfActionToPerform)
	{
		this.numberOfActionsToPerform = numberOfActionToPerform;
	}

	/**
	 * @return
	 */
	public int getNumberOfActionToPerform()
	{
		return numberOfActionsToPerform;
	}

	/**
	 * @return
	 */
	public int getCurrentActionNumber()
	{
		return currentActionNumber;
	}

	/**
	 * @return
	 */
	public String getStatusText()
	{
		return statusText;
	}

	/**
	 * @return
	 */
	public boolean isDone()
	{
		return isDone;
	}

	public void processScript() throws Exception
	{
		if (statusDocument != null)
		{
			centralServicesClient.processStatusDocument(statusDocument);
		}
		
	}

	
}
