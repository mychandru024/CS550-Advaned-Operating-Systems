/**
 * Main java
 * Starts Index Server or Peer based on the user selection
 */

package cs550.pa1.mains;

import java.util.Scanner;



public class Main {
    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        System.out.println("***************************************");
        System.out.println("1. Run Index Server \n2. Run Peer");
        System.out.print("Enter your choice (1/2) ? : ");
        Scanner in = new Scanner(System.in);
        switch (in.nextInt()){
            case 1: RunIndexMain.main();break;
            case 2: RunPeerMain.main();break;
            default: break;
        }
    }
}
