package com.delcyon.updater.client;

import org.apache.tools.ant.BuildFileTest;

import com.delcyon.updater.client.InstallerBuildTask;


public class CallURLBuildTaskTest extends BuildFileTest {

	/**
	 * @param name
	 */
	public CallURLBuildTaskTest(String name) {
		super(name);		
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		configureProject("test_resources/build.xml");
	}

	/*
	 * Test method for 'com.delcyon.updater.client.InstallerBuildTask.execute()'
	 */
	public void testExecute() {
		new InstallerBuildTask();
		executeTarget("URL");
	}

}
