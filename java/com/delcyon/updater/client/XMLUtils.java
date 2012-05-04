/**
Copyright (C) 2012  Delcyon, Inc.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.delcyon.updater.client;

import java.io.OutputStream;
import java.net.URL;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author jeremiah
 */
public class XMLUtils
{

    public static Node selectSingleNode(Node node, String path) throws Exception
    {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        javax.xml.xpath.XPath xPath = xPathFactory.newXPath();

        XPathExpression xPathExpression = xPath.compile(path);
        return (Node) xPathExpression.evaluate(node, XPathConstants.NODE);
    }

    public static boolean evaluateXPath(Node node, String path) throws Exception
    {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        javax.xml.xpath.XPath xPath = xPathFactory.newXPath();

        XPathExpression xPathExpression = xPath.compile(path);
        Boolean result = (Boolean) xPathExpression.evaluate(node, XPathConstants.BOOLEAN);
        return result; 
    }
    
    public static void removeNamespaceDeclarations(Node node, String... namespaces)
    {
        NamedNodeMap namedNodeMap = ((Element) node).getAttributes();
        for (int nameIndex = 0; nameIndex < namedNodeMap.getLength(); nameIndex++)
        {
            Node namedNode = namedNodeMap.item(nameIndex);
            String uri = namedNode.getNamespaceURI();
            String localName = namedNode.getLocalName();
            if (uri != null && uri.equals("http://www.w3.org/2000/xmlns/"))
            {
                for (String removeableNamespace : namespaces)
                {
                    if (namedNode.getNodeValue().equals(removeableNamespace))
                    {
                        ((Element) node).removeAttributeNS("http://www.w3.org/2000/xmlns/", localName);
                        nameIndex--;
                    }
                }
            }
        }
    }

    public static NodeList selectNodes(Node node, String path) throws Exception
    {

        XPathFactory xPathFactory = XPathFactory.newInstance();
        javax.xml.xpath.XPath xPath = xPathFactory.newXPath();
        XPathExpression xPathExpression = xPath.compile(path);
        return (NodeList) xPathExpression.evaluate(node, XPathConstants.NODESET);
    }

    public static String selectSingleNodeValue(Element node, String path) throws Exception
    {

        XPathFactory xPathFactory = XPathFactory.newInstance();
        javax.xml.xpath.XPath xPath = xPathFactory.newXPath();

        XPathExpression xPathExpression = xPath.compile(path);
        return xPathExpression.evaluate(node);

    }

    public static String getXPath(Node node) throws Exception
    {
        // example
        // /Capo/group[1]/choose[1]/when[1]
        return getPathToRoot(node);
    }

    private static String getPathToRoot(Node node) throws Exception
    {

        String name = node.getNodeName();
        if (node instanceof Element)
        {
            String nameAttributeValue = ((Element) node).getAttribute("name");
            if (nameAttributeValue.isEmpty() == true)
            {
                String position = selectSingleNodeValue((Element) node, "count(preceding-sibling::" + name + ")+1");
                name += "[" + position + "]";
            }
            else
            {
                name += "[@name = '" + nameAttributeValue + "']";
            }
        }
        if (node.getParentNode() != null && node.getParentNode().getNodeName() != null && node.getParentNode() instanceof Element)
        {

            return getPathToRoot(node.getParentNode()) + "/" + name;
        }
        else
        {
            return "/" + name;
        }

    }

    public static void dumpNode(Node node, OutputStream outputStream) throws Exception
    {

        Transformer transformer = null;
        URL transformURL = XMLUtils.class.getClassLoader().getResource("identity_transform.xsl");
        if (transformURL != null)
        {
            transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(transformURL.openStream()));

        }
        else
        {
            transformer = TransformerFactory.newInstance().newTransformer();
        }
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        transformer.transform(new DOMSource(node), new StreamResult(outputStream));

    }

    /**
     * @param node
     * @param xpath
     * @return list of nodes removed
     * @throws Exception
     */
    public static NodeList removeNodes(Node node, String xpath) throws Exception
    {
        NodeList nodeList = selectNodes(node, xpath);
        for (int nodeListIndex = 0; nodeListIndex < nodeList.getLength(); nodeListIndex++)
        {
            Node removeableNode = nodeList.item(nodeListIndex);
            Node parentNode = removeableNode.getParentNode();
            if (parentNode != null)
            {
                parentNode.removeChild(removeableNode);
            }
        }
        return nodeList;
    }

    public static void removeContent(Element element)
    {
        NodeList nodeList = element.getChildNodes();
        while (nodeList.getLength() > 0)
        {
            element.removeChild(nodeList.item(0));
        }

    }

    public static void removeAttributes(Element element)
    {
        NamedNodeMap namedNodeMap = element.getAttributes();
        while (namedNodeMap.getLength() > 0)
        {
            element.removeAttributeNode((Attr) namedNodeMap.item(0));
        }
    }
}
