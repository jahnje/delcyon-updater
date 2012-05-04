/*
 * Created on Feb 17, 2009
 */
package com.delcyon.updater.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.delcyon.updater.client.LeveledConsoleHandler.Output;





/**
 * @author jeremiah
 * @version $Id: $
 */

public class CentralServicesClient
{

    public static Level LOGGING_LEVEL = Level.ALL;
    public static Logger logger = null;
    private static LeveledConsoleHandler leveledConsoleHandler;
    private static FileHandler fileHandler;
    private static String logFileName = null;   
    
    Hashtable<String, String> processingPropertiesHashtable = new Hashtable<String, String>(){
        /* (non-Javadoc)
         * @see java.util.Hashtable#put(java.lang.Object, java.lang.Object)
         */
        @Override
        public synchronized String put(String key, String value)
        {
            System.out.println("putting "+key+" ==> "+value);
            return super.put(key, value);
        }
    };
    private CentralServices centralServices;
    
    /**
     * @throws Exception 
     * 
     */
    public CentralServicesClient() throws Exception
    {
        logger = Logger.getLogger(this.getClass().getName());
        logger.setLevel(LOGGING_LEVEL);
        if (leveledConsoleHandler == null)
        {
            leveledConsoleHandler = new LeveledConsoleHandler();
            leveledConsoleHandler.setLevel(LOGGING_LEVEL);
            leveledConsoleHandler.setOutputForLevel(Output.STDERR, Level.FINER);
            logger.setUseParentHandlers(false);
            logger.addHandler(leveledConsoleHandler);           
        }
        logger.log(Level.INFO, "Starting Installer ");

        if (fileHandler == null)
        {
            logFileName = "install.log";
            logger.log(Level.FINE, "Opening LogElement File:" + logFileName);
            fileHandler = new FileHandler(logFileName);
            fileHandler.setLevel(LOGGING_LEVEL);
            logger.addHandler(fileHandler);
        }
    }
    
    /* (non-Javadoc)
     * @see com.delcyon.cs.application.Application#getApplicationName()
     */
    public String getApplicationName()
    {
        return "Central Services Client";
    }
    
    /* (non-Javadoc)
     * @see com.delcyon.cs.application.Application#loadConfiguration()
     */
    
    @Test
    public void testProcessStatusDocument() throws Exception
    {
        
        centralServices = CentralServices.loadCentralServices(new File("test_resources/test_install.xml").toURL());        
        Document requestDocument = initDocument();
        requestDocument.getDocumentElement().setAttribute("RequestType", "STATUS_CHECK");
        CentralServicesRequest centralServicesRequest = new CentralServicesRequest(requestDocument.getDocumentElement(),centralServices);
        
        Document statusDocument = centralServicesRequest.processStatusCheck();
        processStatusDocument(statusDocument);
    }
   
    

    
    public void runStatusCheck() throws Exception
    {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document statusDocument = documentBuilder.parse(new ByteArrayInputStream(new byte[]{}));
        
        
        processStatusDocument(statusDocument);
        CentralServicesClient.logger.log(Level.INFO, "Finished Run. Exiting...");
    }

    /**
     * @param statusDocument
     * @throws Exception 
     */
    private void processStatusDocument(Document statusDocument) throws Exception
    {
        NodeList controlElementList = statusDocument.getDocumentElement().getChildNodes();
        for (int index = 0; index < controlElementList.getLength(); index++)
        {
            if (controlElementList.item(index).getNodeType() != Node.ELEMENT_NODE)
            {
                continue;
            }
            Element controlElement = (Element) controlElementList.item(index);
            processControlElement(controlElement);
        }
        
    }

    public enum ControlType
    {
        copy,
        file,
        shellcommand,
        ask,
        pref;
    }

