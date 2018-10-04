/**
 * File Name : IndexServerThread.java
 * Description : implementaion of a thread for each request to Index Server
 * @authors : Ajay Ramesh and Chandra Kumar Basavaraju
 * version : 1.0
 * @date : 01/28/2017
 */

package cs550.pa1.servers.IndexServer;

import cs550.pa1.helpers.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * IndexServerThread class : Processes the request
 */
public class IndexServerThread extends  Thread{
    private Socket socket = null;
    FileProcessor fileProcessor;

    /**
     * arguement constructor
     * @param : socket - Socket, Socket in server connected to the client
     * @param : fileProcessor - int, handle for the index.txt file containing all the list of files registered. This is used to ensure synchronozation between threads processing client requests
     * @return
     */
    public IndexServerThread(Socket socket,FileProcessor fileProcessor) {
        super("IndexServerThread");//calling Thread constructor
        this.socket = socket;
        this.fileProcessor = fileProcessor;
    }

    /**
     * overriding java.lang.Thread run method
     * @param :
     * @return : void
     */
    public void run() {
        try (
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream()));
        ) {
            System.out.println("Socket at peer client from index server : "+ socket.getPort());
            String inputLine;

            //read the input on the socket and process the request based on message
            while ((inputLine = in.readLine()) != null) {
                process(inputLine, out);
                //out.println(inputLine);
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Processes the request message from client
     * @param : message - request message from the client
     * @param : out - handle to the output stream of the connected socket
     * @return : void
     */
    private void process(String message,  PrintWriter out) throws IOException {
        //splitting the message to identify the request
        String params[] = message.split(" ");

        if(params[0].equals("lookup")){
            //Creating a thread to handle file lookup request on index server
            try {
               LookUp lkup =  new LookUp(fileProcessor, out, params[1]);
                Thread lkup_thread = new Thread(lkup);
                lkup_thread.start();
                lkup_thread.join();
                System.out.println("Inside IndexServerThread");

                socket.shutdownOutput();
                return;
            }
            catch(Exception e){
                e.printStackTrace();
            }

        }else if (params[0].equals("register")) {
            //creating a thread to handle file register request to index server
            try {
                Registry rgstr = new Registry(fileProcessor, params[1], params[2]);
                Thread rgstr_thread = new Thread(rgstr);
                rgstr_thread.start();
                rgstr_thread.join();
            }
            catch(Exception e){
                e.printStackTrace();
            }

        }else if(params[0].equals("delete")){
            //handling automatic notification to index server when a file is deleted on peer
            for(String t : params){
                System.out.println(t + "\n");
            }
            Util.DeleteSingleLineInFile(params[1], params[2]);

        }
        else{
            System.out.println("Invalid Input");
        }

    }
}
