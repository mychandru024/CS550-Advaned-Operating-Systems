/*
 * Copyright (C) 2017.  FileSharingSystem - https://github.com/ajayramesh23/FileSharingSystem
 * Programming Assignment from Professor Z.Lan
 * @author Ajay Ramesh
 * @author Chandra Kumar Basavaraj
 * Last Modified - 3/15/17 6:44 PM
 */

package cs550.pa3.processor;
import java.io.IOException;


public interface Peer {
    void download(String fileName, String host, int port) throws IOException;

    void search(String message_id, String fileName, int ttl, boolean forward);

    void forwardQuery(String message_id, String fileName, int ttl);

    void returnQueryHit(String msgid, String fileName, String addr, int ttl, boolean forward);

    void forwardQueryHit(String msg_id, String fileName, String addr, int ttl);

    void displayPeerInfo();

    void initConfig(String hostName, int port);

    void handleBroadCastEvents(String msgid, String changedFileName, int newFileVersion, int ttl, boolean isForward, String receivedFrom);

    void runPullProcess();

    void runPeerServer();

    void runPeerClient();
}
