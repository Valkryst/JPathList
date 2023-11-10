package com.valkryst.JPathList;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Represents a list of {@link Path} objects.</p>
 *
 * <p>
 *     Supports drag-and-drop of files and directories, as well as the ability to recurse directories and add their
 *     files to the list. By default, both of these features are enabled.
 * </p>
 *
 * <p>Duplicate paths are not allowed.</p>
 */
public class JPathList extends JList<Path> implements DropTargetListener {
    private final Logger logger = Logger.getLogger("JPathList");

    /** The list of paths. */
    private final DefaultListModel<Path> pathsListModel = new DefaultListModel<>();

    /** How to recurse directories, when using drag-and-drop. */
    private final AtomicInteger recursionMode = new AtomicInteger(-1);

    /** Service used to add paths to the list. */
    private ExecutorService additionService;

    /** Shutdown hook for the addition service. */
    private Thread additionServiceShutdownHook;

    /** Aet of paths to be added to the list by the addition service. */
    private final LinkedBlockingQueue<Path> pathsToAdd = new LinkedBlockingQueue<>();

    /** Constructs a new {@code JPathList}. */
    public JPathList() {
        super.setModel(pathsListModel);

        this.setDragAndDropEnabled(true);

        this.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(final AncestorEvent event) {
                startAdditionService();
                addShutdownHook();
            }

            @Override
            public void ancestorRemoved(final AncestorEvent event) {
                removeShutdownHook();
                stopAdditionService();
            }

            @Override
            public void ancestorMoved(final AncestorEvent event) {}
        });
    }

    @Override
    public void dropActionChanged(final DropTargetDragEvent event) {
        // JavaDoc is unclear on what this method is supposed to do. Leaving it blank for now.
    }

    @Override
    public void drop(final DropTargetDropEvent event) {
        event.acceptDrop(DnDConstants.ACTION_COPY);

        final var transferable = event.getTransferable();
        List<File> files;

        try {
            files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
        } catch (final Exception e) {
            logger.log(Level.SEVERE, "An error occurred when retrieving the list of files from the transferable.", e);
            return;
        }

        for (final var file : files) {
            try {
                this.addPath(file.toPath());
            } catch (final IOException e) {
                logger.log(Level.SEVERE, "An I/O error occurred when adding a path to the list.", e);
            }
        }
    }

    @Override
    public void dragEnter(final DropTargetDragEvent event) {}

    @Override
    public void dragExit(final DropTargetEvent event) {}

    @Override
    public void dragOver(final DropTargetDragEvent event) {}

    /**
     * Adds a path to the list.
     *
     * @param path Path to be added.
     *
     * @throws FileNotFoundException If the path does not exist.
     * @throws IllegalAccessError If the path is not readable.
     * @throws IllegalStateException If the path is neither a directory nor a regular file.
     * @throws IOException If an I/O error occurs when recursing directories.
     * @throws NullPointerException If {@code path} is null.
     */
    public void addPath(final Path path) throws IOException {
        Objects.requireNonNull(path);

        if (Files.notExists(path)) {
            throw new FileNotFoundException("The file '%s' does not exist".formatted(path));
        }

        if (!Files.isReadable(path)) {
            throw new IllegalAccessError("The file '%s' cannot be read".formatted(path));
        }

        final var recursionMode = this.recursionMode.get();
        if (Files.isRegularFile(path)) {
            if (recursionMode == JFileChooser.DIRECTORIES_ONLY) {
                return;
            }

            pathsToAdd.add(path);
            return;
        }

        if (!Files.isDirectory(path)) {
            throw new IllegalStateException("The file '%s' is neither a regular file nor a directory.".formatted(path));
        }

        if (recursionMode < JFileChooser.FILES_ONLY || recursionMode > JFileChooser.FILES_AND_DIRECTORIES) {
            pathsToAdd.add(path);
            return;
        }

        final var pathsStream = Files.list(path);
        final var pathsList = pathsStream.filter(p -> switch (recursionMode) {
            case JFileChooser.FILES_ONLY -> Files.isRegularFile(p);
            case JFileChooser.DIRECTORIES_ONLY -> Files.isDirectory(p);
            case JFileChooser.FILES_AND_DIRECTORIES -> true;
            default -> {
                System.err.println("Unknown recursion mode: " + recursionMode);
                yield false;
            }
        }).toList();
        pathsStream.close();

        // In these cases, we want to add the directory itself to the list.
        if (recursionMode == JFileChooser.DIRECTORIES_ONLY || recursionMode == JFileChooser.FILES_AND_DIRECTORIES) {
            pathsToAdd.add(path);
        }

        this.addPaths(pathsList);
    }

    /**
     * Adds one or more paths to the list.
     *
     * @param paths Paths to be added.
     *
     * @throws IOException If an I/O error occurs.
     * @throws NullPointerException If {@code paths} is {@code null}.
     */
    public void addPaths(final Path... paths) throws IOException {
        Objects.requireNonNull(paths);

        for (final var path : paths) {
            this.addPath(path);
        }
    }

    /**
     * Adds one or more paths to the list.
     *
     * @param paths Paths to be added.
     *
     * @throws IOException If an I/O error occurs.
     * @throws NullPointerException If {@code paths} is {@code null}.
     */
    public void addPaths(final List<Path> paths) throws IOException {
        Objects.requireNonNull(paths);

        for (final var path : paths) {
            this.addPath(path);
        }
    }

    /** Removes all paths from the list. */
    public void removeAllPaths() {
        synchronized (pathsListModel) {
            pathsListModel.clear();
        }
    }

    /**
     * Removes a path from the list.
     *
     * @param path Path to be removed.
     * @throws NullPointerException If {@code path} is null.
     */
    public void removePath(final Path path) {
        Objects.requireNonNull(path);

        synchronized (pathsListModel) {
            pathsListModel.removeElement(path);
        }
    }

    /**
     * Removes one or more paths from the list.
     *
     * @param paths Paths to be removed.
     * @throws NullPointerException If {@code paths} is {@code null}.
     */
    public void removePaths(final Path... paths) {
        System.out.println("Removing paths from the list.");
        Objects.requireNonNull(paths);
        System.out.println("Paths are not null.");

        for (final var path : paths) {
            this.removePath(path);
        }
    }

    /**
     * Removes one or more paths from the list.
     *
     * @param paths Paths to be removed.
     * @throws NullPointerException If {@code paths} is {@code null}.
     */
    public void removePaths(final List<Path> paths) {
        Objects.requireNonNull(paths);

        for (final var path : paths) {
            this.removePath(path);
        }
    }

    /**
     * <p>Constructs a new shutdown hook for the addition service and adds it to the runtime.</p>
     *
     * <p>If a shutdown hook already exists, then this method does nothing.</p>
     */
    private void addShutdownHook() {
        if (additionServiceShutdownHook != null) {
            return;
        }

        additionServiceShutdownHook = new Thread(() -> {
            additionService.shutdownNow();

            try {
                additionService.awaitTermination(1, TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
                logger.log(Level.SEVERE, "An error occurred when shutting down the addition service.", e);
            }

            additionService.close();
        });

        Runtime.getRuntime().addShutdownHook(additionServiceShutdownHook);
    }

    /** Removes the shutdown hook for the addition service from the runtime. */
    private void removeShutdownHook() {
        Objects.requireNonNull(additionServiceShutdownHook);
        Runtime.getRuntime().removeShutdownHook(additionServiceShutdownHook);
        additionServiceShutdownHook = null;
    }


    /**
     * <p>Starts the addition service.</p>
     *
     * <p>If the addition service is already running, then this method does nothing.</p>
     */
    private void startAdditionService() {
        if (additionService != null) {
            return;
        }

        additionService = Executors.newSingleThreadExecutor();
        additionService.execute(() -> {
            while (!Thread.interrupted()) {
                try {
                    final var path = pathsToAdd.take();

                    synchronized (pathsListModel) {
                        if (pathsListModel.contains(path)) {
                            continue;
                        }

                        pathsListModel.addElement(path);
                    }
                } catch (final InterruptedException e) {
                    logger.log(Level.INFO, "The addition service has been interrupted.", e);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    /**
     * <p>Stops the addition service.</p>
     *
     * <p>If the addition service is already stopped, then this method does nothing.</p>
     */
    private void stopAdditionService() {
        if (additionService == null) {
            return;
        }

        additionService.shutdownNow();
        additionService = null;
    }

    /**
     * <p>Retrieves a copy of the list of paths.</p>
     *
     * <p>The list itself is a copy, but the paths are not.</p>
     *
     * @return The list of paths.
     */
    public List<Path> getPaths() {
        List<Path> list;
        synchronized (pathsListModel) {
            list = Collections.list(pathsListModel.elements());
        }

        return list;
    }

    /**
     * Retrieves whether drag-and-drop is enabled.
     *
     * @return Whether drag-and-drop is enabled.
     */
    public boolean isDragAndDropEnabled() {
        return this.getDropTarget() != null;
    }

    /**
     * Retrieves the recursion mode.
     *
     * @return The recursion mode.
     */
    public int getRecursionMode() {
        return recursionMode.get();
    }

    /**
     * Sets whether drag-and-drop is enabled.
     *
     * @param isEnabled Whether the feature is enabled.
     */
    public void setDragAndDropEnabled(final boolean isEnabled) {
        if (isEnabled) {
            this.setDropTarget(new DropTarget(this, this));
        } else {
            this.setDropTarget(null);
        }
    }

    /**
     * <p>Sets how to recurse directories, when using drag-and-drop.</p>
     *
     * <p>Any value outside of the following are considered "NONE":</p>
     *
     * <ul>
     *     <li>{@link JFileChooser#FILES_ONLY}</li>
     *     <li>{@link JFileChooser#DIRECTORIES_ONLY}</li>
     *     <li>{@link JFileChooser#FILES_AND_DIRECTORIES}</li>
     * </ul
     *
     * @param mode The new mode.
     */
    public void setRecursionMode(final int mode) {
        final int[] allowedModes = {
            JFileChooser.FILES_ONLY,
            JFileChooser.DIRECTORIES_ONLY,
            JFileChooser.FILES_AND_DIRECTORIES
        };

        for (final var allowedMode : allowedModes) {
            if (mode == allowedMode) {
                this.recursionMode.set(mode);
                return;
            }
        }

        recursionMode.set(-1);
    }
}
