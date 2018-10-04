/*
 * Copyright (C) 2017.  FileSharingSystem - https://github.com/ajayramesh23/FileSharingSystem
 * Programming Assignment from Professor Z.Lan
 * @author Ajay Ramesh
 * @author Chandra Kumar Basavaraj
 * Last Modified - 3/16/17 2:37 PM
 */

package cs550.pa3.test;

import cs550.pa3.ApplicationRunner;
import org.junit.After;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ApplicationRunnerTest {
    ApplicationRunner runner;
    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void main() throws Exception {

        String input = "n\nlocalhost\n52001\n1";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        new ApplicationRunner();


    }

}