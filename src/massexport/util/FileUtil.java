package massexport.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * Contains methods to copy files and create deep directory structures.
 */
public class FileUtil {

    static final Logger LOGGER = Logger.getLogger(FileUtil.class.getName());
    private static final String[] MDTERM = {
        "DESCRIPTION",
        "OWNER",
        "LABEL",
        "STATUS",
        "VERSION",
        "VERSION_DATE",
        "DATE_CREATED",
        "EXPIRATION_DATE"
    };
    private static final int BLOCK_SIZE = 4096;
    private static long DIR_SIZE = 0L;
    
    /**
     * Append metadata information into a specific file in a specific folder in
     * the left-to-right order specified in a list.
     *
     * @param mdFile
     * @param name
     * @param metadata
     * @return true if successful
     */
    public static boolean writeMetadata(
            File mdFile, String name, HashMap<String, String> metadata) {
        StringBuilder mdBuffer = new StringBuilder();
        String mdValue;

        // use the supplied name instead of the metadata NAME value
        // mdBuffer.append(",");
        
        
        mdBuffer.append("\"");
        mdBuffer.append(name);
        mdBuffer.append("\"");

        // the metadata common to all files
        for (int i = 0; i < MDTERM.length; i++) {
            //if (i > 0) {
                mdBuffer.append(",");
            //}
            mdBuffer.append("\"");
            mdValue = metadata.get(MDTERM[i]);
            if (mdValue != null && mdValue.length() > 0) {
                mdBuffer.append(mdValue);
            }
            mdBuffer.append("\"");
        }
        // added for sourcepath
        mdBuffer.append(",");
        mdBuffer.append("\"");
        mdBuffer.append(mdFile.getParent()+File.separator+name);
        mdBuffer.append("\"");
        
        // user access metadata that may not be common to all files
        for (int i = 0; i < 10; i++) {
            mdBuffer.append(",");
            mdBuffer.append("\"");
            mdValue = metadata.get("USER_WITH_ACCESS_" + i);
            if (mdValue != null && mdValue.length() > 0) {
                mdBuffer.append(mdValue);
            }
            mdBuffer.append("\"");
            mdBuffer.append(",");
            mdBuffer.append("\"");
            mdValue = metadata.get("ACCESS_TYPE_" + i);
            if (mdValue != null && mdValue.length() > 0) {
                mdBuffer.append(mdValue);
            }
            mdBuffer.append("\"");
        }

        // taxonomy metadata that may not be common to all files
        for (int i = 0; i < 25; i++) {
            mdBuffer.append(",");
            mdBuffer.append("\"");
            mdValue = metadata.get("TAXO_TYPE_" + i);
            if (mdValue != null && mdValue.length() > 0) {
                mdBuffer.append(mdValue);
            }
            mdBuffer.append("\"");
            mdBuffer.append(",");
            mdBuffer.append("\"");
            mdValue = metadata.get("TAXO_NAME_" + i);
            if (mdValue != null && mdValue.length() > 0) {
                mdBuffer.append(mdValue);
            }
            mdBuffer.append("\"");
            mdBuffer.append(",");
            mdBuffer.append("\"");
            mdValue = metadata.get("TAXO_VALUE_" + i);
            if (mdValue != null && mdValue.length() > 0) {
                mdBuffer.append(mdValue);
            }
            mdBuffer.append("\"");
        }
        mdBuffer.append("\n");

        try {
            //mdFile.createNewFile();
        	
            FileWriter mdWriter = new FileWriter(mdFile, true);
            mdWriter.write(mdBuffer.toString());
            mdWriter.close();

            return true;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE,
                    "IO error {0}", ex.getMessage());
        }
        return false;
    }

    /**
     * Create a sub-directory and all its parent directories, if needed, given a
     * a parent directory and a relative pathname to the parent.
     *
     * @param parent Directory pathname
     * @param path Relative pathname to the parent
     * @return File object for the deepest directory in the relative path
     */
    public static File createDir(String parent, String path) {
        if (parent == null) {
            return null;
        }
        return createDir(new File(parent), path);
    }

    /**
     * Create a sub-directory and all its parent directories, if needed, given a
     * a parent directory and a relative pathname to the parent.
     *
     * @param parent Directory file object
     * @param path Relative pathname to the parent
     * @return File object for the deepest directory in the relative path
     */
    public static File createDir(File parent, String path) {
        if (parent == null) {
            return null;
        }
        if (path == null || path.length() == 0) {
            return parent;
        }

        path = fixPath(path);
        StringTokenizer tok = new StringTokenizer(path, File.separator);
        File f = parent;
        while (tok.hasMoreTokens()) {
        	
        		f = new File(f, tok.nextToken());
	            if (!f.exists()) {
	                if (!f.mkdirs()) {
	                    LOGGER.log(Level.SEVERE, "Unable to create directory: {0}", f.getPath());
	                    return null;
	                }
	            }
        }
        return f;
    }

    /**
     * Copy a file efficiently.
     *
     * @param in input file
     * @param out output file
     * @return true if successful, false otherwise
     */
    public static boolean copyFile(String in, String out) {
        if (in == null || out == null) {
            return false;
        }
        return copyFile(new File(in), new File(out));
    }

    /**
     * Copy a file efficiently.
     *
     * @param in input file
     * @param out output file
     * @return true if successful, false otherwise
     */
    public static boolean copyFile(File in, File out) {
        if (in == null || out == null) {
            return false;
        }

        try {
            FileInputStream inStream = new FileInputStream(in);
            FileOutputStream outStream = new FileOutputStream(out);
            if (!copyFile(inStream, outStream)) {
                LOGGER.log(Level.SEVERE, "Unable to copy file: {0}", in.getPath());
                if (out.exists()) {
                    out.delete();
                }
                return false;
            }
            return true;
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, "File not found: {0}", in.getPath());
        }
        return false;
    }

    /**
     * Copy the content of one stream to another efficiently.
     *
     * @param in input stream
     * @param out output stream
     * @return true if successful, false otherwise
     */
    public static boolean copyFile(InputStream in, OutputStream out) {
        if (in == null || out == null) {
            return false;
        }

        try {
            byte[] buf = new byte[BLOCK_SIZE];
            int i;
            while ((i = in.read(buf)) != -1) {
                out.write(buf, 0, i);
            }
            return true;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "IO error: {0}", ex.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        }
        return false;
    }

    /**
     * Deletes all files and sub-folders from a directory.
     *
     * @param dir The top-level directory
     * @return number of files and directories deleted
     */
    public static int deleteDir(File dir) {
        if (dir == null) {
            return 0;
        }

        int files = 0, dirs = 0;
        boolean containsSubFolder;
        Stack<File> dirStack = new Stack<File>();

        dirStack.push(dir); // add top-level directory to stack
        while (!dirStack.isEmpty()) {
            containsSubFolder = false;
            File currentDir = dirStack.peek();
            String[] fileArray = currentDir.list();
            for (int i = 0; i < fileArray.length; i++) {
                String fileName = currentDir.getPath() + File.separator
                        + fileArray[i];
                File file = new File(fileName);
                if (file.isDirectory()) {
                    // add sub-folder to stack
                    dirStack.push(file);
                    containsSubFolder = true;
                } else {
                    // delete file
                    file.delete();
                    // count file
                    files++;
                }
            }

            if (!containsSubFolder) {
                // remove sub-folder from stack
                dirStack.pop();
                // delete sub-folder
                currentDir.delete();
                // count sub-folder
                dirs++;
            }
        }

        return dirs + files;
    }

    /**
     * Fix a path string so it has the correct file separator characters.
     *
     * @param path A path string
     * @return Fixed path string
     */
    public static String fixPath(String path) {
        char sep = File.separatorChar;
        if (sep == '\\') {
            path = path.replace('/', File.separatorChar);
        } else if (sep == '/') {
            path = path.replace('\\', File.separatorChar);
        }
        return path;
    }

    /**
     * Write a file to a directory, getting the file data from a GZIP'ed stream.
     *
     * @param parent file parent directory
     * @param name file name
     * @param size file size in bytes
     * @param imageStream GZIP'ed stream
     * @param append append to existing file, if one exists
     * @return true if successful, false otherwise
     */
    public static File writeFile(File parent, String name, long size,
            GZIPInputStream imageStream, boolean append) {
        if (parent == null || name == null) {
            return null;
        }

        File f = null;
        BufferedOutputStream bs = null;
        try {
            int read;
            byte[] buf = new byte[BLOCK_SIZE];
            f = new File(parent, name);
            FileOutputStream fs = new FileOutputStream(f, append);
            bs = new BufferedOutputStream(fs, BLOCK_SIZE);
            while ((read = imageStream.read(buf, 0, BLOCK_SIZE)) != -1) {
                bs.write(buf, 0, read);
            }
            bs.flush();
            return f;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "IO error: {0}", ex.getMessage());
            LOGGER.log(Level.SEVERE, "Unable to create file: {0}", f.getPath());
            if (f.exists()) {
                f.delete();
            }
        } finally {
            if (bs != null) {
                try {
                    bs.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        }
        return null;
    }

    /**
     * Clear the last computed directory size.
     */
    public static void clearDirSize() {
        DIR_SIZE = 0L;
    }

    /**
     * Return the space being used by all files and folders under the specified
     * directory path.
     *
     * @param path A directory path
     * @return the space in kilobytes being used by everything under the
     * directory path, recursively.
     */
    public static void computeSpaceUsed(File dir) {
        if (dir == null) {
            return;
        }
        if (dir.isDirectory()) {
            String[] childList = dir.list();
            if (childList == null) {
                return;
            }
            for (int i = 0; i < childList.length; i++) {
                File child = new File(dir, childList[i]);
                DIR_SIZE += child.length();
                if (child.isDirectory()) {
                    computeSpaceUsed(child);
                }
            }
        } else {
            DIR_SIZE += dir.length();
        }
    }

    /**
     * Get the last computed directory size in kilobytes.
     *
     * @return the value of the DIR_SIZE member
     */
    public static long getDirSize() {
        return DIR_SIZE / 1024L;
    }
}
