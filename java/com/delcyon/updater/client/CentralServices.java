/*
 * Created on Feb 11, 2009
 */
package com.delcyon.updater.client;

import java.net.URL;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author jeremiah
 * @version $Id: $
 */
public class CentralServices extends CSNode
{
    private URL location;
    /**
     * @param location
     * @throws Exception 
     */
   
    public static CentralServices loadCentralServices(URL location) throws Exception
    {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(location.openStream());
        CentralServices centralServices = new CentralServices(document.getDocumentElement());
        //location is a special case since it's not listed as an import, but need to be checked for changes
        centralServices.location = location;
        return centralServices;
        
    }
   
    public CentralServices(Element nodeElement) throws Exception {
		super(null, nodeElement);
	}
    
    @Override
    protected void nodeElementInit(Element nodeElement) throws Exception {
    	
    	
        //load environment
        Set<Entry<String, String>> envEntrySet = System.getenv().entrySet();
        for (Entry<String, String> entry : envEntrySet)
        {
            setVar(entry.getKey(), entry.getValue());
        }
        
        //load system properties        
        Set<Entry<Object, Object>> propEntrySet = System.getProperties().entrySet();
        for (Entry<Object, Object> entry : propEntrySet)
        {
            setVar(entry.getKey().toString(), entry.getValue().toString());     
        }
        
       
    
    }
    
    @Override
    protected CSNode loadChildElement(Element childElement) throws Exception {
    	return Group.loadGroup(this,childElement);
    }
    
	
   
        
    /**
     * @param groupKey
     * @return
     */
    public boolean hasGroup(String groupKey)
    {
        return getChildNodeHashtable().containsKey(groupKey);
    }
 
    public Group getGroup(String groupKey)
    {
        return (Group) getChildNodeHashtable().get(groupKey);
    }

	
	
    
}
