/*
 * Created on Feb 11, 2009
 */
package com.delcyon.updater.client;



import org.w3c.dom.Element;

/**
 * @author jeremiah
 * @version $Id: $
 */
public class CSFilter extends CSNode
{
      
    public CSFilter(CSNode parent, Element nodeElement) throws Exception {
		super(parent, nodeElement);		
	}
    
    @Override
	protected void nodeElementInit(Element nodeElement) throws Exception {
    	
	}
    
   @Override
   protected CSNode loadChildElement(Element childElement) throws Exception {
	   
		   return null;
	   
   }

   @Override
   public String getVersion() {
       byte[] value = getGetScopedVariable(null);
       if (value.length == 0)
       {
           return super.getVersion();
       }
       return super.getVersion()+""+new String(value);
   }
    

    /**
     * @return
     */
    public String getTrigger()
    {
       return getVar("trigger");
    }
    
    
 
    /**
     * @param centralServicesRequest 
     * @return
     */
    public byte[] getReplacement(CentralServicesRequest centralServicesRequest)
    {
        return getVar(centralServicesRequest, getVar(centralServicesRequest, "valueAttributeName",false),false).getBytes();
         
    }


    /**
     * centralServicesRequest CAN be null
     * @param centralServicesRequest
     * @return
     */
    private byte[] getGetScopedVariable(CentralServicesRequest centralServicesRequest)
    {
        
        String value = getVar(centralServicesRequest, getVar(centralServicesRequest,"varName",false),false);
        
        return value.getBytes();
    }

    
	
}
