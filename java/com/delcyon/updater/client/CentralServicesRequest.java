/*
 * Created on Feb 11, 2009
 */
package com.delcyon.updater.client;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author jeremiah
 * @version $Id: $
 */
public class CentralServicesRequest
{

    public enum RequestType
    {
        COPY,        
        STATUS_CHECK,
        REPORT;
    }

    private String clientID;
    private Hashtable<String, String> localVariables = new Hashtable<String, String>();
    private Group group;
    private RequestType requestType = null;   
    private String controlName = null;
    private String copyName = null;
    private Element clientElement;
    private String tableName;
    private String tableKeyAttributeName;
    private Element entryElement;
    private CentralServices centralService;

    public static CentralServicesRequest loadClient(Element agentElement,CentralServices centralServices) throws Exception
    {

        return new CentralServicesRequest(agentElement,centralServices);

    }

    /**
     * @param clientElement
     * @throws Exception
     */
    public CentralServicesRequest(Element clientElement,CentralServices centralServices) throws Exception
    {
        this.clientElement = clientElement;
        this.centralService = centralServices;
        localVariables.clear();
        NodeList variableList = XMLUtils.selectNodes(clientElement,"//variable");
        group = null;
        for (int index =0; index < variableList.getLength(); index++)
        {
            Element variableElement = (Element) variableList.item(index);
            localVariables.put(variableElement.getAttribute("name"), variableElement.getAttribute("value"));
            if (centralServices.hasGroup(variableElement.getAttribute("value")))
            {
                group = centralServices.getGroup(variableElement.getAttribute("value"));
            }
        }
        if (group == null)
        {
            group = centralServices.getGroup("default");
        }
        if (group.getClientIDKey() != null)
        {
            this.clientID = localVariables.get(group.getClientIDKey());
        }
        else
        {
            this.clientID = localVariables.get("HOSTNAME");
        }

        if (clientElement.getAttribute("RequestType") != null)
        {
            this.requestType = RequestType.valueOf(clientElement.getAttribute("RequestType"));
        }        
        if (clientElement.getAttribute("controlName") != null)
        {
            this.controlName = clientElement.getAttribute("controlName");
        }
        if (clientElement.getAttribute("copyName") != null)
        {
            this.copyName  = clientElement.getAttribute("copyName");
        }
        
        
    }

   

    /**
     * @param outputStream
     * @throws Exception 
     */
    public Document processRequest(OutputStream outputStream) throws Exception
    {
        switch (requestType)
        {
            case COPY:
            	CentralServicesClient.logger.log(Level.INFO, "Processing Copy request for "+copyName+" from "+getClientID());
                processObjectRequest(controlName, copyName, outputStream);    
                return null;

            case STATUS_CHECK:
            	CentralServicesClient.logger.log(Level.INFO, "Processing Status Check request for "+getClientID());
                return processStatusCheck();                
           
        }
        return null;
        
    }
    
    /**
     * 
     */
    

