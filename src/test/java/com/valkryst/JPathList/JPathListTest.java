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
    /**
     * Creates a new {@link JPathList}.
     *
     * @return The new list.
     */
    public JPathList createPathList() {
        final var list = new JPathList();
        list.getAncestorListeners()[0].ancestorAdded(null); // This is a hack to start the addition service.
        return list;
    }

    /**
     * Attempts to retrieve the length of a {@link JPathList}, waiting up to 2 seconds for the list to update.
     *
     * @param list The list.
     * @param length The expected length of the list.
     *
     * @return Length of the list.
     */
    public int getPathListLength(final JPathList list, final int length) {
        for (int i = 0 ; i < 20 ; i++) {
            if (list.getPaths().size() == length) {
                return length;
            }

            try {
                Thread.sleep(100);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }

        return list.getPaths().size();
    }


    @Test
    public void canCreateJPathList() {
        final var list = createPathList();
        Assertions.assertEquals(0, list.getPaths().size());
        Assertions.assertTrue(list.isDragAndDropEnabled());
        Assertions.assertEquals(-1, list.getRecursionMode());
    }

    @Test
    public void cannotAddPathWhenPathIsNull() {
        final var list = createPathList();
        Assertions.assertThrows(NullPointerException.class, () -> {
            list.addPath(null);
        });
    }

    @Test
    public void cannotAddPathWhenPathDoesNotExist() {
        final var list = createPathList();
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
        try (final var fileSystem = Jimfs.newFileSystem()) {
            final var path = fileSystem.getPath("test");
            Files.createFile(path);

            final var list = createPathList();
            list.addPath(path);

            Assertions.assertEquals(1, getPathListLength(list, 1));
            Assertions.assertEquals(path, list.getPaths().get(0));
        }
    }

    @Test
    public void cannotAddPathWhenPathIsNeitherDirectoryNorRegularFile() {
        // todo I do not know how to test this. It should occur when a file passes the isRegularFile check and fails
        //      the subsequent isDirectory check.
    }

    @Test
    public void canAddDirectoryPathWhenRecursionModeIsNone() throws IOException {
        try (final var fileSystem = Jimfs.newFileSystem()) {
            final var path = fileSystem.getPath("test");
            Files.createDirectory(path);

            final var list = createPathList();
            list.addPath(path);

            Assertions.assertEquals(1, getPathListLength(list, 1));
            Assertions.assertEquals(path, list.getPaths().get(0));
        }
    }

    @Test
    public void canAddDirectoryPathWhenRecursionModeIsFilesOnly() throws IOException {
        try (final var fileSystem = Jimfs.newFileSystem()) {
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

            final var list = createPathList();
            list.setRecursionMode(JFileChooser.FILES_ONLY);
            list.addPath(directoryA);
            list.addPath(directoryB);
            list.addPath(fileA);

            Assertions.assertEquals(3, getPathListLength(list, 3));
            Assertions.assertTrue(list.getPaths().contains(fileA));
            Assertions.assertTrue(list.getPaths().contains(fileB));
            Assertions.assertTrue(list.getPaths().contains(fileC));
        }
    }

    @Test
    public void canAddDirectoryPathWhenRecursionModeIsDirectoriesOnly() throws IOException {
        try (final var fileSystem = Jimfs.newFileSystem()) {
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

            final var list = createPathList();
            list.setRecursionMode(JFileChooser.DIRECTORIES_ONLY);
            list.addPath(directoryA);
            list.addPath(directoryB);
            list.addPath(fileA);

            Assertions.assertEquals(2, getPathListLength(list, 2));
            Assertions.assertTrue(list.getPaths().contains(directoryA));
            Assertions.assertTrue(list.getPaths().contains(directoryB));
        }
    }

    @Test
    public void canAddDirectoryPathWhenRecursionModeIsFilesAndDirectories() throws IOException {
        try (final var fileSystem = Jimfs.newFileSystem()) {
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

            final var list = createPathList();
            list.setRecursionMode(JFileChooser.FILES_AND_DIRECTORIES);
            list.addPath(directoryA);
            list.addPath(directoryB);
            list.addPath(fileA);

            Assertions.assertEquals(5, getPathListLength(list, 5));
            Assertions.assertTrue(list.getPaths().contains(directoryA));
            Assertions.assertTrue(list.getPaths().contains(fileA));
            Assertions.assertTrue(list.getPaths().contains(fileB));
            Assertions.assertTrue(list.getPaths().contains(fileC));
        }
    }

    @Test
    public void cannotAddArrayOfPathsWhenArrayIsNull() {
        final var list = createPathList();
        Assertions.assertThrows(NullPointerException.class, () -> {
            list.addPaths((Path[]) null);
        });
    }

    @Test
    public void canAddArrayOfPathsWhenArrayIsEmpty() throws IOException {
        final var list = createPathList();
        list.addPaths();
        Assertions.assertEquals(0, list.getPaths().size());
    }

    @Test
    public void canAddArrayOfPaths() throws IOException {
        try (final var fileSystem = Jimfs.newFileSystem()) {
            final var fileA = fileSystem.getPath("fileA");
            final var fileB = fileSystem.getPath("fileB");
            Files.createFile(fileA);
            Files.createFile(fileB);

            final var paths = new Path[]{fileA, fileB};

            final var list = createPathList();
            list.addPaths(paths);

            Assertions.assertEquals(2, getPathListLength(list, 2));
            Assertions.assertTrue(list.getPaths().contains(fileA));
            Assertions.assertTrue(list.getPaths().contains(fileB));
        }
    }

    @Test
    public void cannotAddListOfPathsWhenArrayIsNull() {
        final var list = createPathList();
        Assertions.assertThrows(NullPointerException.class, () -> {
            list.addPaths((List<Path>) null);
        });
    }

    @Test
    public void canAddListOfPathsWhenArrayIsEmpty() throws IOException {
        final var list = createPathList();
        list.addPaths(new ArrayList<>());
        Assertions.assertEquals(0, list.getPaths().size());
    }

    @Test
    public void canAddListOfPaths() throws IOException {
        try (final var fileSystem = Jimfs.newFileSystem()) {
            final var fileA = fileSystem.getPath("fileA");
            final var fileB = fileSystem.getPath("fileB");
            Files.createFile(fileA);
            Files.createFile(fileB);

            final var paths = new ArrayList<Path>();
            paths.add(fileA);
            paths.add(fileB);

            final var list = createPathList();
            list.addPaths(paths);

            Assertions.assertEquals(2, getPathListLength(list, 2));
            Assertions.assertTrue(list.getPaths().contains(fileA));
            Assertions.assertTrue(list.getPaths().contains(fileB));
        }
    }

    @Test
    public void cannotRemovePathWhenPathIsNull() {
        final var list = createPathList();
        Assertions.assertThrows(NullPointerException.class, () -> {
            list.removePath(null);
        });
    }

    @Test
    public void canRemovePath() throws IOException {
        try (final var fileSystem = Jimfs.newFileSystem()) {
            final var path = fileSystem.getPath("test");
            Files.createFile(path);

            final var list = createPathList();
            list.addPath(path);
            list.removePath(path);

            Assertions.assertEquals(0, list.getPaths().size());
        }
    }

    @Test
    public void cannotRemoveArrayOfPathsWhenArrayIsNull() {
        final var list = createPathList();
        Assertions.assertThrows(NullPointerException.class, () -> {
            list.removePaths((Path[]) null);
        });
    }

    @Test
    public void canRemoveArrayOfPathsWhenArrayIsEmpty() {
        final var list = createPathList();
        list.removePaths();
        Assertions.assertEquals(0, list.getPaths().size());
    }

    @Test
    public void canRemoveArrayOfPaths() throws IOException {
        try (final var fileSystem = Jimfs.newFileSystem()) {
            final var fileA = fileSystem.getPath("fileA");
            final var fileB = fileSystem.getPath("fileB");
            Files.createFile(fileA);
            Files.createFile(fileB);

            final var paths = new Path[]{fileA, fileB};

            final var list = createPathList();
            list.addPaths(paths);
            list.removePaths(paths);

            Assertions.assertEquals(0, list.getPaths().size());
        }
    }

    @Test
    public void cannotRemoveListOfPathsWhenArrayIsNull() {
        final var list = createPathList();
        Assertions.assertThrows(NullPointerException.class, () -> {
            list.removePaths((List<Path>) null);
        });
    }

    @Test
    public void canRemoveListOfPathsWhenArrayIsEmpty() {
        final var list = createPathList();
        list.removePaths(new ArrayList<>());
        Assertions.assertEquals(0, list.getPaths().size());
    }

    @Test
    public void canRemoveListOfPaths() throws IOException {
        try (final var fileSystem = Jimfs.newFileSystem()) {
            final var fileA = fileSystem.getPath("fileA");
            final var fileB = fileSystem.getPath("fileB");
            Files.createFile(fileA);
            Files.createFile(fileB);

            final var paths = new ArrayList<Path>();
            paths.add(fileA);
            paths.add(fileB);

            final var list = createPathList();
            list.addPaths(paths);
            list.removePaths(paths);

            Assertions.assertEquals(0, list.getPaths().size());
        }
    }

    @Test
    public void canRemoveAllPaths() throws IOException {
        try (final var fileSystem = Jimfs.newFileSystem()) {
            final var fileA = fileSystem.getPath("fileA");
            final var fileB = fileSystem.getPath("fileB");
            Files.createFile(fileA);
            Files.createFile(fileB);

            final var list = createPathList();
            list.addPath(fileA);
            list.addPath(fileB);
            list.removeAllPaths();

            Assertions.assertEquals(0, list.getPaths().size());
        }
    }

    @Test
    public void canSetDragAndDropEnabled() {
        final var list = createPathList();
        list.setDragAndDropEnabled(false);
        Assertions.assertFalse(list.isDragAndDropEnabled());
    }

    @Test
    public void canSetRecursionMode() {
        final var list = createPathList();
        list.setRecursionMode(JFileChooser.FILES_ONLY);
        Assertions.assertEquals(JFileChooser.FILES_ONLY, list.getRecursionMode());
    }
}
