/*
 * Copyright (C) 2017.  FileSharingSystem - https://github.com/ajayramesh23/FileSharingSystem
 * Programming Assignment from Professor Z.Lan
 * @author Ajay Ramesh
 * @author Chandra Kumar Basavaraj
 * Last Modified - 3/17/17 8:39 PM
 */

package cs550.pa3.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import cs550.pa3.helpers.Host;
import cs550.pa3.helpers.PeerFile;
import cs550.pa3.helpers.PeerFiles;
import cs550.pa3.helpers.Util;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JsonPeerFileProcessorTest {
    public static void main(String[] args) throws Exception {
        HashMap<String, PeerFile> files = new HashMap<String, PeerFile>();//changed here
        PeerFile file = new PeerFile(false,"mountain.jpg",1,new Host("localhost",9999),0);
        files.put("mountain.jpg",file);//changed here
        file = new PeerFile(false,"mountain2.jpg",1, new Host("localhost",9999),0);
        files.put("mountain2.jpg",file);//changed here
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String json = mapper.writeValueAsString(files);
        //System.out.println(json);
        String content = new String(Files.readAllBytes(Paths.get("PeerDownloads/Cache/files.metadata.json")));
        // Util.error(content);
        PeerFile newFile = mapper.readValue(content, PeerFile.class);
        System.out.println(newFile.getLastUpdated().toString());
        PeerFiles p = new PeerFiles(files);
        json = mapper.writeValueAsString(p);
        System.out.println(json);
        content = new String(Files.readAllBytes(Paths.get("PeerDownloads/Master/files.metadata.json")));
        PeerFiles fs = mapper.readValue(content, PeerFiles.class);
        /**
         *  Converts the JSON Array list object to string
         */
        Util.print(Util.getJson(p));


        /**
         *  Converts the  string to JSON Array list object
         */
        PeerFiles fileList = (PeerFiles) Util.toObjectFromJson(content,PeerFiles.class);

        for(PeerFile f : fileList.getFilesMetaData().values()){//changed here
            Util.print("f"+f.getName());
        }
    }




}

