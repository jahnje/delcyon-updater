package com.delcyon.updater.client;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.tools.ant.BuildFileTest;

public class InstallerBuildTaskTest extends BuildFileTest {

	/**
	 * @param name
	 */
	public InstallerBuildTaskTest(String name) {
		super(name);		
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		configureProject("test-project/build.xml");
		getProject().setBasedir("test-project");		
		System.out.println(getProjectDir());
		
	}

	/*
	 * Test method for 'com.delcyon.updater.client.InstallerBuildTask.execute()'
	 */
	public void testExecute() throws Exception {
		try
		{
		executeTarget("clean");
		System.out.println(getLog());
		executeTarget("prepare");
		System.out.println(getLog());
		executeTarget("compile");
		System.out.println(getLog());
		executeTarget("create_jar");
		System.out.println(getLog());
		executeTarget("test");
		System.out.println(getLog());
		} catch (Exception exception)
		{
			System.err.println(getFullLog());
			throw exception;
		}
		File distrbutionJarFile = new File(getProjectDir(),"distribution/distribution.jar");
		URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{distrbutionJarFile.toURL()}, getClass().getClassLoader());
		String[] args = null;
		Class.forName("com.delcyon.updater.client.UpdaterClient",true,urlClassLoader).getMethod("setClassLoader",ClassLoader.class).invoke(null,urlClassLoader);
		Class.forName("com.delcyon.updater.client.UpdaterClient",true,urlClassLoader).getMethod("main",String[].class).invoke(null,(Object)args);
	}

}