    /**
     * @param controlElement
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
    private void processControlElement(Element controlElement) throws Exception
    {
        CentralServicesClient.logger.log(Level.FINE, "Processing Control Element "+controlElement.getAttribute("name"));
        NodeList childList = controlElement.getChildNodes();
        for (int index = 0; index < childList.getLength(); index++)
        {
            if (childList.item(index).getNodeType() != Node.ELEMENT_NODE)
            {
                continue;
            }
            Element childElement = (Element) childList.item(index);
            ControlType controlType = ControlType.valueOf(childElement.getNodeName());
            switch (controlType)
            {
                case copy:
                    processCopyElement(controlElement.getAttribute("name"),childElement);
                    break;
                case file:
                    break;
                case shellcommand:
                    processShellCommand(childElement);
                    break;                
                default:
                    break;
            }
        }
    }

    private void processShellCommand(Element childElement) throws Exception {
        String ifProperty = childElement.getAttribute("if");
        if (childElement.hasAttribute("if"))
        {
            if(processingPropertiesHashtable.containsKey(ifProperty))
            {
                if (processingPropertiesHashtable.get(ifProperty).equals("true") == false)
                {
                    if (childElement.getAttribute("onSkip") != null)
                    {
                        processingPropertiesHashtable.put(childElement.getAttribute("onSkip"),"true");
                    }
                    CentralServicesClient.logger.log(Level.FINE, "Skipping "+ifProperty+" not == true");
                    return ;
                }
            }
            else
            {
                CentralServicesClient.logger.log(Level.FINE, "Skipping "+ifProperty+" not == true");
                if (childElement.getAttribute("onSkip") != null)
                {
                    processingPropertiesHashtable.put(childElement.getAttribute("onSkip"),"true");
                }
                return ;
            }
        }
        File workingDir = null;
        if (childElement.hasAttribute("workingDir") == true)
        {
            workingDir = new File(childElement.getAttribute("workingDir"));
        }
        
        runCommand(childElement.getAttribute("exec"), workingDir);
        if (childElement.hasAttribute("onAction") == true)
        {
            processingPropertiesHashtable.put(childElement.getAttribute("onAction"),"true");
        }
    }

    
        
    
    
    
    private String runCommand(String command, File workingDir)throws Exception
    {
        String output = null;
        CentralServicesClient.logger.log(Level.INFO, "Running "+command);
        String[] commandArray = {"/bin/sh","-c",command};
        Process process = Runtime.getRuntime().exec(commandArray,null,workingDir);
        
        ByteArrayOutputStream errorByteArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream outputByteArrayOutputStream = new ByteArrayOutputStream();
        
        int errorValue = 0;
        int outputValue = 0;
        
        InputStream errorInputStream = process.getErrorStream();
        InputStream outputInputStream = process.getInputStream();
        
        while (outputValue >= 0 || errorValue >= 0)
        {
            
            if (outputValue >= 0)
            {
                if (outputValue != 0)
                {
                    outputByteArrayOutputStream.write(outputValue);
                }
                outputValue = outputInputStream.read();
            }
            if (errorValue >= 0)
            {
                if (errorValue != 0)
                {
                    errorByteArrayOutputStream.write(errorValue);
                }
                errorValue = errorInputStream.read();                
            }
        }
        
        String warningMessage = new String(errorByteArrayOutputStream.toByteArray());
        if (warningMessage != null && warningMessage.trim().length() != 0)
        {
            CentralServicesClient.logger.log(Level.WARNING, command+":\n"+warningMessage.trim());
        }
        String infoMessage = new String(outputByteArrayOutputStream.toByteArray());
        if (infoMessage != null && infoMessage.trim().length() != 0)
        {
            CentralServicesClient.logger.log(Level.INFO, infoMessage.trim());
            output = infoMessage.trim();
        }
        
        return output;
    }
    
    /**
     * @param childElement
     */
    private boolean processCopyElement(String controlName,Element childElement)
    {
        try
        {
            String masterFileName = childElement.getAttribute("name");
            String destinationFileName = childElement.getAttribute("dest");
            String ifProperty = childElement.getAttribute("if");
            if (childElement.hasAttribute("if"))
            {
                if(processingPropertiesHashtable.containsKey(ifProperty))
                {
                    if (processingPropertiesHashtable.get(ifProperty).equals("true") == false)
                    {
                        if (childElement.hasAttribute("onSkip"))
                        {
                            processingPropertiesHashtable.put(childElement.getAttribute("onSkip"),"true");
                        }
                        CentralServicesClient.logger.log(Level.FINE, "Skipping "+ifProperty+" not == true");
                        return false;
                    }
                }
                else
                {
                    CentralServicesClient.logger.log(Level.FINE, "Skipping "+ifProperty+" not == true");
                    if (childElement.hasAttribute("onSkip"))
                    {
                        processingPropertiesHashtable.put(childElement.getAttribute("onSkip"),"true");
                    }
                    return false;    
                }
            }
            CentralServicesClient.logger.log(Level.FINE, "Processing Copy Element "+masterFileName+" ==> "+destinationFileName);
            File file = new File(destinationFileName);
            if (file.exists() == false)
            {
                String path = file.getCanonicalPath();
                path = path.substring(0, path.lastIndexOf(File.separator+file.getName()));
                File dirs = new File(path);
                if (dirs.exists() == false)
                {
                    CentralServicesClient.logger.log(Level.INFO, "Creating Directory: "+dirs.getCanonicalPath());
                    dirs.mkdirs();
                }
                file.createNewFile();
                
            }
            String md5 = FileUtility.getMD5ForFile(childElement.getAttribute("dest"));
            if (md5 == null || childElement.getAttribute("md5").equals(md5) == false)
            {
                Document requestDocument = initDocument();
                requestDocument.getDocumentElement().setAttribute("RequestType", "COPY");
                requestDocument.getDocumentElement().setAttribute("controlName", controlName);
                requestDocument.getDocumentElement().setAttribute("copyName", masterFileName);
                
                CentralServicesRequest client = CentralServicesRequest.loadClient(requestDocument.getDocumentElement(),centralServices);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                if (client != null)
                {
                    client.processRequest(byteArrayOutputStream);
                }
                //System.out.println(new String(byteArrayOutputStream.toByteArray()));
                
                processCopyOutput(destinationFileName, new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
                
                if (childElement.getAttribute("chmod") != null)
                {
                    runCommand("/bin/chmod "+childElement.getAttribute("chmod")+" "+destinationFileName, null);
                }
                if (childElement.getAttribute("chown") != null)
                {
                    runCommand("/bin/chown "+childElement.getAttribute("chown")+" "+destinationFileName, null);
                }
                if (childElement.getAttribute("onAction") != null)
                {
                    processingPropertiesHashtable.put(childElement.getAttribute("onAction"),"true");
                }
                return true;
            }
            else
            {
                CentralServicesClient.logger.log(Level.FINE, "File already up to date, skipping.");
                if (childElement.getAttribute("onSkip") != null)
                {
                    processingPropertiesHashtable.put(childElement.getAttribute("onSkip"),"true");
                }
                return false;
            }
        }
        catch (Exception e)
        {
            
            e.printStackTrace();
            if (childElement.getAttribute("onError") != null)
            {
                processingPropertiesHashtable.put(childElement.getAttribute("onError"),"true");
            }
        }
        return false;
    }
   
    private void processCopyOutput(String fileName, InputStream inputStream) throws Exception
    {
        CentralServicesClient.logger.log(Level.INFO, "Copying "+fileName);
        FileOutputStream fileOutputStream = new FileOutputStream(fileName, false);
        readStreamIntoOutputStream(inputStream, fileOutputStream);
        inputStream.close();
    }

    private void readStreamIntoOutputStream(InputStream inputStream, OutputStream outputStream) throws Exception
    {
        byte[] buffer = new byte[1024];
        int bytesRead = 0;
        while (bytesRead >= 0)
        {
            bytesRead = inputStream.read(buffer);
            if (bytesRead == -1)
            {
                break;
            }
            outputStream.write(buffer,0,bytesRead);
        }
        outputStream.flush();
    }

    private Document initDocument() throws Exception
    {
        
        
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element rootElement = document.createElement("agent");
        document.appendChild(rootElement);

        // load environment
        Set<Entry<String, String>> envEntrySet = System.getenv().entrySet();
        for (Entry<String, String> entry : envEntrySet)
        {
            Element variableElement = document.createElement("variable");
            variableElement.setAttribute("name", entry.getKey());
            variableElement.setAttribute("value", entry.getValue());
            rootElement.appendChild(variableElement);
            CentralServicesClient.logger.log(Level.FINE, "Storing "+variableElement.getAttribute("name")+" ==> "+variableElement.getAttribute("value"));
        }

        // load system properties
        Set<Entry<Object, Object>> propEntrySet = System.getProperties().entrySet();
        for (Entry<Object, Object> entry : propEntrySet)
        {
            Element variableElement = document.createElement("variable");
            variableElement.setAttribute("name", entry.getKey().toString());
            variableElement.setAttribute("value", entry.getValue().toString());
            rootElement.appendChild(variableElement);
            CentralServicesClient.logger.log(Level.FINE, "Storing "+variableElement.getAttribute("name")+" ==> "+variableElement.getAttribute("value"));
        }
        
        
        return document;
    }

   

}
