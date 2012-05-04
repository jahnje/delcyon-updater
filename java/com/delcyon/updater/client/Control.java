/*
 * Created on Feb 11, 2009
 */
package com.delcyon.updater.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.prefs.Preferences;

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
    	else if (childElement.getNodeName().equals("pref"))
    	{
    	    
    	    String path = processVariablesInString(null,childElement.getAttribute("path"),true);
    	    Preferences preferences =  Preferences.systemRoot().node(path);
    	    
    	    if (childElement.hasAttribute("set"))
    	    {
    	        String preferenceName =  processVariablesInString(null,childElement.getAttribute("set"),true);
    	        String varName = processVariablesInString(null,childElement.getAttribute("var"),true);
    	        if (childElement.hasAttribute("var") == false)
                {
                    varName = preferenceName;
                }
    	        preferences.put(preferenceName, getVar(varName));
    	        preferences.sync();
    	    }
    	   
    	    if (childElement.hasAttribute("get"))
            {
                String preferenceName =  processVariablesInString(null,childElement.getAttribute("get"),true);
                String property = processVariablesInString(null,childElement.getAttribute("var"),true);
                if (childElement.hasAttribute("var") == false)
                {
                    property = preferenceName;
                }
                setVar(property, preferences.get(preferenceName, ""));
            }
    	    
    	    return null;
    	}
    	else if (childElement.getNodeName().equals("ask"))
    	{
    	    if (childElement.hasAttribute("if"))
    	    {
    	        String xpath = processVariablesInString(null, childElement.getAttribute("if"),true);
    	        CentralServicesClient.logger.log(Level.FINE, "evaluating "+xpath);
    	        boolean result = XMLUtils.evaluateXPath(childElement, xpath);
    	        if (result == false)
    	        {
    	            return null;    	            
    	        }
    	    }
    		System.out.println(childElement.getAttribute("message"));
    		if(childElement.hasAttribute("default"))
    		{
    		    String defaultValue = processVariablesInString(null,childElement.getAttribute("default"),false);
    		    if (defaultValue.isEmpty() == false)
    		    {
    		        setVar(childElement.getAttribute("var"), defaultValue);
    		        System.out.println("Default ["+defaultValue+"]");
    		    }
    		    
    		}
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String value = br.readLine();
            if (value.isEmpty() == false)
            {
                setVar(childElement.getAttribute("var"), value);
            }
            
    		return null;
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
