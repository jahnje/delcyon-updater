/*
 * Created on May 24, 2012
 */
package com.delcyon.updater.client;

import java.util.List;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;

/**
 * @author jeremiah
 * @version $Id: $
 */
public class XPathFunctionResolverImpl implements XPathFunctionResolver
{

    /**
     * 
     */
    public XPathFunctionResolverImpl()
    {
        
    }
    /* (non-Javadoc)
     * @see javax.xml.xpath.XPathFunctionResolver#resolveFunction(javax.xml.namespace.QName, int)
     */
    public XPathFunction resolveFunction(QName functionName, int arity)
    {
        if (functionName.getLocalPart().equals("matches"))
        {
            return new XPathMatchesFunction();
        }
        else if(functionName.getLocalPart().equals("replaceAll"))
        {
            return new XPathReplaceAllFunction();
        }
        
        else return null;
    }

    public class XPathMatchesFunction implements XPathFunction
    {

        /* (non-Javadoc)
         * @see javax.xml.xpath.XPathFunction#evaluate(java.util.List)
         */
        public Object evaluate(List args) throws XPathFunctionException
        {
            String input =  args.get(0).toString();
            String regex = args.get(1).toString();
             
            return Pattern.compile(regex).matcher(input).matches();
        }
        
    }
    
    public class XPathReplaceAllFunction implements XPathFunction
    {

        /* (non-Javadoc)
         * @see javax.xml.xpath.XPathFunction#evaluate(java.util.List)
         */
        public Object evaluate(List args) throws XPathFunctionException
        {
            String input =  args.get(0).toString();
            String regex = args.get(1).toString();
            String replacement = args.get(2).toString();
             
            return input.replaceAll(regex, replacement);
        }
        
    }
}
