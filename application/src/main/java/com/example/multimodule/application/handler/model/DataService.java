package com.example.multimodule.application.handler.model;

import com.example.multimodule.application.data.Client;
import com.example.multimodule.application.data.Pack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class DataService {
    Logger logger = LoggerFactory.getLogger(DataService.class);

    ClientPackIds clientPackIds;
    PackFileIds packFileIds;
    ClientIps clientIps;

    @Autowired
    public DataService(ClientPackIds clientPackIds, PackFileIds packFileIds, ClientIps clientIps) {
        this.clientPackIds = clientPackIds;
        this.packFileIds = packFileIds;
        this.clientIps = clientIps;
    }

    public void addPack(Pack pack){
        packFileIds.addPackIfNotExists(pack);
        clientPackIds.addPack(pack);
    }

    public ArrayList<Pack> findAllPackForFile(String fileId){
        ArrayList<Pack> packsWithFileId = packFileIds.findAllFilePacks(fileId);
        clientPackIds.addClientIdToPacks(packsWithFileId);
        clientIps.addOwnerIpToPacks(packsWithFileId);
        return packsWithFileId;
    }

    public ArrayList<Pack> getAllPacks(){
        return packFileIds.getAllPacks();
    }

    public void registerClient(Client client) {
        clientIps.addClient(client);
        logger.info("Registered a new client with id: " + client.getId() + " and ip: " + client.getIp());
    }
}
