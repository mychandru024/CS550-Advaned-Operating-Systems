package cs550.pa1.mains;

import cs550.pa1.helpers.Util;
import cs550.pa1.servers.PeerServer.PeerClientImpl;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by Ajay on 1/29/17.
 */
public class RunIndexMainTest {

    int lookUp_N=1000;

    Thread peerClient1_registry;
    Thread peerClient2_registry;
    Thread peerClient3_registry;


    Thread peerClient1_lookup;
    Thread peerClient2_lookup;

    @Before
    public void setUp() throws Exception {
        Util.createFolder("peer_3500");
        Util.createFolder("peer_3600");
        Util.createFolder("peer_3700");
        peerClient1_registry = new Thread () {
            public void run () {

                for(int i=0;i<lookUp_N;i++){
                    try {
                        PeerClientImpl peerClient = new PeerClientImpl("localhost",3500);
                        peerClient.registerFile("test"+i+".txt","localhost:3500");
                    } catch ( IOException e ) {
                        e.printStackTrace();
                    }
                }

            }
        };
        peerClient2_registry = new Thread () {
            public void run () {
                for(int i=0;i<lookUp_N;i++){
                    try {
                        PeerClientImpl peerClient = new PeerClientImpl("localhost",3600);
                        peerClient.registerFile("test"+i+".txt","localhost:3600");
                    } catch ( IOException e ) {
                        e.printStackTrace();
                    }
                }
            }
        };
        peerClient3_registry = new Thread () {
            public void run () {
                for(int i=0;i<lookUp_N;i++){
                    try {
                        PeerClientImpl peerClient = new PeerClientImpl("localhost",3700);
                        peerClient.registerFile("test"+i+".txt","localhost:3700");
                    } catch ( IOException e ) {
                        e.printStackTrace();
                    }
                }
            }
        };




    }

    @Test
    public void indexLookUp() throws Exception {
        PeerClientImpl peerClient = new PeerClientImpl("localhost",3500);
        peerClient.lookupFile("test");

        peerClient1_lookup = new Thread () {
            public void run () {
                for(int i=0;i<500;i++){
                    PeerClientImpl peerClient = new PeerClientImpl("localhost",3500);
                    peerClient.lookupFile("test"+i);
                }

            }
        };

        peerClient2_lookup = new Thread () {
            public void run () {
                for(int i=500;i<1000;i++){
                    PeerClientImpl peerClient = new PeerClientImpl("localhost",3600);
                    peerClient.lookupFile("test"+i);
                }
            }
        };

        peerClient1_lookup.start();
        peerClient2_lookup.start();


    }



    @Test
    public void indexRegistry() throws Exception {
       peerClient1_registry.start();
       peerClient2_registry.start();
       peerClient3_registry.start();
    }
}




