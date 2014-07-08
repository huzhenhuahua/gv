package massexport.util;

import java.io.File;
import java.util.ArrayList;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Searches for files in original document source.
 */
public class FileSearch {

    static final Logger LOGGER = Logger.getLogger(FileSearch.class.getName());

    /**
     * Return a listing of all files and sub-folders in the source directory.
     *
     * @param sourcePath Source directory pathname
     * @return list of file and sub-folder pathnames
     */
    public ArrayList<String> getSourceListing(ArrayList<String> fileFolderList,
            File sourcePath) {
        if (fileFolderList == null) {
            fileFolderList = new ArrayList<String>();
        }
        String[] list = sourcePath.list();
        File sourceFile;
        for (int i = 0; i < list.length; i++) {
            sourceFile = new File(sourcePath.getPath(), list[i]);
            fileFolderList.add(sourceFile.getPath());
            if (sourceFile.isDirectory()) {
                getSourceListing(fileFolderList, sourceFile);
            }
        }
        return fileFolderList;
    }

    /**
     * Return a listing of all files in the source directory.
     *
     * @param sourcePath Source directory pathname
     * @return list of file pathnames
     */
    public ArrayList<String> getFileListing(ArrayList<String> fileList,
            File sourcePath) {
        if (fileList == null) {
            fileList = new ArrayList<String>();
        }
        String[] list = sourcePath.list();
        File sourceFile;
        for (int i = 0; i < list.length; i++) {
            sourceFile = new File(sourcePath, list[i]);
            if (sourceFile.isDirectory()) {
                getSourceListing(fileList, sourceFile);
            } else {
                fileList.add(sourceFile.getPath());
            }
        }
        return fileList;
    }

    /**
     * Return a listing of all sub-folders in the source directory.
     *
     * @param sourcePath Source directory pathname
     * @return list of sub-folder pathnames
     */
    public ArrayList<String> getFolderListing(ArrayList<String> folderList,
            File sourcePath) {
        if (folderList == null) {
            folderList = new ArrayList<String>();
        }
        String[] list = sourcePath.list();
        File sourceFile;
        for (int i = 0; i < list.length; i++) {
            sourceFile = new File(sourcePath, list[i]);
            if (sourceFile.isDirectory()) {
                folderList.add(sourceFile.getPath());
                getSourceListing(folderList, sourceFile);
            }
        }
        return folderList;
    }
}
