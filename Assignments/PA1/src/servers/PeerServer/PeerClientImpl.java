/**
 * File Name : PeerClientImpl.java
 * Description : Implementation of Peer Client
 * @authors : Ajay Ramesh and Chandra Kumar Basavaraju
 * version : 1.0
 * @date : 01/28/2017
 */
package cs550.pa1.servers.PeerServer;

import cs550.pa1.helpers.Constants;
import cs550.pa1.helpers.Util;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Peer Client class
 */
public class PeerClientImpl implements Peer {

    public String hostName;//address of the index server
    public int indexServerPort;//port to contact on Index Server
    public int peerServerPort;//port on which this peer is listening

    WatcherThread wt;// watcher to monitor this peers directory for file creation/deletion

    public PeerClientImpl(String hostName, int peerServerPort) {
        this.hostName = hostName;
        this.peerServerPort = peerServerPort;
        this.indexServerPort = Constants.INDEX_SERVER_PORT_DEFAULT;
        wt = new WatcherThread(this.hostName, this.indexServerPort, this.peerServerPort);
    }
    public PeerClientImpl() throws IOException {
        this.indexServerPort = Constants.INDEX_SERVER_PORT_DEFAULT;
        this.peerServerPort = Constants.PEER_SERVER_PORT_DEFAULT;
	    this.hostName = "localhost";
	
	    wt = new WatcherThread(this.hostName, this.indexServerPort, this.peerServerPort);
    }

    //parameterized constructor
    public PeerClientImpl(String hostName, int indexServerPort, int peerServerPort) throws IOException {
        this.hostName = hostName;
        this.indexServerPort = indexServerPort;
        this.peerServerPort = peerServerPort;

        wt = new WatcherThread(this.hostName,this.indexServerPort, this.peerServerPort);
    }

    @Override
    public void init() {
	    wt.start();
        peerClientInterface();

    }

    //based on the user selection makes requests
    public void peerClientInterface() {

        String fileName="";
        int hostPort = Constants.CLIENT_PORT_DEFAULT;
        try {
            while(true){
                System.out.println("List of Choices : \n1 : Lookup a file\n2 : Download file from a peer\n3 : Register File\n4 : Exit\nEnter your choice number");
                Scanner in = new Scanner(System.in);
                int choice = in.nextInt();

                switch(choice){
                    case 1: System.out.println("Enter filename : ");
                        lookupFile(in.next());
                        break;
                    case 2:
                        System.out.println("Enter filename : ");
                        fileName = in.next();
                        System.out.println("Enter Host name of Peer to download (example: localhost) : ");
                        String hostName = in.next();


                        System.out.println("Enter Port address of Peer to download  (example: 6100) : ");
                        try{
                            hostPort = in.nextInt();

                        }catch (  Exception e){

                            System.out.println("Port address must be digit, refer user manual. ");
                            System.exit(0);
                        }
                        System.out.println("Request is sent to download ... ");
                        downloadFrom(fileName,hostName,hostPort);
                        break;
                    case 3:
                        System.out.println("Enter file location : ");
                        System.out.println("Example: /user/files/text1.txt  ");
                        registerFile(in.next(),""+this.hostName+":"+peerServerPort);
                        break;
                    case 4:
                        System.exit(0);
                    default:System.exit(0);
                }
            }
        }
        catch(Exception e){
            System.out.print("Invalid input : "+e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

    }

    public void lookupFile(String fileName) {
       Socket socketToIndexServer = null;
        try {
            socketToIndexServer = new Socket( this.hostName, this.indexServerPort );
            PrintWriter out = new PrintWriter( socketToIndexServer.getOutputStream(), true );
            BufferedReader in = new BufferedReader( new InputStreamReader( socketToIndexServer.getInputStream() ) );
            System.out.print("Socket port at client from peer client file : " + socketToIndexServer.getLocalPort()+"\n");
            out.println("lookup "+fileName);
            String message;
            System.out.println("File available location : ");
            System.out.println("***********************************************");
            //socketToIndexServer.shutdownOutput();
            while((message = in.readLine()) != null){
                System.out.println(message);
                if(message.length() == 0){
                    in.close();
                    out.close();
                    return;
                }

        }
        System.out.println("***********************************************");

        socketToIndexServer.close();
        } catch ( IOException e ) {
            System.out.print("Exception : "+e.getMessage());
           // e.printStackTrace();
        }
    }


    //send download request to another peer on that peer port
    private void downloadFrom(String fileName, String hostName, int port) throws IOException {
        Socket peerClientSocket = null;

        try{
            peerClientSocket = new Socket(hostName,port);
            PrintWriter out = new PrintWriter( peerClientSocket.getOutputStream(), true );
            InputStream in = peerClientSocket.getInputStream();
            OutputStream fout = new FileOutputStream(Constants.PEER_FOLDER_PREFIX + this.peerServerPort + "/" + fileName);

            out.println("Download "+fileName);
            System.out.println("Downloading ...");

            String message = "";
            byte[] bytes = new byte[16*1024];
            int count;

            while ((count = in.read(bytes)) > 0) {
                fout.write(bytes, 0, count);
            }

            System.out.println("Download Complete ...");
            System.out.println("Printing downloaded file contents : \n");

            Util.printFile(Constants.PEER_FOLDER_PREFIX + this.peerServerPort + "/" + fileName);


        }
        catch(Exception e){
            System.out.print("Invalid input from client "+e.getMessage());
        }
        finally{
            peerClientSocket.close();
        }
    }

    public void registerFile(String fileLocation, String requestPeerAddress) throws IOException{
       Socket sock = new Socket( this.hostName, this.indexServerPort );
        PrintWriter out = new PrintWriter(sock.getOutputStream(),true);
        out.println("register " + fileLocation + " " + requestPeerAddress );
        sock.shutdownInput();
    }
}


