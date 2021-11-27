package com.example.multimodule.application.handler.model;

//import com.example.multimodule.application.data.Pack;
//import com.example.multimodule.data.Pack;
import com.example.multimodule.application.data.Pack;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;

@Component
public class PackFileIds {
    HashMap<String, String> data;

    public PackFileIds(){
        data = new HashMap<>();
    }

    public void addPackIfNotExists(Pack pack){
        data.putIfAbsent(pack.id, pack.fileId);
    }

    public ArrayList<Pack> findAllFilePacks(String fileId) {
        ArrayList<Pack> packs = new ArrayList<>();
        data.forEach((_packId, _fileId) -> {
            if (_fileId.equals(fileId)){
                packs.add(new Pack(_packId, _fileId));
            }
        });
        return packs;
    }

    public ArrayList<Pack> getAllPacks() {
        ArrayList<Pack> packs = new ArrayList<>();
        data.forEach((_packId, _fileId) -> {

            packs.add(new Pack(_packId, _fileId));

        });
        return packs;
    }
}
