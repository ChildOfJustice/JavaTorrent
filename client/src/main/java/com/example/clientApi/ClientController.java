package com.example.clientApi;

import com.example.FileManager;
import com.example.core.RestClient;
//import com.example.multimodule.data.Pack;
import com.example.core.data.Pack;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.UUID;

@RestController
@PropertySource("classpath:clientApplication.properties")
public class ClientController {
    Logger logger = LoggerFactory.getLogger(ClientController.class);

    @Value( "${center.url}" )
    private String centerUrl;

    @Value( "${thisClient.url}" )
    private String thisClientUrl;

    private RestClient restClient;
    private final String clientId = UUID.randomUUID().toString();
    private FileManager fileManager;

    //Here we're trying to access an autowired field in the constructor. When the constructor is called, the Spring bean is not yet fully initialized. This is a problem because calling fields that are not yet initialized will result in NullPointerExceptions.
    @Autowired
    public ClientController(FileManager fileManager){
        this.fileManager = fileManager;
//        restClient = new RestClient(centerUrl);
//
//        restClient.post("/clients", "{id:\"" + clientId + "\", \"ip\":\"" + thisClientUrl + "\"}");
    }

    @PostConstruct
    public void init() {
        logger.info("Client controller has been created, registering on the server: " + centerUrl);

        restClient = new RestClient(centerUrl);
        restClient.post("/clients", "{\"id\":\"" + clientId + "\", \"ip\":\"" + thisClientUrl + "\"}");

        //TODO pushing the info about existing packs

        fileManager.getStorageService().loadAll().forEach(
                path -> {
                    logger.info("Found file in the storage: " + path.getFileName());
                    String[] packId_fileId = path.getFileName().toString().split("\\|");
                    restClient.post("/packs", "{\"id\":\"" + packId_fileId[0] + "\", \"fileId\":\"" + packId_fileId[1] + "\", \"ownerClientId\":\"" + clientId + "\"}");
                }
        );
    }


    @GetMapping("/files/{fileId:.+}")
    @ResponseBody
    public ResponseEntity downloadFile(@PathVariable String fileId) {
        logger.info("Got a GET download file request");

        String packsArrayJson = restClient.get("/packs/" + fileId);

        Gson gson = new Gson();

        Type userListType = new TypeToken<ArrayList<Pack>>(){}.getType();

        ArrayList<Pack> filePacks = gson.fromJson(packsArrayJson, userListType);

        fileManager.downloadFile(filePacks, this::pushInfoAboutDownloadedPack);

        //logger.info("Info about file packs: " + answer);

        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/packs/{packId:.+}")
    @ResponseBody
    public byte[] getPack(@PathVariable String packId) {
        logger.info("Got a GET download a PACK request");
        Resource fileResource = fileManager.getStorageService().loadPackFileAsResource(packId);
        //return ResponseEntity.ok().body(file).;

        try {
            File file =  fileResource.getFile();
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void pushInfoAboutDownloadedPack(Pack pack){
        Gson gson = new Gson();
        restClient.post("/packs", gson.toJson(pack));
    }

}
