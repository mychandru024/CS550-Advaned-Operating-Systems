package cs550.pa2;

//import java.util.Scanner;

import cs550.pa2.helpers.Constants;

import java.util.Scanner;

/**
 * Created by Ajay on 2/24/17.
 */
public class MainPa2 {

    PeerImpl peer = new PeerImpl();


    public static void main(String[] args) {
        // This will run programming assignment 2
        new MainPa2();
    }

    public MainPa2() {

		Scanner in = new Scanner(System.in);
        System.out.print("Default config ? (yes/no) or (y/n): ");
        String choice = in.next();

        if(choice.equalsIgnoreCase("yes") ||
                choice.equalsIgnoreCase("y") ){
            peer.initConfig(Constants.DEFAULT_SERVER_HOST,Constants.DEFAULT_SERVER_PORT);
        }else{
            System.out.println("Enter Host Name Example: 'localhost' or 127.0.0.1 ");
            String hostName = in.next();
            System.out.println("Port address  : ");
            int port = in.nextInt();
			peer.initConfig(hostName,port);
        }
	}
}
