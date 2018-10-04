/**
 * File Name : Util.java
 * Description : Implementation of all utility functions
 * @authors : Ajay Ramesh and Chandra Kumar Basavaraju
 * version : 1.0
 * @date : 01/28/2017
 */
package cs550.pa2.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Ajay on 1/25/17.
 */
public  class Util {



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


    public static void downloadFile(String filePath,Socket socket){
        File f = new File(filePath);
        try(
                InputStream fip = new FileInputStream(f);
                OutputStream out = socket.getOutputStream();
        )
        {   //int content = 0;
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




    public static boolean searchInMyFileDB(String folderSuffix, String fileName) {
        boolean isFound = false;
        File file = new File("sharedFolder" + folderSuffix + "/" + fileName);
        if (file.exists()) {
            isFound = true;
        }
        return isFound;
    }
}