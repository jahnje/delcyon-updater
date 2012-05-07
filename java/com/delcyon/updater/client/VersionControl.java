/*
 * Created on Feb 12, 2009
 */
package com.delcyon.updater.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;
import java.util.logging.Level;

/**
 * @author jeremiah
 * @version $Id: $
 */
public class VersionControl
{

   
    private static VersionControl versionControl;

    public static VersionControl getVersionControl() throws Exception
    {
        if (versionControl == null)
        {
            versionControl = new VersionControl();
        }
        return VersionControl.versionControl;
    }

    
    private File file;

    public VersionControl() throws Exception
    {

      
    }

   

   

    public String getMD5ForFile(String fileName) throws Exception
    {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.reset();
        InputStream inputStream = new FileInputStream(fileName);
        String fileMD5Sum = null;
        // read in data from system file to md5 sum
        byte[] inputBuffer = new byte[1024];
        while (true)
        {
            int value = inputStream.read(inputBuffer);
            if (value < 0)
            {
                break;
            }
            messageDigest.update(inputBuffer, 0, value);
        }
        inputStream.close();
        // convert the byte array from the md5 digest to a string in hex
        fileMD5Sum = new BigInteger(1, messageDigest.digest()).toString(16);
        return fileMD5Sum;
    }

    /**
     * @param location
     * @return
     * @throws Exception
     * @throws IOException
     */
    

    private class MD5FilterOutputStream extends FilterOutputStream
    {
        private MessageDigest messageDigest;

        protected MD5FilterOutputStream(OutputStream out) throws NoSuchAlgorithmException
        {
            super(out);
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
        }

        @Override
        public void write(int b) throws IOException
        {
            messageDigest.update((byte) b);
            super.write(b);
        }

        /**
         * override write method
         */
        @Override
        public void write(byte[] data, int offset, int length) throws IOException
        {
            for (int i = offset; i < offset + length; i++)
            {
                this.write(data[i]);
            }
        }

        /**
         * override write method
         */
        @Override
        public void write(byte[] b) throws IOException
        {
            write(b, 0, b.length);
        }

        public String getMD5()
        {
            return new BigInteger(1, messageDigest.digest()).toString(16);
        }
    }

   

    /**
     * @param location
     * @return
     * @throws Exception
     */
   

    

   

    /**
     * @param masterFile
     * @param fileInputStream
     * @param rootOutputStream
     * @param string
     * @throws Exception
     */
    public String readClientVersionStreamIntoOutputStream(String masterFileName, OutputStream outputStream, CentralServicesRequest client, Vector<CSNode> csFilterVector) throws Exception
    {
        String copysrcDir = "";//Application.getConfiguration().getValue("CENTRAL_SERVICES_COPYSOURCE_DIR") + File.separator;
        if (masterFileName.startsWith(File.separator))
        {
            copysrcDir = "";
        }
        
        InputStream fileInputStream = UpdaterClient.classLoader.getResource(masterFileName).openStream();
        MD5FilterOutputStream md5rootOutputStream = new MD5FilterOutputStream(outputStream);
        OutputStream rootOutputStream = md5rootOutputStream;

        for (CSNode node : csFilterVector)
        {
            if (node instanceof CSFilter)
            {
                CSFilterOutputStream filterOutputStream = new CSFilterOutputStream((CSFilter) node, rootOutputStream, client);
                rootOutputStream = filterOutputStream;
            }
        }

        CentralServicesClient.logger.log(Level.FINE, "Copying " + masterFileName + " to client " + client.getClientID());
        readStreamIntoOutputStream(fileInputStream, rootOutputStream);
        return md5rootOutputStream.getMD5();
    }

    private void readStreamIntoOutputStream(InputStream inputStream, OutputStream outputStream) throws Exception
    {
        byte[] buffer = new byte[1024];
        int bytesRead = 0;
        while (bytesRead >= 0)
        {
            bytesRead = inputStream.read(buffer);
            if (bytesRead > 0)
            {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        outputStream.flush();
    }


}