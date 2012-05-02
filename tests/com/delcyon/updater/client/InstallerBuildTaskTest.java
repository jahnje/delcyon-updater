package com.delcyon.updater.client;

import org.apache.tools.ant.BuildFileTest;

import com.delcyon.updater.client.InstallerBuildTask;

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
		configureProject("test_resources/build.xml");
	}

	/*
	 * Test method for 'com.delcyon.updater.client.InstallerBuildTask.execute()'
	 */
	public void testExecute() {
		new InstallerBuildTask();
		executeTarget("test");
	}

}
