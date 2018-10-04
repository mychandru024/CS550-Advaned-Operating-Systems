/**
 * File Name : RunIndexMain.java
 * Description : implementaion of Index Server
 * @authors : Ajay Ramesh and Chandra Kumar Basavaraju
 * version : 1.0
 * @date : 01/28/2017
 */

package cs550.pa1.mains;

import cs550.pa1.helpers.Constants;
import cs550.pa1.servers.IndexServer.FileProcessor;
import cs550.pa1.servers.IndexServer.IndexServerThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;

/**
 * Index Server class : Starts the Index Server. Index Server runs on a specific port.
 * upon request from any of the peers ,creates a thread i.e IndexServerThread to process each request.
 */
public class RunIndexMain {
    
/**
 * Created by Ajay on 1/25/17.
 */
    public static void main(){

        //taking use input
        String  choice;
        int indexServerPort;
        System.out.print("Default config / Manual config ? (yes/no or y/n): ");
        Scanner in = new Scanner(System.in);
        choice = in.next();
        if(choice.equalsIgnoreCase("yes") ||
                choice.equalsIgnoreCase("y") ){
            indexServerPort = Constants.INDEX_SERVER_PORT_DEFAULT ;
        }else{
            System.out.println("Enter Index Server Port Address : ");
            indexServerPort = in.nextInt();
        }

        System.out.println("------------------------------------------");
        System.out.println("Configuration : ");
        System.out.println("Index Server Address -> "+Constants.HOST_DEFAULT+":"+indexServerPort);
        System.out.println("------------------------------------------");

        boolean listening = true;
        FileProcessor fileProcessor = new FileProcessor();
        String quit;

        try (ServerSocket serverSocket = new ServerSocket(indexServerPort)) {
            while (listening) {
                //creating new thread for a request, so that Index server can handle multiple requests at a time.
                //accepts the request and cretaes a new thread
                 new IndexServerThread(serverSocket.accept(),fileProcessor).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + indexServerPort);
            System.exit(-1);

        }


    }

    public static void main(String[] args) throws IOException {
        main();

    }
}
