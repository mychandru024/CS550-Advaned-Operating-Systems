/**
 * File Name : LookUp.java
 * Description : implementation of a lookup class to search a file
 * @authors : Ajay Ramesh and Chandra Kumar Basavaraju
 * version : 1.0
 * @date : 01/28/2017
 */
package cs550.pa1.servers.IndexServer;

import java.io.PrintWriter;

/**
 * Look up class
 */
public class LookUp implements Runnable {

    FileProcessor fileProcessor;
    PrintWriter out;
    String fileName;//file name to be searched

    /**
     * arguement constructor
     * @param : fileProcessor - int, handle for the index.txt file containing all the list of files registered. This is used to ensure synchronozation between threads processing client requests
     * @param : out -  PrinterWriter, a handle to OutputStream of the connection socket
     * @param : fileName - a String containing the file name to be searched for
     * @return
     */
    public LookUp(FileProcessor fileProcessor, PrintWriter out, String fileName) {
        this.fileProcessor = fileProcessor;
        this.out = out;
        this.fileName = fileName;

    }

    @Override
    public void run() {
        try {
            //lookup request
            fileProcessor.lookup(fileName, out);
            System.out.println("Inside Lookup");
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
