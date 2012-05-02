package com.delcyon.updater.client;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;


public class CallURLBuildTask extends Task {

	private String methodType = "GET";
	private String URL = "";
	private List<Parameter> parameterList = new ArrayList<Parameter>(); 
	
	public void execute() throws BuildException {
		log("Calling URL: "+URL);
		try {
			String tempURL = URL;
			if (parameterList.size() != 0){
				tempURL +="?";
				for (Parameter parameter : parameterList) {
					tempURL+= ""+parameter.getName()+"="+parameter.getValue()+"&";
				}
			}
			URL url = new URL(tempURL); 
			HttpURLConnection processorConnection = (HttpURLConnection)url.openConnection();
			processorConnection.setDoOutput(true);
			processorConnection.setDoInput(false);
			processorConnection.setRequestMethod("POST");
			processorConnection.getOutputStream();
			processorConnection.disconnect();

		} catch (Exception e) {
			throw new BuildException(e);
		}
	}
	
	
	
	
	/**
	 * @return the methodType
	 @Column @ToStringInclude @XmlAttribute @Equal @Validate(errorName = "")
	 */
	public String getMethodType() {
		return methodType;
	}



	
	/**
	 * @param methodType the methodType to set
	 */
	public void setMethodType(String methodType) {		
		this.methodType = methodType;
	}



	
	/**
	 * @return the uRL
	 @Column @ToStringInclude @XmlAttribute @Equal @Validate(errorName = "")
	 */
	public String getURL() {
		return URL;
	}



	
	/**
	 * @param url the uRL to set
	 */
	public void setURL(String url) {	
		URL = url;
	}



	public void add(Parameter parameter){
		parameterList.add(parameter);
	}
	
	public static class Parameter {
		private String name = "";
		private String value = "";
		
		/**
		 * @return the name
		 @Column @ToStringInclude @XmlAttribute @Equal @Validate(errorName = "")
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * @param name the name to set
		 */
		public void setName(String name) {		
			this.name = name;
		}
		
		/**
		 * @return the value
		 @Column @ToStringInclude @XmlAttribute @Equal @Validate(errorName = "")
		 */
		public String getValue() {
			return value;
		}
		
		/**
		 * @param value the value to set
		 */
		public void setValue(String value) {
			this.value = value;
		}
		
		
	}
	
}
