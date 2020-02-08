package cc.flogi.smp.util;

import com.google.gson.Gson;
import lombok.NonNull;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Caden Kriese
 *
 * Created on 10/3/18.
 *
 * This code is copyright © Caden Kriese 2018
 */
public class UtilFile {

    private static final Gson GSON = new Gson();

    /**
     * Reads text from a file.
     *
     * @param file The file to be read from.
     * @return A string with all the lines of the file.
     */
    @NonNull public static String read(File file) {
        try {
            return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Writes text to a file.
     *
     * @param string The string to be written to a file.
     * @param file   The file to be written to.
     */
    @NonNull public static void write(String string, File file) {
        try {
            FileUtils.writeStringToFile(file, string, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Writes an object to a file, serializing it with {@link Gson#toJson(Object)}.
     *
     * @param object The object to be written to a file
     * @param file   The file to be written to.
     */
    @NonNull public static void write(Object object, File file) {
        write(GSON.toJson(object), file);
    }

    @NonNull public static void writeAndCreate(String string, File file) {
        try {
            if (!file.exists() && !file.createNewFile())
                throw new IOException("File creation failed.");

            write(string, file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @NonNull public static void writeAndCreate(Object object, File file) {
        writeAndCreate(GSON.toJson(object), file);
    }
}
