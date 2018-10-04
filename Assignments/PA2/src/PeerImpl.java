package cs550.pa2;


import cs550.pa2.helpers.Constants;
import cs550.pa2.helpers.Host;
import cs550.pa2.helpers.Util;

import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
//import helpers.Constants;
//import helpers.Util;
/**
 * Created by Ajay on 2/24/17.
 */
public class PeerImpl implements Peer {
    static int messageID=0;

    ServerSocket serverSocket;
    Thread clientThread, serverThread;
    Thread cleanUpThread;

    Socket new_socket;
    
    HashMap seenMessages;
    HashMap seenQueryHitMessages;
    List<Host> neighbours;
    static List<String> thrash;
   

    Host host;

    public PeerImpl() {
        this.seenMessages = new HashMap<String,List<String>>();
        this.seenQueryHitMessages = new HashMap<String,List<String>>();
        neighbours = new ArrayList<Host>();
    }

    @Override
    public void search(String query_id, String fileName, int ttl, boolean isForward){
        Socket sock = null;
        if(!isForward){
            query_id = host.address() + "_" + Integer.toString(++messageID);
            ttl = 7;
        }
        try{
            for (Host neighbour:neighbours) {
                    //if(!query_id.contains(host.address())){
                        sock = new Socket(neighbour.getUrl(),neighbour.getPort());
                        PrintWriter out = new PrintWriter( sock.getOutputStream(), true );
                        out.println(Constants.QUERY + " " + query_id + " " + fileName + " " + host.address() + " " + Integer.toString(ttl));
                        out.close();
                        if(!seenMessages.containsKey(query_id)){
                        	List addr = new ArrayList<String>();
                        	this.seenMessages.put(query_id,addr);
                        }
                }
            }
            catch(IOException e){
                e.printStackTrace();
            }

    }

    @Override
	public void forwardQuery(String quer_id, String fileName, int ttl){
		search(quer_id, fileName, ttl,true);
	}

    @Override
    public void download(String fileName, String host, int port)throws IOException {
          Socket peerClientSocket = null;

          try{
              peerClientSocket = new Socket(host,port);
              PrintWriter out = new PrintWriter( peerClientSocket.getOutputStream(), true );
              //BufferedReader in = new BufferedReader(new InputStreamReader(peerClientSocket.getInputStream()));
              InputStream in = peerClientSocket.getInputStream();
              OutputStream fout = new FileOutputStream("sharedFolder" + this.host.address() + "/" + fileName);

              out.println(Constants.DOWNLOAD + " " + fileName);
              String message = "";
              PrintWriter p = new PrintWriter(fileName,"UTF-8");

              byte[] bytes = new byte[16*1024];

              int count;
              while ((count = in.read(bytes)) > 0) {
                  fout.write(bytes, 0, count);
              }
              p.close();

          }
          catch(Exception e){
              e.printStackTrace();
          }
          finally{
              peerClientSocket.close();
          }

    }

