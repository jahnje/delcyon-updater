package com.delcyon.updater.client;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public abstract class CSNode {

	private CSNode parent = null;
	private Element nodeElement = null;
	private Hashtable<String, String> localVariables = new Hashtable<String, String>();
	private Hashtable<String, CSNode> childNodeHashtable = new Hashtable<String, CSNode>();
	private Vector<CSNode> childVector = new Vector<CSNode>();
	
	private String name = null;
	private String version;
	
	
	public CSNode(CSNode parent, Element nodeElement) throws Exception {
		this.parent  = parent;
		this.nodeElement  = loadElement(nodeElement);
		init(this.nodeElement);
	}
	
	protected void reloadNode(Element nodeElement) throws Exception
	{
	    if (getImportValue() != null)
	    {
	        nodeElement.setAttribute("import", getImportValue());
	    }
		this.nodeElement  = loadElement(nodeElement);
		init(this.nodeElement);
		CentralServicesClient.logger.log(Level.INFO, "reloaded: "+getName()+" from "+getImportValue());
		
	}
	
	protected Element loadElement(Element initialNodeElement) throws Exception
    {
    	if (initialNodeElement.hasAttribute("import"))
        {
            String importValue = initialNodeElement.getAttribute("import");
           
            initialNodeElement.setAttribute("import", importValue);
        }
        
        return initialNodeElement;
    }
	
	@SuppressWarnings("unchecked")
	private void init(Element nodeElement) throws Exception
	{
		this.version = "";
		MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.reset();
		localVariables.clear();
		
		NamedNodeMap atrributeList = nodeElement.getAttributes();
        for (int index = 0; index < atrributeList.getLength() ; index++)
        {
            
            Attr attribute = (Attr) atrributeList.item(index);
            localVariables.put(attribute.getName(), attribute.getValue());
            messageDigest.update(attribute.getName().getBytes());
            messageDigest.update(attribute.getValue().getBytes());
        }
        name = localVariables.get("name");
        if (childVector == null)
    	{
        	childVector = new Vector<CSNode>();
    	}
    	childVector.clear();
    	if (childNodeHashtable == null)
    	{
    		childNodeHashtable = new Hashtable<String, CSNode>();
    	}
    	childNodeHashtable.clear();
        
    	nodeElementInit(nodeElement);
        
        NodeList childList = nodeElement.getChildNodes();
        
        for (int index = 0; index < childList.getLength(); index++)
        {
            if (childList.item(index).getNodeType() != Node.ELEMENT_NODE)
            {
                continue;
            }
            Element childElement = (Element) childList.item(index);
            CSNode node = null;
            
            node = loadChildElement(childElement);
            
            if (node == null)
            {
            	continue;
            }
            messageDigest.update(node.getVersion().getBytes());
            childVector.add(node);
            if (node.getName() != null)
            {
            	childNodeHashtable.put(node.getName(), node);
            }
        }
        this.version = new BigInteger(1,messageDigest.digest()).toString(16);
           
	}
	
	public String getVersion() {
		return this.version;
	}

	protected abstract void nodeElementInit(Element nodeElement) throws Exception;

	public CSNode getParent()
	{
		return parent;
	}

	public Element getNodeElement() {
		return nodeElement;
	}
	
	public String getName()
	{
		return getVar("name");
	}
	
	/**
     * @param varName
     * @return
     */
    public boolean hasVar(String varName)
    {
        return localVariables.containsKey(varName);
    }
 
    public void setVar(String key, String value)
    {        
    	//CentralServicesClient.logger.log(Level.FINER, "Storing '"+key+"' => '"+value+"'");
    	localVariables.put(key, value);
    }

    /**
     * @param varName
     * @return
     */
    public String getVar(String varName)
    {
        return getVar(null, varName,false);
    }
    
    
    public String getVar(CentralServicesRequest centralServicesRequest, String varName,boolean emptyOK)
    {
        //Thread.dumpStack();
        String value = "";
        
         
       

        if (value.length() == 0)
        {
            //check for var name replacement
            if (varName.matches(".*\\$\\{.*\\}.*"))
            {
                varName = processVariablesInString(centralServicesRequest,varName,emptyOK);
            }



            if (centralServicesRequest != null && centralServicesRequest.hasVar(varName))
            {
                value = centralServicesRequest.getVar(varName);
            }
            else if (localVariables.containsKey(varName))
            {
                value = localVariables.get(varName);
            }
            else {
                CSNode node = this;
                while (node != null)
                {
                    if (node.hasVar(varName))
                    {
                        value = node.getVar(centralServicesRequest,varName,emptyOK);
                        break;
                    }
                    
                    node = node.getParent();
                }            
            }
        }
        if (value.matches(".*\\$\\{.*\\}.*"))
        {
            value = processVariablesInString(centralServicesRequest,value,emptyOK);
        }
        
        return value;
        
    }
	
    
    
    public String processVariablesInString(CentralServicesRequest centralServicesRequest,String name,boolean emptyOK)
    {
        
        String[] variables = getVariableNames(name);
        for (String variableName : variables)
        {
            String replacement = getVar(centralServicesRequest,variableName,emptyOK);
            if (replacement.length() != 0 || emptyOK)
            {
                name = name.replaceAll("\\$\\{"+variableName+"\\}", replacement);
            }            
        }
        return name;
    }
    
    private String[] getVariableNames(String variableString)
    {
        String[] variables = {};
        Vector<String> varNameVector = new Vector<String>();
        String[] split = variableString.split("\\$");
        for (String string : split)
        {
            if (string.startsWith("{"))
            {
                int endIndex = string.indexOf('}');
                String varName = string.substring(1, endIndex);
                varNameVector.add(varName);
            }
        }
        return varNameVector.toArray(variables);
    }
    
    /**
     * @return
     */
    public String getKeyAttributeName()
    {
        return getVar("keyAttributeName");        
    }
    
    
    protected Vector<CSNode> getChildVector() {
		return childVector;
	}
    
    protected Hashtable<String,CSNode> getChildNodeHashtable()
    {
    	return this.childNodeHashtable;
    }
    
	public boolean needsReload() throws Exception {
	  
	  if (hasImportChanged() == true)
	  {
	      clientVersionChanged();
	      CentralServicesClient.logger.log(Level.INFO, getName()+" has changed, and need to be reloaded");
	      return true;
	      //reloadNode(VersionControl.getVersionControl().loadDocument(getImportValue()).getRootElement());
	      //
	  }

	  else
	  {
	      Vector<CSNode> childVector = getChildVector();
	      for (CSNode node : childVector) 
	      {
	          if (node.needsReload() == true)
	          {
	              return true;
	          }
	      }
	  }
	  
	  return false;
		
	}
    
 

	protected boolean hasImportChanged() throws Exception {
		boolean hasImportChanged = false;
	
		return hasImportChanged;
	}
    
    /**
     * This is here so that we can override it in central services, which dosen't use an import attribute
     * @return
     */
    protected String getImportValue()
    {
    	if(nodeElement.hasAttribute("import"))
		{
			return nodeElement.getAttribute("import");
		}
    	else
    	{
    		return null;
    	}
    }

	protected abstract CSNode loadChildElement(Element childElement) throws Exception;

	public void clientVersionChanged() throws Exception {
		if (getParent() != null)
		{
			getParent().clientVersionChanged();
		}
	}

   
    
    
    
}
