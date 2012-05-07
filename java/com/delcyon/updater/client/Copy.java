/*
 * Created on Feb 12, 2009
 */
package com.delcyon.updater.client;

import java.io.OutputStream;
import java.util.logging.Level;

import org.w3c.dom.Element;


/**
 * @author jeremiah
 * @version $Id: $
 */
public class Copy extends CSNode
{
    
   
    	
    public Copy(CSNode parent, Element nodeElement) throws Exception {
		super(parent, nodeElement);
	}
    
    @Override
    protected void nodeElementInit(Element nodeElement) throws Exception {
       
    }
    
    @Override
    protected CSNode loadChildElement(Element childElement) throws Exception
    {
        CSNode node = null;
        if (childElement.getNodeName().equals("filter"))
        {
            node = new CSFilter(this,childElement);
        }

    	
    	return node;
    }
   
    
    /**
     * @param centralServicesRequest
     * @param outputStream
     * @throws Exception 
     */
    public String processRequest(CentralServicesRequest centralServicesRequest, OutputStream outputStream) throws Exception
    {
    	if (getNodeElement().hasAttribute("if"))    	
    	{
    		String xpath = processVariablesInString(null, getNodeElement().getAttribute("if"),true);    		
    		boolean result = XMLUtils.evaluateXPath(getNodeElement(), xpath);
    		CentralServicesClient.logger.log(Level.INFO, "evaluating "+xpath+" "+result);
    		if (result == false)
    		{
    			outputStream.close();
    			return null;    	            
    		}
    	}
    	return VersionControl.getVersionControl().readClientVersionStreamIntoOutputStream(getVar(centralServicesRequest, "name",false), outputStream, centralServicesRequest, getChildVector());    
    }

    public String getVersion(CentralServicesRequest centralServicesRequest) throws Exception
    {
       
        //return VersionControl.getVersionControl().getClientVersion(getVar(centralServicesRequest, "name"), centralServicesRequest.getClientID());    
          return null; 
    }
    
    /**
     * @param value
     * @return
     */
    public String getSourceName(CentralServicesRequest centralServicesRequest)
    {
        return getVar(centralServicesRequest, "name",false);
    }
    
    public String getDestinationName(CentralServicesRequest centralServicesRequest)
    {
        return getVar(centralServicesRequest, "dest",false);
    }
    
   
    /**
     * @throws Exception 
     * 
     */
//    public void clientVersionChanged() throws Exception
//    {          	
//    	VersionControl.getVersionControl().reversionFile(getName());
//    	super.clientVersionChanged();
//    }
//    
    
}
