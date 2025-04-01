package com.example.thumbnailator.controller;

import com.example.thumbnailator.model.Folder;
import com.example.thumbnailator.service.FolderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/folders")
@CrossOrigin(origins = "*")
public class FolderController {

    private final FolderService folderService;
    private final Logger logger = LoggerFactory.getLogger(FolderController.class);

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @PostMapping("/create")
    public ResponseEntity<Folder> createFolder(@RequestParam String name, @RequestParam String parentPath) {
        Folder parent = folderService.getFolderByPath(parentPath);

        if (parent == null) {
            return ResponseEntity.badRequest().build();
        }

        if (folderService.folderExists(name, parent)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Folder newFolder = folderService.createFolder(name, parent);
        return ResponseEntity.ok(newFolder);
    }

    @GetMapping("/root")
    public Folder getRootFolder() {
        return folderService.getRootFolder();
    }

    @GetMapping
    public Folder getFolderByPath(@RequestParam String path) {
        logger.info("Folder '{}' found", path);
        return folderService.getFolderByPath(path);
    }

    @GetMapping("/subfolders")
    public List<Folder> getSubfolders(@RequestParam String path) {
        Folder parent = folderService.getFolderByPath(path);
        return folderService.getSubfolders(parent);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteFolder(@PathVariable UUID id) {
        try {
            folderService.deleteFolder(id);
            return ResponseEntity.ok("Folder deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error deleting folder: " + e.getMessage());
        }
    }
}
