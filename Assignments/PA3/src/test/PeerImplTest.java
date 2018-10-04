/*
 * Copyright (C) 2017.  FileSharingSystem - https://github.com/ajayramesh23/FileSharingSystem
 * Programming Assignment from Professor Z.Lan
 * @author Ajay Ramesh
 * @author Chandra Kumar Basavaraj
 * Last Modified - 3/17/17 8:10 PM
 */

package cs550.pa3.test;

import cs550.pa3.processor.PeerImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Ajay on 3/17/17.
 */
public class PeerImplTest {
    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void runPullProcess() throws Exception {

    }

    @Test
    public void pullFile() throws Exception {
        PeerImpl peer = new PeerImpl();
        //changed here
        //peer.getDownloadedFiles().add(new PeerFile(false,"stars.jpg",3,new Host("localhost",9999),0));
        peer.runPullProcess();

    }

}