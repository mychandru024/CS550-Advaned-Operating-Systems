/*
 * Copyright (C) 2017.  FileSharingSystem - https://github.com/ajayramesh23/FileSharingSystem
 * Programming Assignment from Professor Z.Lan
 * @author Ajay Ramesh
 * @author Chandra Kumar Basavaraj
 * Last Modified - 3/15/17 6:44 PM
 */

/**
 * File Name : Util.java
 * Description : Implementation of all utility functions
 * @authors : Ajay Ramesh and Chandra Kumar Basavaraju
 * version : 1.0
 * @date : 01/28/2017
 */
package cs550.pa3.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;


/**
 * Created by Ajay on 1/25/17.
 */
public  class Util {

    /**
     * Creates a folder is not already existing
     * @param rootDirName, path of the directory
     */
    public static void createFolder(String rootDirName){
        Util.print("Creating folder "+rootDirName);
        try {
            File file = new File(rootDirName);
            if (!file.exists()) {
                if (file.mkdirs()) {
                } else {
                    System.out.println("Failed to create directory!");
                }
            }
        }catch ( Exception e ){
            e.printStackTrace();
        }

    }

    /*

    public static void downloadFile(String filePath,Socket socket){
        File f = new File(filePath);
        try(
                InputStream fip = new FileInputStream(f);
                OutputStream out = socket.getOutputStream();
        ) {   //int content = 0;
            byte b[] = new byte[16 * 1024];
            int count;
            while ((count = fip.read(b)) > 0) {
                out.write(b, 0, count);
            }
            //fetch the file details from peerFiles object
            //out.write("fileVersion");//send file attributes : version, origin server, TTR and last modified time
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static boolean searchInMaster( String fileName) {
        boolean isFound = false;
        File file = new File(Util.getValue(Constants.MASTER_FOLDER,Constants.PEER_PROPERTIES_FILE) + "/" + fileName);
        if (file.exists()) {
            isFound = true;
        }
        return isFound;
    }

    public static boolean searchInCached(String fileName, int version, String originServer, boolean validateVersion){

        //BufferedReader br = null;
        RandomAccessFile raf = null;

        try {
            //createFolder(Constants.INDEX_FILE_NAME.split("/")[0]);

            //File file = new File("downloadlist.txt");
            raf = new RandomAccessFile(Util.getValue(Constants.DOWNLOADED_LIST,Constants.PEER_PROPERTIES_FILE), "rw");

            // if file doesnt exists, then create it
            if (raf == null) {
                //file.createNewFile();
                return false;
            }

            //br = new BufferedReader(new FileReader(file));
            String txt = null;

            while ((txt = raf.readLine()) != null) {
                System.out.println("file content : " + txt);
                if(txt.contains(fileName)){
                    String fileAttrs[] = txt.split(" ");
                    if(!validateVersion && fileAttrs[3].equals("valid")){
                        return true;
                    }
                    //System.out.println(txt+"\n");
                    if(validateVersion) {
                        if ((fileAttrs[2].equals(originServer)) && (version > Integer.parseInt(fileAttrs[1])) && (fileAttrs[3].equals("valid"))) {
                            //markFileInvalid(fileName, originServer);
                            //FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
                            //BufferedWriter bw = new BufferedWriter(fw);
                            txt = txt.replace("valid", "invalid");
                            System.out.println("Changed content : " + txt);
                            synchronized (Util.class){
                                raf.writeChars(txt);
                            }
                            return false;
                        } else if (fileAttrs[3] == "valid" && originServer.equals(fileAttrs[2])) {
                            return true;
                        }
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    */
    public static String getValue(String key) {

        Properties prop = new Properties();
        String filePath = "";

        try {

            InputStream inputStream =
                    Util.class.getClassLoader().getResourceAsStream("config.properties");

            prop.load(inputStream);
            filePath = prop.getProperty(key);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return filePath;

    }

    public static String getValue(String key, String fromFile) {

        Properties prop = new Properties();
        String filePath = "";

        try {

            InputStream inputStream =
                    Util.class.getClassLoader().getResourceAsStream(fromFile);

            prop.load(inputStream);
            filePath = prop.getProperty(key);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return filePath;

    }

    public static String getValue(String key, FileInputStream fromFile) {

        Properties prop = new Properties();
        String filePath = "";

        try {

            InputStream inputStream = fromFile;

            prop.load(inputStream);
            filePath = prop.getProperty(key);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return filePath;

    }

    public static void print(String input){
        if(Util.getValue("debug").equals("on"))
            System.out.println(new Date().toString()+" >>>> PA3 - Debug Information ::  "+ input);
    }

    public static void println(String input){
         System.out.println(input);
         //System.out.println(new Date().toString()+" >>>> PA3 -  Information ::  "+ input);
    }

    public  static void sleep(int seconds){
        try{
            Thread.sleep(seconds*1000);
        }catch(InterruptedException e){
            Util.print(e.getMessage());
        }
    }

    public static void error(String errorMessage){
        System.err.println("*******************************************************");
        System.err.println(errorMessage);
        System.err.println("*******************************************************");
        System.exit(-1);
    }

    public static String getJson(Object obj){
        ObjectMapper mapper = new ObjectMapper();
        //mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            return  mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }

   public static String getFormattedJson(Object obj){
    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    try {
      return  mapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return "";
   }

    public static Object toObjectFromJson(String json, Class classObject){
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            return mapper.readValue(json,classObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object toObjectJsonFromJson(String filePath,Class classObject){
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            return  Util.toObjectFromJson(content,classObject);
        } catch (IOException e) {
            Util.error("Error is reading the file - "+e.getMessage());
        }
        return null;
    }
    public static void printHeader(){
      Util.println(" ---------------------------------------------------------------- ");
      Util.println("/                                                                \\");



    }
  public static void printFooter(){
    Util.println("\\                                                                /");
    Util.println(" ---------------------------------------------------------------- ");

  }

  public static void modifyFile(String filePath){
      FileWriter fw = null;
      BufferedWriter bw = null;
      try{
          fw = new FileWriter(filePath, true);
          bw = new BufferedWriter(fw);
          bw.write("I am changed");
                }
      catch (Exception e){
          e.printStackTrace();
      }
      finally {

          try {
              if (bw != null)
                  bw.close();
              if (fw != null)
                  fw.close();
          } catch (IOException ex) {
              ex.printStackTrace();
          }
      }
  }
  private static String OS = System.getProperty("os.name").toLowerCase();
  public static boolean isMac() {

    return (OS.indexOf("mac") >= 0);

  }

  public static void main(String[] args) {

        Util.print(new String("query Test json:{f}").split(Constants.SPACE,3)[2]);
        Util.printHeader();
        Util.println("                      Peer Display Menu                           ");
        Util.printFooter();
        Util.print("Hello World");
        Util.createFolder("test/test");
        System.out.println(Util.getValue(Constants.MASTER_FOLDER));
        System.out.println(Util.getValue("push.switch"));
        System.out.println(Util.getValue("pull.switch"));
        System.out.println(Util.getValue("pull.TTR"));
        System.out.println(Util.getValue(Constants.CACHED_FOLDER));
        System.out.println(Util.getValue("debug"));
        try {
            System.out.print("Value of TTR "+Util.getValue("pull.TTR",new FileInputStream("peer.properties")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }
}