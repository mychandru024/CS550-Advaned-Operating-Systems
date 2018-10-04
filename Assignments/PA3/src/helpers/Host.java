/*
 * Copyright (C) 2017.  FileSharingSystem - https://github.com/ajayramesh23/FileSharingSystem
 * Programming Assignment from Professor Z.Lan
 * @author Ajay Ramesh
 * @author Chandra Kumar Basavaraj
 * Last Modified - 3/15/17 6:44 PM
 */

package cs550.pa3.helpers;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Host {
    private String url;
    private int port;

    public Host() {
    }

    public Host(String url, int port) {
        this.url = url;
        this.port = port;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String address() {
        return url+":"+port;
    }


    @JsonIgnore
    public String getHashCode(){
        long hash = (url+""+port).hashCode();
        if(hash<0) return "N"+Math.abs(hash);
        else return "P"+Math.abs(hash);
    }


}