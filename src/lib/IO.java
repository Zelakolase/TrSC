package lib;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Standard Disk IO Library
 * @author Morad
 */
public class IO {
    /**
     * Reads file as byte array
     * Please consider OutOfMemoryException, when the file size exceeds the allocated memory for JVM.
     *
     * @param filename the name of the dest. file
     * @return file content in bytes
     */
    public static byte[] read(String filename) {
        try {
            return Files.readAllBytes(Paths.get(filename));
        } catch (Exception e) {
            return null; /* Don't forget to handle 'null' in your application */
        }
    }

    /**
     * Writes on file a String value
     * @param filename the name of the dest. file
     * @param content  the content to write in String
     * @param append   weather to append to existing value or not
     * @see write(String filename, byte[] content, boolean append)
     * @return Status code [0=success,1=error]
     */
    public static int write(String filename, String content, boolean append) {
        return write(filename, content.getBytes(), append);
    }

    /**
     * Writes on file an array of bytes
     * @param filename the name of the dest. file
     * @param content  the content to write in bytes
     * @param append   weather to append to existing value or not
     * @return Status code [0=success,1=error]
     */
    public static int write(String filename, byte[] content, boolean append) {

        try {
            StandardOpenOption set = null;
            if (append) set = StandardOpenOption.APPEND;
            else set = StandardOpenOption.WRITE;
            Files.write(Paths.get(filename), content, set);
            return 0;
        } catch (Exception e) {
            return 1;
        }
    }
}