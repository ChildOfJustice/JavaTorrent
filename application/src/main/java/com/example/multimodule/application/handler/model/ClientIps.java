package com.example.multimodule.application.handler.model;

import com.example.multimodule.application.data.Client;
import com.example.multimodule.application.data.Pack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;

@Component
public class ClientIps {
    Logger logger = LoggerFactory.getLogger(ClientIps.class);

    HashMap<String, String> data;

    public ClientIps(){
        data = new HashMap<>();
    }

    public void addClient(Client client){
        data.put(client.getId(), client.getIp());
    }

    public String getClientIp(String clientId){
        return data.get(clientId);
    }

    public void addOwnerIpToPacks(ArrayList<Pack> packs) {
        logger.info("Registered clients: " + data.size());
        data.forEach((k, v) -> logger.info("Client ID: " + k + " Client IP: " + v));
        for (Pack pack:packs) {
            logger.info("ADDING owner ip address for pack from client with id" + pack.getOwnerClientId() + ", ip: " + data.get(pack.getOwnerClientId()));
            pack.setOwnerClientIp(data.get(pack.getOwnerClientId()));
        }
    }
}