    @Override
    public void returnQueryHit(String msgid, String fileName, String addr, int ttl, boolean isForward) {
        //lookup
        Socket sock = null;
        List addresses = (List)seenMessages.get(msgid);
        //System.out.printf("Sending queryhit to %d peers",ports.size());
        Iterator i = addresses.iterator();
        if(!isForward){
            ttl = 7;
        }

        while(i.hasNext()){
            try{
            	String toSendAddr = (String)i.next();//got concurrent modification error
            	String addr_attrs[] = toSendAddr.split(":");
            	sock = new Socket(addr_attrs[0],Integer.valueOf(addr_attrs[1]));
                PrintWriter out = new PrintWriter( sock.getOutputStream(), true );

                out.println(Constants.QUERYHIT + " " + msgid + " " + fileName + " " + addr + " " + ttl);
                out.close();

                if(!seenQueryHitMessages.containsKey(msgid)){
                    //I saw the message I am sending
                    List p = new ArrayList<Integer>();
                    //ports.add(params[3]);//dont have to forward queryhit to myself
                    this.seenQueryHitMessages.put(msgid,p);
                }
                
                if(!thrash.contains(msgid)){
                	//System.out.println("pushing");
                	thrash.add(msgid);
                }
                //else{
                //	System.out.println("2. Not forwarding queryhit message");
                //}
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }

    }

	@Override
    public void forwardQueryHit(String queryHit_id, String fileName, String addr, int ttl){
                returnQueryHit(queryHit_id,fileName,addr,ttl,true);
    }

    @Override
    public void runPeerServer(){
        boolean listening = true;
        //Socket new_socket = null;
        try {
            this.serverSocket = new ServerSocket(host.getPort());
            while (listening) {
            	
                new_socket = this.serverSocket.accept();
                //try {
                	BufferedReader in = new BufferedReader(new InputStreamReader(new_socket.getInputStream()));
                    String message;
                    if ((message = in.readLine()) != null)
                        new Thread(new Runnable(){
                        	public void run(){
                            processInput(message,new_socket);
                            //System.out.println("Finished processing");
                            }
                        }).start();
                    //new_socket.close();
                /*} catch (IOException e) {
                    e.printStackTrace();
                }*/
            }
            System.out.println("Peer Server is running ");
        }catch (BindException e){
            System.err.println("*******************************************************");
            System.err.println("Peer Server address already in use, try again !");
            System.err.println("*******************************************************");
            System.exit(-1);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not listen on port " + host.getUrl());
            System.exit(-1);
        }

    }

    @Override
    public void runPeerClient(){
        String fileName="";
        try {
            while(true){
                System.out.println("\n1 : Lookup a file\n2 : Download file from a peer\n3 : Display seen query messages\n4 : Display seen queryhit messages\n5 : Exit\nEnter your choice number");
                Scanner in = new Scanner(System.in);
                int choice = in.nextInt();
                switch(choice){
                    case 1: System.out.println("Enter filename : \n");
                        fileName = in.next();
                        search(null,fileName,0,false);
                        break;
                    case 2:
                        System.out.println("Enter filename : \n");
                        fileName = in.next();
                        System.out.println("Enter Host name of the download server : \n");
                        String host = in.next();
                        System.out.println("Enter port number of of the download server : \n");
                        int hostPort = in.nextInt();
                        download(fileName,host,hostPort);
                        break;
                    case 3: // TODO remove all other choices
                        displaySeenMessages(Constants.QUERY);
                        break;
                    case 4:
                        displaySeenMessages(Constants.QUERYHIT);
                        break;
                    case 5:
                        System.exit(0);
                    default:
                        System.exit(0);
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    @Override
    public void displayPeerInfo(){
        System.out.printf("Peer address %s. \nMy Neighbours are \n",host.address());
        for(Host neighbour : neighbours){
            System.out.println(neighbour.address());
        }
    }

    private void processInput(String input,Socket socket) {
	    System.out.println("Received Message : " + input);
	    //String fileContent = "";
	    int senderPort = 0;
	    String params[] = input.split(" ");
	    if (params[0].equals(Constants.DOWNLOAD)){
            Util.downloadFile("sharedFolder" + this.host.address() + "/" + params[1],socket);
	    }
	    else if(params[0].equals(Constants.QUERY)){
		    //DisplaySeenMessages(params[0]);
		    int ttl = Integer.valueOf(params[4]);
                    ttl = ttl - 1;		

		    //not searching or forwarding already seen message
		    if(!seenMessages.containsKey(params[1]) && ttl > 0){

			    List addresses = new ArrayList<String>();
                	addresses.add(params[3]);
			    this.seenMessages.put(params[1],addresses);
                	forwardQuery(params[1],params[2],ttl);
			    if(Util.searchInMyFileDB(this.host.address(),params[2]))
				    returnQueryHit(params[1],params[2],host.address(),7,false);
            }
		    else{
			    List ports = (List)seenMessages.get(params[1]);
			    if(!ports.contains(params[3])){
				    ports.add(params[3]);
			    }
			    System.out.println("Not forwarding " + input);
		    }
	    }
	else if(params[0].equals(Constants.QUERYHIT)){
		//DisplaySeenMessages(params[0]);
		int ttl = Integer.valueOf(params[4]);
                ttl = ttl - 1;
	
		//Not forwarding already seen query hit messages
		if(!seenQueryHitMessages.containsKey(params[1]) && ttl > 0){
			List addr = new ArrayList<String>();
            addr.add(params[3]);
            this.seenQueryHitMessages.put(params[1],addr);
		
			String msg_params[] = params[1].split("_");
			if (msg_params[0].equals(host.address())){
				System.out.printf("\n------------------------------------------------------------------ \n" +
                        "File %s found at peer with port %s" +
                        " \n------------------------------------------------------------------\n",params[2],params[3]);
			}
			else{
				forwardQueryHit(params[1],params[2],params[3],ttl);
			}
		}
		else{
				String msg_params[] = params[1].split("_");
				if (msg_params[0].equals(host.address())){
					System.out.printf("\n------------------------------------------------------------------ \n " +
                            "File %s found at peer with port %s" +
                            "\n------------------------------------------------------------------\n",params[2],params[3]);
				}
				List ports = (List)seenQueryHitMessages.get(params[1]);
				if(!ports.contains(params[3])){
					ports.add(params[3]);
				}
				System.out.println("Not forwarding " + input);
		}
	    }
    }

    public void displaySeenMessages(String type){
		System.out.println("Displaying seen " + type + " messages");
		Set set = null;
		if(type.equals(Constants.QUERY)){
		set = seenMessages.entrySet();
		}
		else{
			set = seenQueryHitMessages.entrySet();
		}
		Iterator i = set.iterator();
		while(i.hasNext()){
			Map.Entry entry = (Map.Entry)i.next();
			System.out.println(entry.getKey() + ":" + entry.getValue());
		}
	}

	@Override
    public void initConfig(String hostName, int port) {

        host = new Host(hostName,port);
	    neighbours = new ArrayList<Host>();
	    thrash = new ArrayList<String>();

        // Read the list of config files
        String line = null;
	    File file = new File(port+Constants.CONFIG_FILE);
	    try(BufferedReader br = new BufferedReader(new FileReader(file))){

            while((line = br.readLine()) != null) neighbours.add(
                    new Host(line.split(":")[0],
                              Integer.parseInt(line.split(":")[1])));
        }
	    catch(IOException e){
		    e.printStackTrace();
	    }
	    displayPeerInfo();

	    Util.createFolder("sharedFolder"+host.address());


        serverThread = new Thread () {
            public void run () {
                runPeerServer();
            }
        };
        clientThread = new Thread () {
            public void run () {
                try {
                    runPeerClient();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        cleanUpThread = new Thread () {
            public void run () {
                try {
                    cleanUpSeenMessages();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        serverThread.start();
        clientThread.start();
        cleanUpThread.start();

    }
	public void cleanUpSeenMessages(){
		while(true){
				try{
				Thread.sleep(5000);
				for (String qid : thrash){
					System.out.println("Removing " + qid);
					seenMessages.remove(qid);
					seenQueryHitMessages.remove(qid);
				}
				thrash.clear();
				}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
