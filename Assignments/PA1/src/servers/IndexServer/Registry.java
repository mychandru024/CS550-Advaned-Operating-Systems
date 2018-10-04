/**
 * File Name : Registry.java
 * Description : implementation of a Registry class to register file with Index server
 * @authors : Ajay Ramesh and Chandra Kumar Basavaraju
 * version : 1.0
 * @date : 01/28/2017
 */
package cs550.pa1.servers.IndexServer;

/**
 * Registry class : implements Runnable
 */
public class Registry implements  Runnable {

    FileProcessor fileProcessor;
    String peerServerAddress;//peer server port to register along with filename
    String fileName;//file to be registered

    /**
     * arguement constructor
     * @param : fileProcessor - object to handle lock and synchronization on shared file in index server
     * @param : fileName - a String, file to be registered
     * @param : peerSeverAddress - a integer, peer server port where file resides
     * @return : void
     */
    public Registry(FileProcessor fileProcessor,String fileName, String peerServerAddress) {
        this.fileProcessor = fileProcessor;
        this.peerServerAddress = peerServerAddress;
        this.fileName = fileName;

    }

    @Override
    public void run() {
        try {
            fileProcessor.registry(fileName, peerServerAddress);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
