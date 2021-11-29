package com.example.multimodule.application.handler.model;

import com.example.multimodule.application.data.Pack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;

@Component
public class PackIdPackNumber {
    Logger logger = LoggerFactory.getLogger(PackIdPackNumber.class);

    HashMap<String, Long> data;

    public PackIdPackNumber(){
        data = new HashMap<>();
    }

    public void addPackNumber(Pack pack){
        logger.info("Adding a new pack with number: " + pack.getNumber());
        data.putIfAbsent(pack.id, pack.getNumber());
    }

    public void addNumberToPacks(ArrayList<Pack> packs) {
        for (Pack pack:packs) {
            pack.setNumber(getPackNumber(pack.getId()));
        }
    }

    public long getPackNumber(String packId){
        return data.get(packId);
    }
}
