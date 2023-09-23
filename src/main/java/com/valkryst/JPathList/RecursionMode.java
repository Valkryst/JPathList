package com.valkryst.JPathList;

public enum RecursionMode {
    /** Include all paths, but do not recurse directories. */
    NONE,

    /** Include all file paths. */
    FILES_ONLY,

    /** Include all directory paths. */
    DIRECTORIES_ONLY,

    /** Include all paths and recurse directories. */
    FILES_AND_DIRECTORIES
}
