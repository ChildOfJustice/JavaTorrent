package com.example;

import com.example.core.RestClient;
//import com.example.multimodule.data.Pack;
import com.example.core.data.Pack;
import com.example.storage.StorageService;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Component
public class FileManager {
    Logger logger = LoggerFactory.getLogger(FileManager.class);

    ExecutorService executorService;

    private StorageService storageService;

    @Autowired
    public FileManager(StorageService storageService){
        this.storageService = storageService;
    }

    public StorageService getStorageService(){
        return storageService;
    }

    public void downloadFile(ArrayList<Pack> filePacks, Consumer<Pack> callBack){
        executorService = Executors.newCachedThreadPool();
        ConcurrentHashMap<String, String> errors = new ConcurrentHashMap<>();
        for (Pack pack: filePacks) {
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    try {
                        Gson gson = new Gson();
                        logger.info("Starting downloading the pack: " + gson.toJson(pack));
//
                        RestClient restClient = new RestClient(pack.getOwnerClientIp());
                        byte[] fileBytes = restClient.getBytes("/packs/" + pack.getId());
                        logger.info("Pack file length: " + fileBytes.length);

                        storageService.store(fileBytes, joinPackIdAndFileId(pack.getId(), pack.getFileId()));

                        callBack.accept(pack);
                    } catch (Exception e){
                        errors.put(pack.getId(), e.getLocalizedMessage());
                    }
                }
            };

            executorService.submit(task);
        }
        //TODO WAIT FOR ALL
        executorService.shutdown();
        errors.forEach(
                (k, v) -> logger.error("PACK " + k + " FAILED: " + v)
        );

    }

    public String joinPackIdAndFileId(String packId, String fileId){
        return packId + "|" + fileId;
    }
}
