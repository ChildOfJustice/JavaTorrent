package com.example.multimodule.application.handler;

//import com.example.multimodule.application.data.Client;
//import com.example.multimodule.application.data.Pack;
import com.example.multimodule.application.data.Client;
import com.example.multimodule.application.data.Pack;
import com.example.multimodule.application.handler.model.DataService;
//import com.example.multimodule.data.Client;
//import com.example.multimodule.data.Pack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
public class CenterController {
    Logger logger = LoggerFactory.getLogger(CenterController.class);
    DataService dataService;

    @Autowired
    public CenterController(DataService dataService) {
        this.dataService = dataService;
    }

    //    @GetMapping("/files")
//    public String getAllFiles(Model model) throws IOException {
//
//        model.addAttribute("files", storageService.loadAll().map(
//                        path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
//                                "serveFile", path.getFileName().toString()).build().toUri().toString())
//                .collect(Collectors.toList()));
//
//        return "uploadForm";
//    }
    @GetMapping("/packs")
    @ResponseBody
    public ResponseEntity<ArrayList<Pack>> getAllPacks() {
        logger.info("Got a GET all packs request");
        ArrayList<Pack> packs = dataService.getAllPacks();
        logger.info("Packs on the center: " + packs.size());
        return ResponseEntity.ok(packs);
    }

    @GetMapping("/packs/{fileId:.+}")
    @ResponseBody
    public ResponseEntity<ArrayList<Pack>> getPackInfoByFileId(@PathVariable String fileId) {
        logger.info("Got a GET packs for specified file request");
        ArrayList<Pack> packs = dataService.findAllPackForFile(fileId);
        logger.info("IP0:" + packs.get(0).getOwnerClientIp());
        if (packs.size() == 0)
            return ResponseEntity.notFound().build();
        else
            return ResponseEntity.ok(packs);
    }

    @PostMapping("/packs")
    public ResponseEntity addInfoAboutPack(@RequestBody Pack pack) {
        logger.error("Got a POST new pack request");

//        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
//                .path("/{id}")
//                .buildAndExpand(pack.id)
//                .toUri();
        dataService.addPack(pack);

        return ResponseEntity.ok(HttpStatus.OK);

//        redirectAttributes.addFlashAttribute("message",
//                "You successfully uploaded " + file.getOriginalFilename() + "!");
//
//        return "redirect:/";
    }

    @PostMapping("/clients")
    public ResponseEntity registerClient(@RequestBody Client client) {
        logger.error("Got a POST new client request");

//        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
//                .path("/{id}")
//                .buildAndExpand(pack.id)
//                .toUri();
        dataService.registerClient(client);

        return ResponseEntity.ok(HttpStatus.OK);

//        redirectAttributes.addFlashAttribute("message",
//                "You successfully uploaded " + file.getOriginalFilename() + "!");
//
//        return "redirect:/";
    }

//    @ExceptionHandler(StorageFileNotFoundException.class)
//    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
//        return ResponseEntity.notFound().build();
//    }

}