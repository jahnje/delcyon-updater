/*
 * Created on Feb 11, 2009
 */
package com.delcyon.updater.client;

import java.io.OutputStream;

import org.w3c.dom.Element;

/**
 * @author jeremiah
 * @version $Id: $
 */
public class Control extends CSNode
{
   
	
    
    public Control(CSNode parent, Element nodeElement) throws Exception {
		super(parent, nodeElement);
	}
    
    
    @Override
    protected void nodeElementInit(Element nodeElement) throws Exception {
    	
    }

    @Override
    protected CSNode loadChildElement(Element childElement) throws Exception {
    	if (childElement.getNodeName().equals("copy"))
    	{
    		return new Copy(this,childElement);
    	}
    	else
    	{
    		return null;
    	}
    }
   

    
    
    /**
     * @param attributeValue
     * @return
     * @throws Exception 
     */
    public static Control loadControl(Group parentGroup, Element controlElement) throws Exception
    {
        return new Control(parentGroup,controlElement);
    }


 
    /**
     * @param centralServicesRequest
     * @param copyNumber
     * @param outputStream
     * @throws Exception 
     */
    public void processRequest(CentralServicesRequest centralServicesRequest, String copyName, OutputStream outputStream) throws Exception
    {
        ((Copy) getChildNodeHashtable().get(copyName)).processRequest(centralServicesRequest, outputStream);
    }

   

    public Copy getCopyForMasterFile(String masterFileName)
    {
        return (Copy) getChildNodeHashtable().get(masterFileName);
    }

    
   
}
