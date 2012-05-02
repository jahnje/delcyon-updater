/*
 * Created on Feb 11, 2009
 */
package com.delcyon.updater.client;

import java.util.Hashtable;
import java.util.Vector;

import org.w3c.dom.Element;

/**
 * @author jeremiah
 * @version $Id: $
 */
public class Group extends CSNode
{
    
    public static Group loadGroup(CentralServices centralServices,Element groupElement) throws Exception
    {   
        Group group = new Group(centralServices,groupElement);
        
        return group;
    }
    
    public Group(CSNode parent, Element nodeElement) throws Exception {
		super(parent, nodeElement);		
	}
    
    
    @Override
    protected void nodeElementInit(Element nodeElement) throws Exception {
      
    }

    @Override
    protected CSNode loadChildElement(Element childElement) throws Exception {
        if (childElement.getNodeName().equals("control"))
        {
            return Control.loadControl(this,childElement);
        }        
        return null;
    }
  
    /**
     * @param objectName
     * @return
     */
    public boolean hasControl(String controlName)
    {
        return getChildNodeHashtable().containsKey(controlName);
    }

    /**
     * @param objectName
     * @throws Exception 
     */
    public Control getControl(String controlName) throws Exception
    {
        return (Control) getChildNodeHashtable().get(controlName);
    }

   

    /**
     * @return
     */
    public String getClientIDKey()
    {
        return getVar("varName");
    }


    /**
     * @return
     */
    public Vector<CSNode> getControlVector()
    {
        Vector<CSNode> controlVector = new Vector<CSNode>();
        for (CSNode node : getChildVector())
        {
            if (node instanceof Control)
            {
                controlVector.add(node);
            }
        }
        return controlVector;
    }



   

    /**
     * @param requiredGroups
     * @param clientID
     * @return
     */
    public boolean isMember(String[] requiredGroups, CentralServicesRequest centralServicesRequest)
    {
        
        if (requiredGroups.length == 0)
        {
            return true;
        }
        Hashtable<String, String> requiredMembershipsHashtable = new Hashtable<String, String>();
        for (String requiredMembership : requiredGroups)
        {
            requiredMembershipsHashtable.put(requiredMembership, requiredMembership);
        }
        if (getVar(centralServicesRequest,"memberships").length() > 0)
        {
            String[] memberships = getVar(centralServicesRequest,"memberships").split(",");
            for (String membership : memberships)
            {
                requiredMembershipsHashtable.remove(membership);
            }
            if (requiredMembershipsHashtable.size() == 0)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else 
        {
            return false;
        }        
    }

}







