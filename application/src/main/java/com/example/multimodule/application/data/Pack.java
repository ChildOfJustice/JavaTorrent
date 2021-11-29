package com.example.multimodule.application.data;

public class Pack {

    public Pack(String id, String fileId) {
        this.id = id;
        this.fileId = fileId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getOwnerClientId() {
        return ownerClientId;
    }

    public void setOwnerClientId(String ownerClientId) {
        this.ownerClientId = ownerClientId;
    }

    public String getOwnerClientIp() {
        return ownerClientIp;
    }

    public void setOwnerClientIp(String ownerClientIp) {
        this.ownerClientIp = ownerClientIp;
    }


    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public String id;
    public long number;
    public String fileId;
    public String ownerClientId;
    public String ownerClientIp;
}
