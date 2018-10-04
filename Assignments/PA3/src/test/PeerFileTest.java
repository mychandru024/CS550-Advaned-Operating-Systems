/*
 * Copyright (C) 2017.  FileSharingSystem - https://github.com/ajayramesh23/FileSharingSystem
 * Programming Assignment from Professor Z.Lan
 * @author Ajay Ramesh
 * @author Chandra Kumar Basavaraj
 * Last Modified - 3/16/17 2:08 PM
 */

package cs550.pa3.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cs550.pa3.helpers.Host;
import cs550.pa3.helpers.PeerFile;
import cs550.pa3.helpers.Util;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public  class PeerFileTest {

    PeerFile dummyMasterFile;
    PeerFile dummyCachedFile;



    @Before
    public void setUp() throws Exception {
        dummyMasterFile = new PeerFile(true,"foo-original.txt");
        dummyCachedFile = new PeerFile(false,"foo-cache.txt", 10, new Host("localhost",5555),0);
    }

    @After
    public void tearDown() throws Exception {
        dummyMasterFile = null;
        dummyCachedFile = null;
    }

    @Test
    public  void fileExpiredTest1() {
        assertEquals(false,dummyCachedFile.fileExpired());
        Util.sleep(5);
        assertEquals(false, dummyCachedFile.fileExpired());
        Util.sleep(10);
        assertEquals(true, dummyCachedFile.fileExpired());
    }

}