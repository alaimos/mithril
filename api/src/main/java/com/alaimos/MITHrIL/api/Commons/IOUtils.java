package com.alaimos.MITHrIL.api.Commons;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class IOUtils {

    /**
     * The Unix separator character.
     */
    private static final char UNIX_SEPARATOR = '/';

    /**
     * The Windows separator character.
     */
    private static final char WINDOWS_SEPARATOR = '\\';

    /**
     * Returns the index of the last directory separator character. This method will handle a file in either Unix or
     * Windows format. The position of the last forward or backslash is returned. The output will be the same
     * irrespective of the machine that the code is running on.
     *
     * @param filename the filename to find the last path separator in, null returns -1
     * @return the index of the last separator character, or -1 if there is no such character
     */
    public static int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        }
        int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
        int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
        return Math.max(lastUnixPos, lastWindowsPos);
    }

    /**
     * Gets the name minus the path from a full filename. This method will handle a file in either Unix or Windows
     * format. The text after the last forward or backslash is returned. a/b/c.txt --> c.txt a.txt     --> a.txt a/b/c
     * --> c a/b/c/    --> "" The output will be the same irrespective of the machine that the code is running on.
     *
     * @param filename the filename to query, null returns null
     * @return the name of the file without the path, or an empty string if none exists
     */
    public static String getName(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfLastSeparator(filename);
        return filename.substring(index + 1);
    }

    /**
     * Sanitize a filename by replacing all non-allowed characters with a dash. The allowed characters are: a-z, A-Z,
     * 0-9, dot, dash and underscore. This method should be used only for filenames, not for paths.
     *
     * @param filename the filename to sanitize
     * @return the sanitized filename
     */
    @Contract(pure = true)
    public static @NotNull String sanitizeFilename(@NotNull String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "-");
    }


}
