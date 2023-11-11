package com.valkryst.JPathList;

import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JPathListTest {
    @Test
    public void canCreateJPathList() {
        final var list = new JPathList();
        Assertions.assertEquals(0, list.getPaths().size());
        Assertions.assertTrue(list.isDragAndDropEnabled());
        Assertions.assertEquals(-1, list.getRecursionMode());
    }

    @Test
    public void cannotAddPathWhenPathIsNull() {
        final var list = new JPathList();
        Assertions.assertThrows(NullPointerException.class, () -> {
            list.addPath(null);
        });
    }

    @Test
    public void cannotAddPathWhenPathDoesNotExist() {
        final var list = new JPathList();
        Assertions.assertThrows(FileNotFoundException.class, () -> {
            list.addPath(Paths.get("test"));
        });
    }

    @Test
    public void cannotAddPathWhenPathIsNotReadable() {
        // todo I do not know how to test this. It should occur when a file fails the isReadable check.
    }

    @Test
    public void canAddRegularFilePath() throws IOException {
        final var fileSystem = Jimfs.newFileSystem();
        final var path = fileSystem.getPath("test");
        Files.createFile(path);

        final var list = new JPathList();
        list.addPath(path);

        Assertions.assertEquals(1, list.getPaths().size());
        Assertions.assertEquals(path, list.getPaths().get(0));

        fileSystem.close();
    }

    @Test
    public void cannotAddPathWhenPathIsNeitherDirectoryNorRegularFile() {
        // todo I do not know how to test this. It should occur when a file passes the isRegularFile check and fails
        //      the subsequent isDirectory check.
    }

    @Test
    public void canAddDirectoryPathWhenRecursionModeIsNone() throws IOException {
        final var fileSystem = Jimfs.newFileSystem();
        final var path = fileSystem.getPath("test");
        Files.createDirectory(path);

        final var list = new JPathList();
        list.addPath(path);

        Assertions.assertEquals(1, list.getPaths().size());
        Assertions.assertEquals(path, list.getPaths().get(0));
    }

    @Test
    public void canAddDirectoryPathWhenRecursionModeIsFilesOnly() throws IOException {
        final var fileSystem = Jimfs.newFileSystem();

        final var directoryA = fileSystem.getPath("directoryA");
        final var directoryB = fileSystem.getPath("directoryA/directoryB");
        final var fileA = fileSystem.getPath("fileA");
        final var fileB = fileSystem.getPath("directoryA/fileB");
        final var fileC = fileSystem.getPath("directoryA/directoryB/fileC");
        Files.createDirectory(directoryA);
        Files.createDirectory(directoryB);
        Files.createFile(fileA);
        Files.createFile(fileB);
        Files.createFile(fileC);

        final var list = new JPathList();
        list.setRecursionMode(JFileChooser.FILES_ONLY);
        list.addPath(directoryA);
        list.addPath(directoryB);
        list.addPath(fileA);

        Assertions.assertEquals(3, list.getPaths().size());
        Assertions.assertTrue(list.getPaths().contains(fileA));
        Assertions.assertTrue(list.getPaths().contains(fileB));
        Assertions.assertTrue(list.getPaths().contains(fileC));
    }

    @Test
    public void canAddDirectoryPathWhenRecursionModeIsDirectoriesOnly() throws IOException {
        final var fileSystem = Jimfs.newFileSystem();

        final var directoryA = fileSystem.getPath("directoryA");
        final var directoryB = fileSystem.getPath("directoryA/directoryB");
        final var fileA = fileSystem.getPath("fileA");
        final var fileB = fileSystem.getPath("directoryA/fileB");
        final var fileC = fileSystem.getPath("directoryA/directoryB/fileC");
        Files.createDirectory(directoryA);
        Files.createDirectory(directoryB);
        Files.createFile(fileA);
        Files.createFile(fileB);
        Files.createFile(fileC);

        final var list = new JPathList();
        list.setRecursionMode(JFileChooser.DIRECTORIES_ONLY);
        list.addPath(directoryA);
        list.addPath(directoryB);
        list.addPath(fileA);

        Assertions.assertEquals(2, list.getPaths().size());
        Assertions.assertTrue(list.getPaths().contains(directoryA));
        Assertions.assertTrue(list.getPaths().contains(directoryB));
    }

    @Test
    public void canAddDirectoryPathWhenRecursionModeIsFilesAndDirectories() throws IOException {
        final var fileSystem = Jimfs.newFileSystem();

        final var directoryA = fileSystem.getPath("directoryA");
        final var directoryB = fileSystem.getPath("directoryA/directoryB");
        final var fileA = fileSystem.getPath("fileA");
        final var fileB = fileSystem.getPath("directoryA/fileB");
        final var fileC = fileSystem.getPath("directoryA/directoryB/fileC");
        Files.createDirectory(directoryA);
        Files.createDirectory(directoryB);
        Files.createFile(fileA);
        Files.createFile(fileB);
        Files.createFile(fileC);

        final var list = new JPathList();
        list.setRecursionMode(JFileChooser.FILES_AND_DIRECTORIES);
        list.addPath(directoryA);
        list.addPath(directoryB);
        list.addPath(fileA);

        Assertions.assertEquals(5, list.getPaths().size());
        Assertions.assertTrue(list.getPaths().contains(directoryA));
        Assertions.assertTrue(list.getPaths().contains(fileA));
        Assertions.assertTrue(list.getPaths().contains(fileB));
        Assertions.assertTrue(list.getPaths().contains(fileC));
    }

    @Test
    public void cannotAddArrayOfPathsWhenArrayIsNull() {
        final var list = new JPathList();
        Assertions.assertThrows(NullPointerException.class, () -> {
            list.addPaths((Path[]) null);
        });
    }

    @Test
    public void canAddArrayOfPathsWhenArrayIsEmpty() throws IOException {
        final var list = new JPathList();
        list.addPaths();
        Assertions.assertEquals(0, list.getPaths().size());
    }

    @Test
    public void canAddArrayOfPaths() throws IOException {
        final var fileSystem = Jimfs.newFileSystem();

        final var fileA = fileSystem.getPath("fileA");
        final var fileB = fileSystem.getPath("fileB");
        Files.createFile(fileA);
        Files.createFile(fileB);

        final var paths = new Path[] {fileA, fileB};

        final var list = new JPathList();
        list.addPaths(paths);

        Assertions.assertEquals(2, list.getPaths().size());
        Assertions.assertTrue(list.getPaths().contains(fileA));
        Assertions.assertTrue(list.getPaths().contains(fileB));
    }

    @Test
    public void cannotAddListOfPathsWhenArrayIsNull() {
        final var list = new JPathList();
        Assertions.assertThrows(NullPointerException.class, () -> {
            list.addPaths((List<Path>) null);
        });
    }

    @Test
    public void canAddListOfPathsWhenArrayIsEmpty() throws IOException {
        final var list = new JPathList();
        list.addPaths(new ArrayList<>());
        Assertions.assertEquals(0, list.getPaths().size());
    }

    @Test
    public void canAddListOfPaths() throws IOException {
        final var fileSystem = Jimfs.newFileSystem();

        final var fileA = fileSystem.getPath("fileA");
        final var fileB = fileSystem.getPath("fileB");
        Files.createFile(fileA);
        Files.createFile(fileB);

        final var paths = new ArrayList<Path>();
        paths.add(fileA);
        paths.add(fileB);

        final var list = new JPathList();
        list.addPaths(paths);

        Assertions.assertEquals(2, list.getPaths().size());
        Assertions.assertTrue(list.getPaths().contains(fileA));
        Assertions.assertTrue(list.getPaths().contains(fileB));
    }

    @Test
    public void cannotRemovePathWhenPathIsNull() {
        final var list = new JPathList();
        Assertions.assertThrows(NullPointerException.class, () -> {
            list.removePath(null);
        });
    }

    @Test
    public void canRemovePath() throws IOException {
        final var fileSystem = Jimfs.newFileSystem();
        final var path = fileSystem.getPath("test");
        Files.createFile(path);

        final var list = new JPathList();
        list.addPath(path);
        list.removePath(path);

        Assertions.assertEquals(0, list.getPaths().size());
    }

    @Test
    public void cannotRemoveArrayOfPathsWhenArrayIsNull() {
        final var list = new JPathList();
        Assertions.assertThrows(NullPointerException.class, () -> {
            list.removePaths((Path[]) null);
        });
    }

    @Test
    public void canRemoveArrayOfPathsWhenArrayIsEmpty() {
        final var list = new JPathList();
        list.removePaths();
        Assertions.assertEquals(0, list.getPaths().size());
    }

    @Test
    public void canRemoveArrayOfPaths() throws IOException {
        final var fileSystem = Jimfs.newFileSystem();

        final var fileA = fileSystem.getPath("fileA");
        final var fileB = fileSystem.getPath("fileB");
        Files.createFile(fileA);
        Files.createFile(fileB);

        final var paths = new Path[] {fileA, fileB};

        final var list = new JPathList();
        list.addPaths(paths);
        list.removePaths(paths);

        Assertions.assertEquals(0, list.getPaths().size());
    }

    @Test
    public void cannotRemoveListOfPathsWhenArrayIsNull() {
        final var list = new JPathList();
        Assertions.assertThrows(NullPointerException.class, () -> {
            list.removePaths((List<Path>) null);
        });
    }

    @Test
    public void canRemoveListOfPathsWhenArrayIsEmpty() {
        final var list = new JPathList();
        list.removePaths(new ArrayList<>());
        Assertions.assertEquals(0, list.getPaths().size());
    }

    @Test
    public void canRemoveListOfPaths() throws IOException {
        final var fileSystem = Jimfs.newFileSystem();

        final var fileA = fileSystem.getPath("fileA");
        final var fileB = fileSystem.getPath("fileB");
        Files.createFile(fileA);
        Files.createFile(fileB);

        final var paths = new ArrayList<Path>();
        paths.add(fileA);
        paths.add(fileB);

        final var list = new JPathList();
        list.addPaths(paths);
        list.removePaths(paths);

        Assertions.assertEquals(0, list.getPaths().size());
    }

    @Test
    public void canRemoveAllPaths() throws IOException {
        final var fileSystem = Jimfs.newFileSystem();

        final var fileA = fileSystem.getPath("fileA");
        final var fileB = fileSystem.getPath("fileB");
        Files.createFile(fileA);
        Files.createFile(fileB);

        final var list = new JPathList();
        list.addPath(fileA);
        list.addPath(fileB);
        list.removeAllPaths();

        Assertions.assertEquals(0, list.getPaths().size());
    }

    @Test
    public void canSetDragAndDropEnabled() {
        final var list = new JPathList();
        Assertions.assertNotNull(list.isDragAndDropEnabled());

        list.setDragAndDropEnabled(false);
        Assertions.assertFalse(list.isDragAndDropEnabled());
        Assertions.assertNull(list.getDropTarget());
    }

    @Test
    public void canSetRecursionMode() {
        final var list = new JPathList();
        list.setRecursionMode(JFileChooser.FILES_ONLY);
        Assertions.assertEquals(JFileChooser.FILES_ONLY, list.getRecursionMode());
    }
}
