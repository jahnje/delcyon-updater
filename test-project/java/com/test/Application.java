package com.test;
import java.util.Arrays;
import com.testlib.TestLib;

public class Application
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		System.out.println("Test Application args= "+Arrays.toString(args));
		System.out.println(TestLib.getTestString());
	}

}
