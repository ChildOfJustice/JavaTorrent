package com.example;

import com.example.core.RestClient;
import com.example.multimodule.data.Pack;
import com.example.storage.StorageService;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Component
public class FileManager {
    Logger logger = LoggerFactory.getLogger(FileManager.class);

    ExecutorService executorService = Executors.newCachedThreadPool();

    private StorageService storageService;

    @Autowired
    public FileManager(StorageService storageService){
        this.storageService = storageService;
    }

    public StorageService getStorageService(){
        return storageService;
    }

    public void downloadFile(ArrayList<Pack> filePacks, Consumer<Pack> callBack){
        for (Pack pack: filePacks) {
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    Gson gson = new Gson();
                    logger.info("Starting downloading the pack: " + gson.toJson(pack));
//
                    RestClient restClient = new RestClient(pack.getOwnerClientIp());
                    Resource file = restClient.getResource("packs/"+pack.getId());
                    logger.info("Pack file: " + file.getFilename());
                    storageService.store(file);

                    callBack.accept(pack);
                }
            };

            executorService.submit(task);
        }
        //TODO WAIT FOR ALL
        //executorService.
    }
}
