/*
 * Copyright (C) 2017.  FileSharingSystem - https://github.com/ajayramesh23/FileSharingSystem
 * Programming Assignment from Professor Z.Lan
 * @author Ajay Ramesh
 * @author Chandra Kumar Basavaraj
 * Last Modified - 3/15/17 6:44 PM
 */

/**
 * File Name : WatcherThread.java
 * Description : implementation of WatcherThread to monitor a folder of the client for addition/deletion of file
 *                if there is an addition/deletion, sends a notification to Index Server to update the registered files
 * @authors : Ajay Ramesh and Chandra Kumar Basavaraju
 * version : 1.0
 * @date : 01/28/2017
 */
package cs550.pa3.processor;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import cs550.pa3.helpers.Util;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ajay on 1/28/17.
 */
class WatcherThread extends Thread{

    PeerImpl observer;
    String folderName;
    private WatchService watcher;
    private Map<WatchKey, Path> keys;

    /*
    public WatcherThread(){
        try {
            this.observer = null;
            this.folderName = null;
            this.watcher = FileSystems.getDefault().newWatchService();
            this.keys = new HashMap<WatchKey, Path>();
            Path dir = Paths.get(Constants.PEER_FOLDER_PREFIX + this.peerServerPort);
            registerDirectory(dir);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    */
    public WatcherThread(PeerImpl observer, String folderName ){
        Util.print("Watching this folder "+folderName);
        try {
            this.observer = observer;
            this.folderName = folderName;
            this.watcher = FileSystems.getDefault().newWatchService();
            this.keys = new HashMap<WatchKey, Path>();
            Path dir = Paths.get(this.folderName);//this peer's directory
            registerDirectory(dir);//register watcher to monitor peer's directory
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    //register watcher to monitor peer's directory
    //@param : Absolute path of the peer's directory
    private void registerDirectory(Path dir) throws IOException {

        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
    }

    //monitor the directory forever
    public void run(){
        for (;;) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                @SuppressWarnings("rawtypes")
                WatchEvent.Kind kind = event.kind();

                // Context for directory entry event is the file name of entry
                @SuppressWarnings("unchecked")
                Path name = ((WatchEvent<Path>)event).context();
                //Path child = dir.resolve(name);

                //Socket sock = null;
                String changedFileName = name.toString();
                // if directory is created, and watching recursively, then register it and its sub-directories
                if( kind == ENTRY_CREATE || kind == ENTRY_MODIFY || kind == ENTRY_DELETE ) {
                    String fileName = name.toString();
                    if (!fileName.contains("_jb_")) {
                        Util.print("Forwarding create/modify/delete event");
                        observer.handleWatcherThreadEvents(kind.toString(), fileName);
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }
}