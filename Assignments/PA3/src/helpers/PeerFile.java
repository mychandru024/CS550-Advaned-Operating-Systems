/*
 * Copyright (C) 2017.  FileSharingSystem - https://github.com/ajayramesh23/FileSharingSystem
 * Programming Assignment from Professor Z.Lan
 * @author Ajay Ramesh
 * @author Chandra Kumar Basavaraj
 * Last Modified - 3/15/17 6:44 PM
 */

package cs550.pa3.helpers;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;

public class PeerFile {
    int version;
    boolean original=true;
    String name;
    int TTR;
    Host fromAddress;
    boolean isStale;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    LocalDateTime lastUpdated;

    public PeerFile() {
    }

    public PeerFile(boolean original, String name) {
        this.original = original;
        this.name = name;
        this.TTR = -1;
        this.fromAddress = null; // TODO change to make peer server address .
        this.lastUpdated = LocalDateTime.now();

    }

    public PeerFile(int version, boolean original, String name, int TTR,
        Host fromAddress, boolean isStale, LocalDateTime lastUpdated) {
        this.version = version;
        this.original = original;
        this.name = name;
        this.TTR = TTR;
        this.fromAddress = fromAddress;
        this.isStale = isStale;
        this.lastUpdated = lastUpdated;
    }

    public PeerFile(boolean original, String name, int TTR, Host address, int version) {
        this.original = original;
        this.name = name;
        this.TTR = TTR;
        this.fromAddress = address;
        this.lastUpdated = LocalDateTime.now();
        this.version = version;
        this.isStale = false;
    }

    public boolean isOriginal() {
        return original;
    }

    public String getName() {
        return name;
    }

    public int getTTR() {
        return TTR;
    }

    public Host getFromAddress() {
        return fromAddress;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public boolean fileExpired(){
        if(!original){
            return LocalDateTime.now().isAfter(lastUpdated.plusSeconds(TTR));
        }else{
            return false;
        }

    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setOriginal(boolean original) {
        this.original = original;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTTR(int TTR) {
        this.TTR = TTR;
    }

    public void setFromAddress(Host fromAddress) {
        this.fromAddress = fromAddress;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isStale(){
        return isStale;
    }
    public void setIsStale(boolean isStale){
        this.isStale = isStale;
    }

    public void setStale(boolean stale) {
        isStale = stale;
    }
}
