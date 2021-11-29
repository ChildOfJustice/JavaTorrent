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
    PackIdPackNumber packIdPackNumber;
    ClientIps clientIps;

    @Autowired
    public DataService(ClientPackIds clientPackIds, PackFileIds packFileIds, PackIdPackNumber packIdPackNumber, ClientIps clientIps) {
        this.clientPackIds = clientPackIds;
        this.packFileIds = packFileIds;
        this.clientIps = clientIps;
        this.packIdPackNumber = packIdPackNumber;
    }

    public void addPack(Pack pack){
        packFileIds.addPackIfNotExists(pack);
        clientPackIds.addPack(pack);
        packIdPackNumber.addPackNumber(pack);
    }

    public ArrayList<Pack> findAllPackForFile(String fileId){
        ArrayList<Pack> aggregatedPacks = packFileIds.findAllFilePacks(fileId);
        clientPackIds.addClientIdToPacks(aggregatedPacks);
        packIdPackNumber.addNumberToPacks(aggregatedPacks);
        clientIps.addOwnerIpToPacks(aggregatedPacks);
        return aggregatedPacks;
    }

    public ArrayList<Pack> getAllPacks(){
        return packFileIds.getAllPacks();
    }

    public void registerClient(Client client) {
        clientIps.addClient(client);
        logger.info("Registered a new client with id: " + client.getId() + " and ip: " + client.getIp());
    }
}
