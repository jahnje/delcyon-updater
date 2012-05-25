package com.delcyon.updater.client;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class InstallModel
{

    public static final String DESCRIPTOR_DOCUMENT_NAME = "descriptor.xml";
    public static final boolean isWindows = System.getProperty("os.name", "").trim().startsWith("Windows");

    public static String filesPath = "/applicationDescriptor/copy";

    private URL url;
    private Document descriptorDocument;
    private String applicationDirectory;

    private Exception exeception;
    private DocumentBuilder documentBuilder;
    private Document installDocument;   
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
                setApplicationDirectory(descriptorDocument.getDocumentElement().getAttribute("applicationDirectory"));
                if (descriptorDocument.getDocumentElement().hasAttribute("installDocumentPath"))
                {
                    loadInstallDocument(descriptorDocument.getDocumentElement().getAttribute("installDocumentPath"));
                }
            }
        }
        catch (IOException e)
        {
            System.out.println(url);
            throw e;
        }
        return descriptorDocument;
    }

    public void checkForUpdates() throws Exception
    {

        getDescriptorDocument();
        //XMLUtils.dumpNode(descriptorDocument, System.out);
        if (installDocument != null)
        {
            Element copyOtherFilesElement = (Element) XMLUtils.selectSingleNode(installDocument, "//CopyOtherFiles");
            if (copyOtherFilesElement != null)
            {

                NodeList fileDecriptorNodes = XMLUtils.selectNodes(descriptorDocument, filesPath);
                for (int index = 0; index < fileDecriptorNodes.getLength(); index++)
                {

                    Element fileDescriptorElement = (Element) fileDecriptorNodes.item(index);
                    Element copyElement = copyOtherFilesElement.getOwnerDocument().createElement("copy");
                    copyElement.setAttribute("dest", XMLUtils.evaluateXPathString(fileDescriptorElement, copyOtherFilesElement.getAttribute("dest")));
                    copyElement.setAttribute("src", fileDescriptorElement.getAttribute("src"));
                    copyElement.setAttribute("md5", fileDescriptorElement.getAttribute("md5"));
                    if (XMLUtils.selectSingleNode(installDocument, "//copy[@src = '" + fileDescriptorElement.getAttribute("src") + "']") != null)
                    {
                        continue;
                    }
                    copyOtherFilesElement.getParentNode().insertBefore(copyElement, copyOtherFilesElement);
                }
                copyOtherFilesElement.getParentNode().removeChild(copyOtherFilesElement);
            }
            centralServicesClient = new CentralServicesClient();
            
            centralServicesClient.setVar("applicationDirectory", descriptorDocument.getDocumentElement().getAttribute("applicationDirectory"));
            //XMLUtils.dumpNode(installDocument, System.out);
            centralServicesClient.processStatusDocument(installDocument);
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
                System.out.println("loading install document: "+documentPath);
                URL descriptorURL = UpdaterClient.classLoader.getResource(documentPath);
                installDocument = documentBuilder.parse(descriptorURL.openStream());

            }
        }
        catch (IOException e)
        {
            System.out.println(url);
            throw e;
        }

    }

    public Exception getException()
    {
        return exeception;
    }

}
