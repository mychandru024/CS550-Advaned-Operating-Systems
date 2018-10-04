/**
 * File Name : Util.java
 * Description : Implementation of all utility functions
 * @authors : Ajay Ramesh and Chandra Kumar Basavaraju
 * version : 1.0
 * @date : 01/28/2017
 */
package cs550.pa1.helpers;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ajay on 1/25/17.
 */
public  class Util {

    /**Searches if the provided file is registered with the Index Server
     *
     * @param text file name
     * @return list of all peers containing the file
     */
    public static List<String> searchInFile(String text){

        BufferedReader br = null;
        List<String> lst = new ArrayList<String>();
        try {
            createFolder(Constants.INDEX_FILE_NAME.split("/")[0]);

            File file = new File(Constants.INDEX_FILE_NAME);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            br = new BufferedReader(new FileReader(file));
            String txt = null;

            while ((txt = br.readLine()) != null) {
                //System.out.println("File content : "+txt);
                if(txt.contains(text)){
                    System.out.println(txt+"\n");
                    String temp[] = txt.split(" ");
                    lst.add(temp[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lst;
    }

    /**
     * Register the file with Index Server : appends the file to index list on server
     * @param data, file name
     * @return append was success or failure
     */
    public static boolean appendDataToFile(String data) {
        BufferedWriter bw = null;
        FileWriter fw = null;

        try {

            File file = new File(Constants.INDEX_FILE_NAME);
            createFolder(Constants.INDEX_FILE_NAME.split("/")[0]);

            // if file doesnt exists, then create itportRequested
            if (!file.exists()) {
                file.createNewFile();
            }

            // true = append file
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);

            bw.write(data);
            return true;

        } catch (IOException e) {

            e.printStackTrace();
            return false;

        } finally {
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

    public static void DeleteSingleLineInFile(String fileName, String portNumber) {

        try {

            File inputFile = new File(Constants.INDEX_FILE_NAME);
            File tempFile = new File(Constants.TEMP_FILE_NAME);

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            String lineToRemove = fileName + " " + portNumber;
            String currentLine;

            while ((currentLine = reader.readLine()) != null) {
                // trim newline when comparing with lineToRemove
                String trimmedLine = currentLine.trim();
                if (trimmedLine.equals(lineToRemove))
                    continue;
                writer.write(currentLine + System.getProperty("line.separator"));
            }
            writer.close();
            reader.close();
            if (inputFile.delete())
                tempFile.renameTo(inputFile);

            //return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a folder is not already existing
     * @param rootDirName, path of the directory
     */
    public static void createFolder(String rootDirName){
        try {
            File file = new File(rootDirName);
            if (!file.exists()) {
                if (file.mkdir()) {
                } else {
                    System.out.println("Failed to create directory!");
                }
            }
        }catch ( Exception e ){
            e.printStackTrace();
        }

    }

    /**
     * Opens the file, and writes the contents to a socket output stream
     * @param filename, file name to download
     * @param socket, socket to which the file contents has to be written
     */
    public static void downloadFile(String filename,Socket socket){
        File f = new File(filename);
        try(
                InputStream fip = new FileInputStream(f);
                OutputStream out = socket.getOutputStream();
        )
        {
            //int content = 0;
            byte b[] = new byte[16 * 1024];
            int count;
            while ((count = fip.read(b)) > 0) {
                out.write(b, 0, count);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Prints the file contents
     * @param filePath, file to be printed out
     */
    public static  void printFile(String filePath){

        try(
                BufferedReader br = new BufferedReader(new FileReader(filePath));
        )
        {
            int lineCount = 0;
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                ++lineCount;
                if(lineCount > 10) return;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

}