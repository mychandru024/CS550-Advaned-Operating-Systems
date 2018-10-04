/**
 * File Name : RunPeerMain.java
 * Description : implementaion of a Peer
 * @authors : Ajay Ramesh and Chandra Kumar Basavaraju
 * version : 1.0
 * @date : 01/28/2017
 */


package cs550.pa1.mains;

import cs550.pa1.helpers.Constants;
import cs550.pa1.helpers.Util;
import cs550.pa1.servers.PeerServer.PeerClientImpl;
import cs550.pa1.servers.PeerServer.PeerServerImpl;

import java.util.Scanner;

/**
 * Peer Class : starts two threads one as client and another as a server
 */
public class RunPeerMain {

    String hostName;//hostname of index server
    int indexServerPort;//port number the index server is listening on
    int peerServerPort;//port number this peer server is listening on


    public RunPeerMain() {
        this.hostName = Constants.HOST_DEFAULT;
        this.indexServerPort = Constants.INDEX_SERVER_PORT_DEFAULT;
        this.peerServerPort = Constants.PEER_SERVER_PORT_DEFAULT;

    }


    /**
     * arguement constructor
     * @param : hostName - String, address of the Index Server
     * @param : indexServerPort - int, port on which indexing server is listening
     * @param : peerServerPort - int, port on which this peer server is listening
     * @return
     */

    public RunPeerMain(String hostName, int indexServerPort, int peerServerPort) {
        this.hostName = hostName;
        this.indexServerPort = indexServerPort;
        this.peerServerPort = peerServerPort;
        try {
            peerServer.start();
            peerServer.join(500);
            peerClient.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //creating new thread to act as a server
    Thread peerServer = new Thread () {
        public void run () {
            System.out.print("Peer Server Started");
            PeerServerImpl peerServerImpl = new PeerServerImpl(peerServerPort);
            peerServerImpl.init();
        }
    };

    //creating new thread to act as a client
    Thread peerClient = new Thread () {
        public void run () {

            System.out.println("------------------------------------------");
            System.out.println("Peer Client Started");
            try {
                PeerClientImpl peerClient = new PeerClientImpl(hostName, indexServerPort, peerServerPort);
                peerClient.init();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    public static void main(String[] args) {
          main();
    }

    static void main() {

        String hostName,choice;
        int indexServerPort,peerServerPort;

        Scanner in = new Scanner(System.in);
        System.out.print("Default config / Manual config ? (yes/no) or (y/n): ");
        choice = in.next();


        if(choice.equalsIgnoreCase("yes") ||
                choice.equalsIgnoreCase("y") ){
            hostName = Constants.HOST_DEFAULT;
            indexServerPort = Constants.INDEX_SERVER_PORT_DEFAULT ;
            peerServerPort = Constants.PEER_SERVER_PORT_DEFAULT;
        }else{
            System.out.println("Enter Host Name Example: 'localhost' or 127.0.0.1 ");
            hostName = in.next();
            System.out.println("Enter Index Server Port Address  : ");
            indexServerPort = in.nextInt();
            System.out.println("Enter PeerServer Port Address  : ");
            peerServerPort = in.nextInt();
        }

        Util.createFolder(Constants.PEER_FOLDER_PREFIX+peerServerPort);

        System.out.println("------------------------------------------");
        System.out.println("Configuration : ");
        System.out.println("Index Server Address : "+hostName+":"+indexServerPort);
        System.out.println("Peer Server Address : "+hostName+":"+peerServerPort);
        System.out.println("Peer Folder is created Dir Name:" + Constants.PEER_FOLDER_PREFIX+peerServerPort);
        System.out.println("------------------------------------------");

        RunPeerMain runPeerMain = new RunPeerMain(hostName, indexServerPort, peerServerPort);

    }

}
