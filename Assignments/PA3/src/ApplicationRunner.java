/*
 * Copyright (C) 2017.  FileSharingSystem - https://github.com/ajayramesh23/FileSharingSystem
 * Programming Assignment from Professor Z.Lan
 * @author Ajay Ramesh
 * @author Chandra Kumar Basavaraj
 * Last Modified - 3/15/17 6:44 PM
 */

package cs550.pa3;


import cs550.pa3.helpers.Util;
import cs550.pa3.processor.PeerImpl;

import java.util.Scanner;

public class ApplicationRunner {

  public PeerImpl peer = new PeerImpl();

  public ApplicationRunner() {

    Scanner in = new Scanner(System.in);
    System.out.print("Default config ? (yes/no) or (y/n): ");
    String choice = in.next();

    if (choice.equalsIgnoreCase("yes") ||
        choice.equalsIgnoreCase("y")) {
      peer.initConfig(Util.getValue("peer.host", "peer.properties"),
          Integer.parseInt(Util.getValue("peer.port", "peer.properties")));
    } else {
      System.out.println("Enter Host Name Example: 'localhost' or 127.0.0.1 ");
      String hostName = in.next();
      System.out.println("Port address  : ");
      int port = in.nextInt();
      peer.initConfig(hostName, port);
    }
  }


  public ApplicationRunner(String [] args) {
        peer.initConfig(args[0], Integer.parseInt(args[1]));
    }

  public static void main(String[] args) {
    if (args.length > 0) {
      new ApplicationRunner(args);
    } else {
      new ApplicationRunner();
    }
    // TODO - write the data to file metadata-json
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {

      }
    });

  }
}

