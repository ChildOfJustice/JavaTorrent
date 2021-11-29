package com.example;

import com.example.core.RestClient;
import com.example.core.data.Pack;
import com.example.storage.FileSystemStorageService;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
public class FileManager {
    Logger logger = LoggerFactory.getLogger(FileManager.class);

    ExecutorService executorService;

    private FileSystemStorageService storageService;

    @Autowired
    public FileManager(FileSystemStorageService storageService){
        this.storageService = storageService;
    }

    public FileSystemStorageService getStorageService(){
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

                        storageService.store(fileBytes, storageService.joinPackId_FileId_PackNumber(pack.getId(), pack.getFileId(), pack.getNumber()));

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
        try {
            if (!executorService.awaitTermination(60*3, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ex) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        //TODO all downloaded packs were with number 0!!!
        storageService.constructFilesFromPacks(filePacks.get(0).getFileId());

        errors.forEach(
                (k, v) -> logger.error("PACK " + k + " FAILED: " + v)
        );
    }
}
