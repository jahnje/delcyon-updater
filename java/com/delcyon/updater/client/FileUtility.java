/*
 * Created on Feb 17, 2009
 */
package com.delcyon.updater.client;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * @author jeremiah
 * @version $Id: $
 */
public abstract class FileUtility
{

    public static String getMD5ForFile(String fileName) throws Exception
    {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.reset();
        InputStream inputStream = new FileInputStream(fileName);      
        String fileMD5Sum = null;       
        //read in data from system file to md5 sum
        byte[] inputBuffer = new byte[1024];
        while (true) {                  
            int value = inputStream.read(inputBuffer);
            if (value < 0) {
                break;
            }
            messageDigest.update(inputBuffer,0,value);
        }       
        inputStream.close();
        //convert the byte array from the md5 digest to a string in hex
        fileMD5Sum = new BigInteger(1,messageDigest.digest()).toString(16);
        return fileMD5Sum;
    }
}
