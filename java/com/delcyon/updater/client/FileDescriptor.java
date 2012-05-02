package com.delcyon.updater.client;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FileDescriptor
{

	private String jarFileName = null;
	private String name = null;
	private String systemFileName = null;
	private Element fileDescriptorElement = null;
	private byte[] data = null;
	private String md5 = null;
	private String size = null;

	public FileDescriptor(FileInputStream fileInputStream)
	{
		try
		{

			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(false);
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();	
			Document document = documentBuilder.newDocument();
			fileDescriptorElement = document.createElement("fileDescriptor");
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			byte[] inputBuffer = new byte[1024];
			while (true)
			{
				int value = fileInputStream.read(inputBuffer);
				if (value < 0)
				{
					break;
				}
				buffer.write(inputBuffer, 0, value);
			}
			data = buffer.toByteArray();
			buffer.close();
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.update(data);
			// convert the byte array from the md5 digest to a string in hex
			md5 = new BigInteger(1, messageDigest.digest()).toString(16);
			fileDescriptorElement.setAttribute("md5", md5);
			size = data.length + "";
			fileDescriptorElement.setAttribute("size", size);
		} catch (Exception e)
		{
		}

	}

	public final String getJarFileName()
	{
		return jarFileName;
	}

	/**
	 * This is the name of the file in the distribution archive
	 * 
	 * @param jarFileName
	 */
	public final void setJarFileName(String jarFileName)
	{
		this.jarFileName = jarFileName;
		fileDescriptorElement.setAttribute("jarFileName", jarFileName);
	}

	public final String getName()
	{
		return name;
	}

	public final void setName(String name)
	{
		this.name = name;
		fileDescriptorElement.setAttribute("name", name);
	}

	public final String getSystemFileName()
	{
		return systemFileName;
	}

	public final void setSystemFileName(String systemFileName)
	{
		this.systemFileName = systemFileName;
		fileDescriptorElement.setAttribute("systemFileName", systemFileName);
	}

	public Element getElement()
	{
		return fileDescriptorElement;
	}

	public byte[] getData()
	{
		return data;
	}
}
