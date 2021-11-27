package com.example.multimodule.application.handler.model;

//import com.example.multimodule.application.data.Pack;
//import com.example.multimodule.data.Pack;
import com.example.multimodule.application.data.Pack;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;

@Component
public class ClientPackIds {
    HashMap<String, String> data;

    public ClientPackIds(){
        data = new HashMap<>();
    }

    public void addPack(Pack pack){
        data.put(pack.id, pack.ownerClientId);
    }

    public void addClientIdToPacks(ArrayList<Pack> packs) {
        for (Pack pack:packs) {
            pack.ownerClientId = data.get(pack.id);
        }
    }
}
