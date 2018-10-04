/*
 * Copyright (C) 2017.  FileSharingSystem - https://github.com/ajayramesh23/FileSharingSystem
 * Programming Assignment from Professor Z.Lan
 * @author Ajay Ramesh
 * @author Chandra Kumar Basavaraj
 * Last Modified - 3/15/17 6:44 PM
 */

package cs550.pa3.processor;

import cs550.pa3.helpers.Constants;
import cs550.pa3.helpers.Host;
import cs550.pa3.helpers.PeerFile;
import cs550.pa3.helpers.PeerFiles;
import cs550.pa3.helpers.Util;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class PeerImpl implements Peer {

  private static int messageID = 0;
  private static List<String> thrash;
  private ServerSocket serverSocket;
  private Thread clientThread;
  private Thread serverThread;
  private Thread cleanUpThread;
  private Thread pullThread;
  private WatcherThread watchThread;
  private Socket new_socket;
  private HashMap seenMessages;
  private HashMap seenQueryHitMessages;
  private Set seenInvalidationHitMessages;
  private List<Host> neighbours;
  private Host host;
  //private PeerFiles myfiles;
  //private PeerFiles downloadedFiles;
  public PeerFiles peerFiles;
  int pullOrPush;//1 : Pull, 2 : Push

  //todo - iniital file contents are not indexed by watch thread

  public PeerImpl() {
    this.seenMessages = new HashMap<String, List<String>>();
    this.seenQueryHitMessages = new HashMap<String, List<String>>();
    this.neighbours = new ArrayList<Host>();
    this.seenInvalidationHitMessages = new HashSet();

    thrash = new ArrayList<String>();
    peerFiles = new PeerFiles();
    pullOrPush = 2;

  }

  private synchronized void addToSeenMessages(String messageId, String address){
    if(seenMessages.containsKey(messageId)){
      List list_addr = (List) seenMessages.get(messageId);
      if (!list_addr.contains(address)) {
        list_addr.add(address);
        seenMessages.remove(messageId);
        seenMessages.put(messageId,list_addr);
      }
    }
    else{
      List addr_list = new ArrayList<String>();
      addr_list.add(address);
      this.seenMessages.put(messageId, addr_list);
    }

  }

  private synchronized void removeFromSeenMessages(String messageId){
    if(seenMessages.containsKey(messageId)){
      seenMessages.remove(messageId);
    }
  }

  private synchronized List<String> getSeenMessages(String messageId){
    return (List)seenMessages.get(messageId);
  }

  private synchronized void addToSeenQueryHitMessages(String fileName, String address){

    if(seenQueryHitMessages.containsKey(fileName)){
      List list_addr = (List) seenMessages.get(fileName);
      if (!list_addr.contains(address)) {
        list_addr.add(address);
        seenMessages.remove(fileName);
        seenMessages.put(fileName,list_addr);
      }
    }
    else{
      List addr_list = new ArrayList<String>();
      addr_list.add(address);
      this.seenQueryHitMessages.put(fileName, addr_list);
    }

  }

  private synchronized void removeFromSeenQueryHitMessages(String fileName){
    if(seenQueryHitMessages.containsKey(fileName)){
      seenQueryHitMessages.remove(fileName);
    }
  }

  private synchronized List<String> getSeenQueryHitMessages(String messageId){
    return (List)seenQueryHitMessages.get(messageId);
  }

  @Override
  public void search(String query_id, String fileName, int ttl, boolean isForward) {
    Socket sock = null;
    if (!isForward) {
      query_id = host.address() + "_" + Integer.toString(++messageID);
      ttl = Constants.TTL;
    }
    try {
      for (Host neighbour : neighbours) {
        //if(!query_id.contains(host.address())){
        sock = new Socket(neighbour.getUrl(), neighbour.getPort());
        PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
        out.println(
            Constants.QUERY + " " + query_id + " " + fileName + " " + host.address() + " " + Integer
                .toString(ttl));
        out.close();
        addToSeenMessages(query_id,null);
      }
    } catch (ConnectException e) {
      Util.error("Peer Server is not running ....");
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  @Override
  public void forwardQuery(String quer_id, String fileName, int ttl) {
    search(quer_id, fileName, ttl, true);
  }

  @Override
  public void download(String fileName, String host, int port) throws IOException {
    String metaDataFileName  = getCacheFolderName(this.host)+ "/" + fileName+""+Constants.TEMP_FILE;
    downloadFile(Constants.DOWNLOAD_METADATA,metaDataFileName,host,port,fileName);
    downloadFile(Constants.DOWNLOAD,getCacheFolderName(this.host)+ "/" + fileName,host,port,fileName);
    PeerFile cacheFile = (PeerFile) Util.toObjectJsonFromJson(metaDataFileName,PeerFile.class);
    cacheFile.setOriginal(false);
    peerFiles.add(cacheFile);
    deleteMetaDataFile(metaDataFileName);
 }


  public void refresh(String fileName, String host, int port) throws IOException {
    String metaDataFileName  = getCacheFolderName(this.host)+ "/" + fileName+""+Constants.TEMP_FILE;
    downloadFile(Constants.DOWNLOAD_METADATA,metaDataFileName,host,port,fileName);
    downloadFile(Constants.DOWNLOAD,getCacheFolderName(this.host)+ "/" + fileName,host,port,fileName);
    synchronized (peerFiles){
      PeerFile cacheFile = (PeerFile) Util.toObjectJsonFromJson(metaDataFileName,PeerFile.class);
      cacheFile.setOriginal(false);
      cacheFile.setIsStale(false);
      peerFiles.add(cacheFile);
      deleteMetaDataFile(metaDataFileName);
    }
  }

  /**
   * TODO check synchronized p1
   * @param msgid
   * @param fileName
   * @param addr
   * @param ttl
   * @param isForward
   */
  @Override
  public void returnQueryHit(String msgid, String fileName, String addr, int ttl, boolean isForward) {
    //lookup
    Socket sock = null;
    for(String address: getSeenMessages(msgid)){
      Util.print("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
      Util.print(address);

    }
    List addresses = getSeenMessages(msgid);
    //System.out.printf("Sending queryhit to %d peers",ports.size());
    Iterator i = addresses.iterator();
    if (!isForward) {
      ttl = Constants.TTL;
    }
    while (i.hasNext()) {
      try {
        String toSendAddr = (String) i.next();//got concurrent modification error
        String addr_attrs[] = toSendAddr.split(":");
        sock = new Socket(addr_attrs[0], Integer.valueOf(addr_attrs[1]));
        PrintWriter out = new PrintWriter(sock.getOutputStream(), true);

        Util.print("File Information to send with QueyHit\n"+ Util.getJson(peerFiles.getFilesMetaData().get(fileName)));
        Util.print(queryHitMessage(msgid,fileName,addr,ttl));
        out.println(queryHitMessage(msgid,fileName,addr,ttl));

        // out.println(Constants.QUERYHIT + " " + msgid + " " + fileName + " " + addr + " " + ttl);
        out.close();

        //I saw the message I am sending
        //ports.add(params[3]);//dont have to forward queryhit to myself
        addToSeenQueryHitMessages(msgid, null);

        if (!thrash.contains(msgid)) {
          //Util.print();("pushing");
          thrash.add(msgid);
        }
        //else{
        //	Util.print();("2. Not forwarding queryhit message");
        //}
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  @Override
  public void forwardQueryHit(String queryHit_id, String fileName, String addr, int ttl) {
    returnQueryHit(queryHit_id, fileName, addr, ttl, true);
  }

  @Override
  public void runPeerServer() {
    Util.print("Peer Server running ... ");
    boolean listening = true;
    try {
      this.serverSocket = new ServerSocket(host.getPort());
      while (listening) {
        new_socket = this.serverSocket.accept();
        BufferedReader in = new BufferedReader(new InputStreamReader(new_socket.getInputStream()));
        String message;
        if ((message = in.readLine()) != null) {
          new Thread(new Runnable() {
            public void run() {
              processInput(message, new_socket);
            }
          }).start();
        }
      }
      Util.print("Peer Server is running ");
    } catch (BindException e) {
      Util.error("Peer Server address already in use, try again !");
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println("Could not listen on port " + host.getUrl());
      System.exit(Constants.MINUS_ONE);
    }

  }

  @Override
  public void runPeerClient() {
    Util.print("Peer Client running ... ");
    String fileName = "";
    try {
      while (true) {
        Util.println("******************************************************************");
        Util.println("Peer Display Menu ************************************************");
        Util.println(Constants.DISPLAY_MENU);
        Util.println("******************************************************************");


        Scanner in = new Scanner(System.in);
        System.out.print("Your Choice : ");
        int choice = in.nextInt();
        switch (choice) {
          case 1:
            System.out.print("Enter filename : ");
            fileName = in.next();
            search(null, fileName, Constants.ZERO, false);
            break;
          case 2:
            System.out.print("Enter filename : ");
            fileName = in.next();
            System.out.print("Enter Host name of the download server : ");
            String host = in.next();
            System.out.print("Enter port number of of the download server : ");
            int hostPort = in.nextInt();
            download(fileName, host, hostPort);
            Util.println("Message to User: \nFile is downloaded to "+getCacheFolderName(this.host)+" Folder");
            break;
          case 3: // TODO remove all other choices
            displaySeenMessages(Constants.QUERY);
            break;
          case 4:
            displaySeenMessages(Constants.QUERYHIT);
            break;
          case 5:
            displayDownloadedFilesInfo();
            break;
          case 6:
            System.out.print("Enter filename : \n");
            fileName = in.next();
            Host h = peerFiles.getFileMetadata(fileName).getFromAddress();
            refresh(fileName, h.getUrl(), h.getPort());
            break;
          case 7:
            System.out.print("Enter filename : \n");
            fileName = in.next();
            Util.modifyFile(getMasterFolderName(this.host) + "/" + fileName);
            break;
          case 8:
            System.exit(Constants.ZERO);
          default:
            Util.print("Invalid Input Try again ! ");
            continue;
        }
      }
    } catch (InputMismatchException e){
      Util.print("Invalid Input Try again ! ");
      runPeerClient();

    }catch (Exception e) {
      e.printStackTrace();
      System.exit(Constants.MINUS_ONE);
    }
  }

  @Override
  public void displayPeerInfo() {
    Util.print("Peer address "+host.address());
    Util.print("Peer Neighbours  ");
    for (Host neighbour : neighbours) {
      Util.print(neighbour.address());
    }
  }

  @Override
  public void initConfig(String hostName, int port) {
    host = new Host(hostName, port);
    createFolders(host);
    readFromFile(Constants.CONFIG_FILE_PREFIX + port + Constants.PEER_PROPERTIES_FILE);
    displayPeerInfo();
    serverThread = new Thread() {
      public void run() {
        runPeerServer();
      }
    };
    clientThread = new Thread() {
      public void run() {
        runPeerClient();
      }
    };
    cleanUpThread = new Thread() {
      public void run() {
        cleanUpSeenMessages();
      }
    };
    pullThread = new Thread() {
      public void run() {
        runPullProcess();
      }
    };
    watchThread = new WatcherThread(this, getMasterFolderName(host));
    serverThread.start();
    clientThread.start();
    cleanUpThread.start();
    //pullThread.start();
    watchThread.start();

  }

  @Override
  public void handleBroadCastEvents(String originServer, String changedFileName, int fileVersion,
      int ttl, boolean isForward, String receivedFrom) {
    Socket sock = null;
    String messageId = host.address() + "_" + Integer.toString(++messageID);
    if (!isForward) {
      ttl = Constants.TTL;
      originServer = host.address();
    }
    for (Host h : neighbours) {
      if(h.address().equals(receivedFrom)){
        Util.println("Not forwarding to server from whom I received this message");
        continue;
      }
      try {
        sock = new Socket(h.getUrl(), h.getPort());
        PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
        out.println(Constants.INVALIDATION + " " + messageId + " " + changedFileName + " " + Integer
            .toString(fileVersion) + " " + Integer.toString(ttl) + " " + originServer);
        sock.close();
        } catch (Exception e) {
        e.printStackTrace();
        try {
          sock.close();
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
      }
    }

  }


  public void handleForwardBroadCastEvents(String originServer, String changedFileName,
      int fileVersion, int ttl,String receivedFrom) {
    handleBroadCastEvents(originServer, changedFileName, fileVersion, ttl, true,receivedFrom);
  }

  /**
   * 1. Create a Pull->thread and let that run in background when peerserver starts.
   * 2.
   */

  @Override
  public void runPullProcess() {
    Util.print("Pull Thread Started Running.");
    Pull pullEvent = new Pull(this);
    while (true) {
      int ttr = Integer.parseInt(Util.getValue("pull.TTR"));
      pullEvent.trigger();
      Util.print("Pull thread will awake in "+ttr+" seconds");
      Util.sleep(ttr);

    }
  }

  private void sendPullRequest(String fileName,String address){
    PeerFile file =  peerFiles.getFileMetadata(fileName);
    file.setIsStale(false);
    if(!file.isOriginal())  file.setOriginal(false);

    Socket peerClientSocket = null;
    try {
      peerClientSocket = new Socket(address.split(":")[0], Integer.parseInt(address.split(":")[1]));
      PrintWriter out = new PrintWriter(peerClientSocket.getOutputStream(), true);
      out.println(Constants.PULL + " " + file.getName()+" "+Util.getJson(file));
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        peerClientSocket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  private void parsePullRequest(String input,String fileName) {
    try{

      synchronized (peerFiles){PeerFile file = (PeerFile) Util.toObjectFromJson(input.split(Constants.SPACE,3)[2],PeerFile.class);
        if(peerFiles.getFilesMetaData().get(fileName).getVersion() != file.getVersion()){
          peerFiles.getFilesMetaData().remove(fileName);
          file.setIsStale(true);
          peerFiles.getFilesMetaData().put(fileName,file);
          Util.println("File Meta Data out-dated");
        }
      }

    }catch (Exception e){
      Util.println(e.getMessage()+"\nException Occurred while parsing the poll request\n"+input);
    }

  }

  // todo synchronized ch5eck p
  private void processInput(String input, Socket socket) {
    Util.print("Received Message : " + input);

    String params[] = input.split(Constants.SPACE);
    if(params[0].equals(Constants.POLL)){
      sendPullRequest(params[1],params[2]);
      return;
    }

    if(params[0].equals(Constants.PULL)){
      parsePullRequest(input,params[1]);
      return;
    }

    if (params[0].equals(Constants.DOWNLOAD) || params[0].equals(Constants.DOWNLOAD_METADATA) ) {
      Util.print("Serving request of the type = "+params[0]);
      serveDownloadRequest(params[0], params[1], socket);
    } else if (params[0].equals(Constants.QUERY)) {
      int ttl = Integer.valueOf(params[4]);
      ttl = ttl - 1;

      //not searching or forwarding already seen message
      if (!seenMessages.containsKey(params[1]) && ttl > Constants.ZERO) {

        //List addresses = new ArrayList<String>();
        //addresses.add(params[3]);
        addToSeenMessages(params[1], params[3]);
        forwardQuery(params[1], params[2], ttl);

        /*
        if (( peerFiles.fileExists(params[2]) && (peerFiles.getFileMetadata(params[2]).isOriginal() || (!peerFiles.getFileMetadata(params[2]).isStale())))) {//for cached files need to check if it is valid?
          returnQueryHit(params[1], params[2], host.address(), Constants.TTL, false);
        }
        */

        //only to collect experiment results
        if ( peerFiles.fileExists(params[2])) {
          returnQueryHit(params[1], params[2], host.address(), Constants.TTL, false);
        }

      } else {
        //List ports = (List) seenMessages.get(params[1]);
        //if (!ports.contains(params[3])) {
          //ports.add(params[3]);
        //}
          addToSeenMessages(params[1], params[3]);
        Util.print("Not forwarding " + input);
      }
    } else if (params[0].equals(Constants.QUERYHIT)) {
      //DisplaySeenMessages(params[0]);
      int ttl = Integer.valueOf(params[4]);
      ttl = ttl - 1;



      //Not forwarding already seen query hit messages
      if (!seenQueryHitMessages.containsKey(params[1]) && ttl > Constants.ZERO) {
        //List addr = new ArrayList<String>();
        //addr.add(params[3]);
        //this.seenQueryHitMessages.put(params[1], addr);
        addToSeenQueryHitMessages(params[1],params[3]);

        String msg_params[] = params[1].split("_");
        if (msg_params[0].equals(host.address())) {
          System.out
              .printf("\n------------------------------------------------------------------ \n" +
                      "File %s found at peer with port %s" +
                      " \n------------------------------------------------------------------\n",
                  params[2], params[3]);
        } else {
          forwardQueryHit(params[1], params[2], params[3], ttl);
        }
      } else {
        String msg_params[] = params[1].split("_");
        if (msg_params[0].equals(host.address())) {
          System.out
              .printf("\n------------------------------------------------------------------ \n " +
                      "File %s found at peer with port %s" +
                      "\n------------------------------------------------------------------\n",
                  params[2], params[3]);
        }
        else{
          List addresses = getSeenQueryHitMessages(params[1]);
          if (!addresses.contains(params[3])) {
            forwardQueryHit(params[1], params[2], params[3], ttl);
            addToSeenQueryHitMessages(params[1],params[3]);
          }
          else {
            Util.print("Not forwarding " + input);
          }
        }
      }
    } else if (params[0].equals(Constants.INVALIDATION)) {
      int ttl = Integer.valueOf(params[4]);
      ttl = ttl - 1;

      //not forwarding already seen message
      if (!seenInvalidationHitMessages.contains(params[1]) && ttl > Constants.ZERO) {
        this.seenInvalidationHitMessages.add(params[1]);
        handleForwardBroadCastEvents(params[5], params[2], Integer.parseInt(params[3]), ttl,params[1].split("_")[0]);
        //Util.searchInCached(params[2],Integer.parseInt(params[3]),params[1].split("_")[0],true);
        if (peerFiles.fileExistsAndValid(params[2], params[5])){//filename, origin server
          //Util.println("Updating as Stale");
          peerFiles.updateFileMetadata(params[2], Integer.parseInt(params[3]));
        }
      } else {
        Util.print("Not forwarding " + input);
      }
    }
  }

  public void displaySeenMessages(String type) {
    Util.print("Displaying seen " + type + " messages");
    Set set = null;
    if (type.equals(Constants.QUERY)) {
      set = seenMessages.entrySet();
    } else {
      set = seenQueryHitMessages.entrySet();
    }
    Iterator i = set.iterator();
    while (i.hasNext()) {
      Map.Entry entry = (Map.Entry) i.next();
      Util.print(entry.getKey() + ":" + entry.getValue());
    }
  }

  void readFromFile(String path) {

    String neighbors[] = new String[0];
    try {
      neighbors = Util.getValue(Constants.PEER_NEIGHBORS, new FileInputStream(path))
          .split(",");
    } catch (FileNotFoundException e) {
      Util.error("Peer Configuration is not present\nCreate  configuration "
          + "\nFileName should be: " + path +"\n\nContents of the file\n\n"
          + "peer.host=localhost\npeer.port=4000\npeer.neighbour=localhost:5000,123.34.3.3:8080");

    }
    for (String neighbour : neighbors) {
      String params[] = neighbour.split(":");
      neighbours.add(new Host(params[0], Integer.parseInt(params[1])));
    }
  }

  public void pollRequest(PeerFile f) {
    Socket peerClientSocket = null;
    try {
      Host from = f.getFromAddress();
      peerClientSocket = new Socket(from.getUrl(), from.getPort());
      PrintWriter out = new PrintWriter(peerClientSocket.getOutputStream(), true);
      out.println(Constants.POLL + " " + f.getName()+" "+this.host.address());
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        peerClientSocket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void handleWatcherThreadEvents(String eventType, String fileName) {
    Util.print("Event = " + eventType + " file = " + fileName);
    if (eventType.equals("ENTRY_CREATE") ||
         (eventType.equals("ENTRY_MODIFY") &&   Util.isMac())) {
      if(peerFiles.fileExists(fileName)){//file is modified
        PeerFile modifiedFile = peerFiles.getFileMetadata(fileName);
        modifiedFile.setVersion(modifiedFile.getVersion() + 1);
        modifiedFile.setLastUpdated(LocalDateTime.now());
        modifiedFile.setIsStale(false);
        peerFiles.add(modifiedFile);
        Util.print(Util.getJson(modifiedFile));
        if(pullOrPush == 2)
          handleBroadCastEvents(null, fileName, modifiedFile.getVersion(), Constants.ZERO, false, null);
      }//file is created
      else
        peerFiles.getFilesMetaData().put(fileName,new PeerFile(1,true, fileName, Integer.parseInt(Util.getValue("TTR")), host,false,LocalDateTime.now()));
        Util.print(Util.getJson(peerFiles));
    } else if (eventType.equals("ENTRY_DELETE")) {
      if(peerFiles.fileExists(fileName)) {
        PeerFile deletedFile = peerFiles.getFileMetadata(fileName);
        deletedFile.setIsStale(true);
        peerFiles.add(deletedFile);
      }
    }
  }

  public void serveDownloadRequest(String taskType, String fileName, Socket socket) {
    /**
     * 1.Search in peerFiles
     * 2.Check filename.stale == true if true return "Invalid Message"
     * 3.If above condition fails, then send the file
     *  3.a Send File MetaData
     *  3.b Send FIle content
     */
    if (isPeerFileOutdated(fileName)) {
       Util.println("File is outdated, try after some time "+fileName);
    }
    switch (taskType){
      case Constants.DOWNLOAD:
        sendFileData(fileName,socket);
        break;
      case Constants.DOWNLOAD_METADATA:
        sendFileMetadata(fileName,socket);
        break;
    }
  }

  private void sendFileData(String fileName, Socket socket) {
    Util.print("Sending File Data " + getFilePath(fileName));

    try (
        InputStream fip = new FileInputStream(getFilePath(fileName));
        OutputStream out = socket.getOutputStream();
    ) {
      byte b[] = new byte[16 * 1024];
      int count;
      while ((count = fip.read(b)) > Constants.ZERO) {
        out.write(b, Constants.ZERO, count);
      }
      out.flush();
    } catch (FileNotFoundException e) {
      Util.print("File is not present in any below locations: \n" + getCacheFolderName(this.host) + "\n"
          + getMasterFolderName(this.host));
      return;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void sendFileMetadata(String fileName, Socket socket) {
    Util.print("Sending Meta Data.");
    try (
        OutputStream out = socket.getOutputStream();
    ) {
      InputStream fip = new ByteArrayInputStream(Util.getJson(peerFiles.getFileMetadata(fileName)).getBytes(StandardCharsets.UTF_8));
      byte b[] = new byte[16 * 1024];
      int count;
      while ((count = fip.read(b)) > Constants.ZERO) {
        out.write(b, Constants.ZERO, count);
      }
      out.flush();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private boolean isPeerFileOutdated(String fileName) {
    try{
      if(peerFiles.getFilesMetaData().get(fileName).isOriginal()) return false;
      if(peerFiles.getFilesMetaData().get(fileName).isStale()) return true;
      return false;
    }catch (NullPointerException e) {
      Util.error("Peer Data is corrupted please restart again.");
      return true;
    }
  }

  private String getFilePath(String fileName) {
    if(peerFiles.getFilesMetaData().get(fileName).isOriginal())
      return  getMasterFolderName(this.host) + "/" + fileName;
    return getCacheFolderName(this.host) + "/" + fileName;
  }

  public void displayDownloadedFilesInfo() {
    HashMap<String, PeerFile> peerDatabase = peerFiles.getFilesMetaData();
    Util.printHeader();
    Util.println("                      Peer File Information                        ");
    Util.printFooter();

    for (PeerFile file : peerDatabase.values()) {
      Util.printHeader();
      Util.println(Util.getFormattedJson(file));
      Util.printFooter();

    }
  }

  public String queryHitMessage(String messageID, String fileName, String address, int ttl) {
    return
        Constants.QUERYHIT + " "
            + messageID + " " +
            fileName + " " +
            address + " " +
            ttl + " " +
            Util.getJson(peerFiles.getFilesMetaData().get(fileName)) +
                " " + peerFiles.getFileMetadata(fileName).getLastUpdated() +
                " " + ((peerFiles.getFileMetadata(fileName).isOriginal())?1:0);
  }

  public void cleanUpSeenMessages() {
    while (true) {
      try {
        Thread.sleep(5000);
        for (String qid : thrash) {
          Util.print("Removing " + qid);
          //seenMessages.remove(qid);
          //seenQueryHitMessages.remove(qid);
          removeFromSeenMessages(qid);
          removeFromSeenQueryHitMessages(qid);
        }
        thrash.clear();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private String getMasterFolderName(Host host){
    return  Util.getValue(Constants.MASTER_FOLDER, Constants.PEER_PROPERTIES_FILE) + "_" + host.getHashCode();
  }

  private String getCacheFolderName(Host host){
    return  Util.getValue(Constants.CACHED_FOLDER, Constants.PEER_PROPERTIES_FILE) + "_" + host.getHashCode();
  }

  private void createFolders(Host host) {
    Util.createFolder(getMasterFolderName(host));
    Util.createFolder(getCacheFolderName(host));
  }

  /**
   * 1.Download the file PeerServer
   *  1.a) create a temp file in cache fileName_temp.json nd read the buffer content from server
   */
  private void downloadFile(String taskType,String fileName, String host, int port, String fname) {
    Socket peerClientSocket = null;
    try {
      peerClientSocket = new Socket(host, port);
      PrintWriter out = new PrintWriter(peerClientSocket.getOutputStream(), true);
      InputStream in = peerClientSocket.getInputStream();
      OutputStream fout = new FileOutputStream(fileName);
      out.println(taskType + " " + fname);
      String message = "";
      PrintWriter p = new PrintWriter(fileName, "UTF-8");
      byte[] bytes = new byte[16 * 1024];

      int count;
      while ((count = in.read(bytes)) > Constants.ZERO) {
        fout.write(bytes, Constants.ZERO, count);
      }
      p.close();

    }catch (ConnectException e){
      Util.error(e.getMessage());
    }catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        peerClientSocket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  private void updatePeerFilesList(String fileName) {
    Util.print("File Name for json "+fileName);
    //todo
    /**
     * read the content from json
     * store it to the meta data
     */
  }

  private void deleteMetaDataFile(String filePath){
    try {
      Files.delete(Paths.get(filePath));
    } catch (NoSuchFileException x) {
      System.err.format("%s: no such" + " file or directory%n", filePath);
    } catch (DirectoryNotEmptyException x) {
      System.err.format("%s not empty%n", filePath);
    } catch (IOException x) {
      // File permission problems are caught here.
      System.err.println(x);
    }
  }

}
