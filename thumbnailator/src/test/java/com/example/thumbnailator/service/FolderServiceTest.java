package com.example.thumbnailator.service;

import com.example.thumbnailator.model.Folder;
import com.example.thumbnailator.repository.FolderRepository;
import com.example.thumbnailator.repository.ImageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FolderServiceTest {
    private static final String ROOT_FOLDER_PATH = "root";
    private static final Folder ROOT_FOLDER = new Folder(null, ROOT_FOLDER_PATH);
    private static final String SUBFOLDER_PATH = "root/subfolder";
    private static final Folder SUBFOLDER = new Folder(ROOT_FOLDER, SUBFOLDER_PATH);
    @Mock
    private FolderRepository folderRepository;
    @Mock
    private ImageRepository imageRepository;
    @InjectMocks
    private FolderService folderService;

    @Test
    void testCreateFolder() {
        // Given
        // When
        when(folderRepository.save(any(Folder.class))).thenReturn(SUBFOLDER);
        Folder createdFolder = folderService.createFolder(SUBFOLDER_PATH, ROOT_FOLDER);

        // Then
        assertNotNull(createdFolder);
        assertEquals(SUBFOLDER_PATH, createdFolder.getPath());
        verify(folderRepository, times(1)).save(any(Folder.class));
    }

    @Test
    void testGetFolderByPath() {
        // Given
        // When
        when(folderRepository.findByPath(SUBFOLDER_PATH)).thenReturn(Optional.of(SUBFOLDER));
        Folder retrievedFolder = folderService.getFolderByPath(SUBFOLDER_PATH);

        // Then
        assertNotNull(retrievedFolder);
        assertEquals(SUBFOLDER_PATH, retrievedFolder.getPath());
        verify(folderRepository, times(1)).findByPath(SUBFOLDER_PATH);
    }

    @Test
    void shouldCreateRootFolder() {
        // Given
        // When
        when(folderRepository.findByPath(ROOT_FOLDER_PATH)).thenReturn(Optional.empty());
        when(folderRepository.save(any(Folder.class))).thenReturn(ROOT_FOLDER);
        when(folderRepository.findByPath(ROOT_FOLDER_PATH)).thenReturn(Optional.of(ROOT_FOLDER));
        folderService.createFolder(ROOT_FOLDER_PATH, ROOT_FOLDER);
        Folder retrievedRootFolder = folderService.getRootFolder();

        // Then
        assertNotNull(retrievedRootFolder);
        assertEquals(ROOT_FOLDER_PATH, retrievedRootFolder.getPath());
        verify(folderRepository, times(1)).save(any(Folder.class));
    }

    @Test
    void testGetRootFolderWhenRootExists() {
        // Given
        // When
        when(folderRepository.findByPath(ROOT_FOLDER_PATH)).thenReturn(Optional.of(ROOT_FOLDER));
        Folder retrievedRootFolder = folderService.getRootFolder();

        // Then
        assertNotNull(retrievedRootFolder);
        assertEquals(ROOT_FOLDER_PATH, retrievedRootFolder.getPath());
        verify(folderRepository, never()).save(any(Folder.class));
    }

    @Test
    void testCreateFolderAndAllParentFolders() {
        // Given
        // When
        when(folderRepository.findByPath(SUBFOLDER_PATH))
                .thenReturn(Optional.empty());
        when(folderRepository.findByPath(ROOT_FOLDER_PATH))
                .thenReturn(Optional.of(ROOT_FOLDER));
        folderService.createFolderAndAllParentFolders(SUBFOLDER_PATH);

        // Then
        verify(folderRepository, times(SUBFOLDER_PATH.split("/").length)).save(any(Folder.class));
    }

    @Test
    void testGetSubfolders() {
        // Given
        // When
        when(folderRepository.findFoldersByParent(ROOT_FOLDER)).thenReturn(List.of(SUBFOLDER));
        List<Folder> subfolders = folderService.getSubfolders(ROOT_FOLDER);

        // Then
        assertNotNull(subfolders);
        assertEquals(1, subfolders.size());
        assertEquals(SUBFOLDER_PATH, subfolders.getFirst().getPath());
        verify(folderRepository, times(1)).findFoldersByParent(ROOT_FOLDER);
    }

    @Test
    void testDeleteFolder() {
        // Given
        UUID folderId = UUID.randomUUID();
        // When
        when(folderRepository.findById(folderId)).thenReturn(Optional.of(SUBFOLDER));
        when(imageRepository.findOrphanImages()).thenReturn(Collections.emptyList());
        folderService.deleteFolder(folderId);

        // Then
        verify(folderRepository, times(1)).delete(SUBFOLDER);
        verify(imageRepository, times(1)).deleteAll(Collections.emptyList());
    }

    @Test
    void testFindFolder() {
        // Given
        // When
        when(folderRepository.findByPath(SUBFOLDER_PATH)).thenReturn(Optional.of(SUBFOLDER));
        Folder foundFolder = folderService.findFolder(SUBFOLDER_PATH);

        // Then
        assertNotNull(foundFolder);
        assertEquals(SUBFOLDER_PATH, foundFolder.getPath());
        verify(folderRepository, times(1)).findByPath(SUBFOLDER_PATH);
    }
}
