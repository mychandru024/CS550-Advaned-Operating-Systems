/*
 * Copyright (C) 2017.  FileSharingSystem - https://github.com/ajayramesh23/FileSharingSystem
 * Programming Assignment from Professor Z.Lan
 * @author Ajay Ramesh
 * @author Chandra Kumar Basavaraj
 * Last Modified - 3/15/17 6:44 PM
 */

/**
 * File Name : Constants.java
 * Description : declaration of all constants used
 * @authors : Ajay Ramesh and Chandra Kumar Basavaraju
 * version : 1.0
 * @date : 01/28/2017
 */
package cs550.pa3.helpers;

/**
 * Created by Ajay on 1/27/17.
 */
/**
 * Created by Ajay on 1/25/17.
 */
public  class Constants {
    public static final String CONFIG_FILE="StarTopologyTest/config.file";
    public static final String DEFAULT_SERVER_HOST ="localhost" ;
    public static final int    DEFAULT_SERVER_PORT =5555;
    public static final String INDEX_FILE_NAME="output/index.txt";
    public static final String TEMP_FILE_NAME="output/myTempFile.txt";
    public static final String QUERY = "query";
    public static final String QUERYHIT = "queryhit";
    public static final String DOWNLOAD = "download";
    public static final String DOWNLOAD_METADATA = "download_metadata";
    public static final String INVALIDATION = "INVALIDATION";
    public static final String PEER_PROPERTIES_FILE = "peer1.properties";
    public static final String MASTER_FOLDER = "master.foldername";
    public static final String CACHED_FOLDER = "cache.foldername";
    public static final String PEER_NEIGHBORS = "peer.neighbors";
    public static final String PULL_TTR = "pull.TTR";
    public static final String CONFIG_FILE_PREFIX = "config-";
    public static final int TTL = 7;
    public static final int ZERO = 0;
    public static final int MINUS_ONE = -1;
    public static final String TEMP_FILE="__temp.json";
    public static final String PULL="pull";
    public static final String POLL="poll";
    public static final String SPACE=" ";


    public static final String DISPLAY_MENU = "1 : Lookup a file\n2 : Download file from a peer\n3 : Display seen query messages\n4 : Display seen queryhit messages\n5 : Display Peer files info\n6 : Refresh a stale file\n7 : Modify one of the files held by me\n8 : Exit\nEnter your choice number";

}