    /**
     * @param outputStream
     * @throws Exception 
     */
    public Document processStatusCheck() throws Exception
    {
        
        
        Document statusDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        
        Element statusElement = statusDocument.createElement("status");
        statusDocument.appendChild(statusElement);
        
        Vector<CSNode> controlVector = group.getControlVector();
        for (CSNode node : controlVector)
        {
        	Control control = (Control) node;
            Element newControlElement = statusDocument.createElement("control");
            newControlElement.setAttribute("name", control.getName());
            Element controlElement = control.getNodeElement();
            NodeList controlElementChildren = controlElement.getChildNodes();
            for (int index = 0; index < controlElementChildren.getLength(); index++)
            {   
                if (controlElementChildren.item(index).getNodeType() != Node.ELEMENT_NODE)
                {
                    continue;
                }
                Element child = (Element) controlElementChildren.item(index);
                //find out if child has a group, and if the request matches the childs group
                if (child.hasAttribute("requiredGroups") == true)
                {
                    String[] requiredGroups = child.getAttribute("requiredGroups").split(",");
                    if (group.isMember(requiredGroups,this) == false)
                    {
                        continue;
                    }
                }   
                //END Grouping check
                Element newChildElement = statusDocument.createElement(child.getNodeName());
                NamedNodeMap attributeList = child.getAttributes();
                for (int attrIndex = 0; attrIndex < attributeList.getLength(); attrIndex++)
                {
                    Attr attribute = (Attr) attributeList.item(attrIndex);
                    String attributeValue = attribute.getValue();
                    if (attributeValue.matches(".*\\$\\{.*\\}.*"))
                    {
                        attributeValue = control.processVariablesInString(this,attributeValue,false);
                    }
                    newChildElement.setAttribute(attribute.getName(),attribute.getValue());
                    if (attribute.getName().equals("name"))
                    {
                    	String md5 = null;

                    	//need to get to the copy from here
                    	Copy copy = control.getCopyForMasterFile(attribute.getValue());
                    	if (copy != null)
                    	{                                
                    	    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    	    md5 = copy.processRequest(this, byteArrayOutputStream);                                                                                                
                    	}

                    	if (md5 != null)
                        {
                            newChildElement.setAttribute("md5",md5);
                        }
//                        else
//                        {
//                            System.out.println("here");
//                        }
                    }
                    else if (attribute.getName().equals("dest") )
                    {
//                        if (child.getAttribute("name").indexOf("$") >= 0)
//                        {
//                            System.out.println("dealing with var");
//                        }
                        Copy copy = control.getCopyForMasterFile(child.getAttribute("name"));
                        if (copy != null)
                        {
                            newChildElement.setAttribute(attribute.getName(),control.processVariablesInString(this,copy.getDestinationName(this),false));    
                        }
//                        else
//                        {
//                            System.out.println("here");
//                        }
                        
                    }
                }
                newControlElement.appendChild(newChildElement);
            }
            statusElement.appendChild(newControlElement);
        }
        
        XMLUtils.dumpNode(statusDocument, System.out);
        return statusDocument;
        //outputter.output(statusDocument,System.out);
    }

    /**
     * @param OName
     *            of file
     * @param outputStream
     *            of servlet request
     * @throws Exception
     */
    public void processObjectRequest(String controlName, String copyName, OutputStream outputStream) throws Exception
    {

        if (group.hasControl(controlName) == true)
        {
            Control control = group.getControl(controlName);
            control.processRequest(this, copyName, outputStream);
        }

    }

  
    /**
     * @return the clientID
     */
    public String getClientID()
    {
        return clientID;
    }

    /**
     * @param varName
     * @return
     */
    public boolean hasVar(String varName)
    {
        return localVariables.containsKey(varName);
    }

    /**
     * @param varName
     * @return
     */
    public String getVar(String varName)
    {
        return localVariables.get(varName);
    }

   

    /**
     * @param name
     * @param keyAttributeName
     */
    public void setClientVersionInformation(String name, String keyAttributeName, Element entryElement)
    {
        this.tableName = name;
        this.tableKeyAttributeName = keyAttributeName;
        this.entryElement = entryElement;
    }

    /**
     * @return the tableName   
     */
    public String getTableName()
    {
        return tableName;
    }

    /**
     * @return the tableKeyAttributeName    
     */
    public String getTableKeyAttributeName()
    {
        return tableKeyAttributeName;
    }

    /**
     * @return
     * @throws Exception 
     */
    public String getTableEntryMD5() throws Exception
    {
        String md5 = getEntryMD5(entryElement);
        if (md5 == null)
        {
            return "";
        }
        else
        {
            return md5;
        }
        
    }

    private String getEntryMD5(Element entryElement) throws Exception
    {
        if (entryElement == null)
        {
            return null;
        }
        
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.reset();
        NamedNodeMap atrributeList = entryElement.getAttributes();
        for (int index = 0; index < atrributeList.getLength(); index++)
        {
            Attr attribute = (Attr) atrributeList.item(index);           
            messageDigest.update(attribute.getName().getBytes());
            messageDigest.update(attribute.getValue().getBytes());
        }
        return new BigInteger(1,messageDigest.digest()).toString(16);
    }
    
   
}
