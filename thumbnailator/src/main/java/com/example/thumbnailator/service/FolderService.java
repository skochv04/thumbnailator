package com.example.thumbnailator.service;

import com.example.thumbnailator.model.Folder;
import com.example.thumbnailator.model.Image;
import com.example.thumbnailator.repository.FolderRepository;
import com.example.thumbnailator.repository.ImageRepository;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FolderService {
    private static final String ROOT_PATH = "root";
    private final FolderRepository folderRepository;
    private final ImageRepository imageRepository;
    private final Logger logger = LoggerFactory.getLogger(FolderService.class);

    public FolderService(FolderRepository folderRepository, ImageRepository imageRepository) {
        this.folderRepository = folderRepository;
        this.imageRepository = imageRepository;
    }

    private static String getSubfolderPath(String name, Folder parent) {
        return parent.getPath() + "/" + name;
    }

    @Transactional
    public Folder createFolder(String name, Folder parent) {
        Folder folder = new Folder(parent, getSubfolderPath(name, parent));
        logger.info("Created folder: {} on path {}", name, folder.getPath());
        return folderRepository.save(folder);
    }

    @Transactional(readOnly = true)
    public Folder getFolderByPath(String path) {
        return folderRepository.findByPath(path).orElseThrow(() -> new RuntimeException("Folder " + path + " not found"));
    }

    @Transactional
    public void createRootFolder() {
        Optional<Folder> rootFolderOptional = folderRepository.findByPath(ROOT_PATH);

        if (rootFolderOptional.isEmpty()) {
            Folder rootFolder = new Folder(null, ROOT_PATH);
            folderRepository.save(rootFolder);
            logger.info("Root folder created at path: {}", ROOT_PATH);
        }
    }

    @Transactional
    public Folder getRootFolder() {
        Optional<Folder> rootFolderOptional = folderRepository.findByPath(ROOT_PATH);

        return rootFolderOptional.orElse(null);
    }

    @Transactional
    public void createFolderAndAllParentFolders(String path) {
        Optional<Folder> folder = folderRepository.findByPath(path);
        if (folder.isPresent()) {
            return;
        }
        List<String> folderNames = List.of(path.split("/"));
        Folder parentFolder = getRootFolder();
        Folder currentFolder;

        for (String name : folderNames) {
            String currentPath = getSubfolderPath(name, parentFolder);
            Optional<Folder> currentFolderOptional = folderRepository.findByPath(currentPath);
            if (currentFolderOptional.isEmpty()) {
                currentFolder = new Folder(parentFolder, currentPath);
                logger.info("Created folder: {} on path {}", name, parentFolder.getPath());
                folderRepository.save(currentFolder);
            } else {
                currentFolder = currentFolderOptional.get();
            }
            parentFolder = currentFolder;
        }
    }

    @Transactional(readOnly = true)
    public List<Folder> getSubfolders(Folder parent) {
        return folderRepository.findFoldersByParent(parent);
    }

    @Transactional(readOnly = true)
    public boolean folderExists(String name, Folder parent) {
        return folderRepository.findByPath(getSubfolderPath(name, parent)).isPresent();
    }

    @Transactional
    public void deleteFolder(UUID folderId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found with id: " + folderId));
        logger.info("Deleting folder {} and all its content.", folder.getPath());
        folderRepository.delete(folder);
        List<Image> orphanImages = imageRepository.findOrphanImages();
        imageRepository.deleteAll(orphanImages);
    }

    @Transactional(readOnly = true)
    public Folder findFolder(String folderPath) {
        return folderRepository.findByPath(folderPath).or(() -> folderRepository.findByPath(getSubfolderPath(folderPath, getRootFolder()))).orElseGet(this::getRootFolder);
    }
}