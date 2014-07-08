package massexport;

import java.io.File;
import java.sql.Connection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import massexport.util.*;

/**
 * Dumps a tree of files from HP Brain into a local directory. Each sub-folder
 * created will contain not only files from HP Brain but also a "metadata.csv"
 * file containing the attributes for the files in the sub-folder.
 */
public class DumpTree {

    static final Logger LOGGER = Logger.getLogger(DBUtil.class.getName());
    private int _nodeId;
    private boolean _getVersions, _getDeleted;
    private HashMap<Integer, String> _folderMap;

    public DumpTree(int nodeId, boolean getVersions, boolean getDeleted) {
        _nodeId = nodeId;
        _getVersions = getVersions;
        _getDeleted = getDeleted;
    }

    public void doit() {
        Connection con = DBConnection.getConnectionWait();
        DBUtil dbUtil = new DBUtil();

        
        if (!dbUtil.isFolder(con, _nodeId)) {
            LOGGER.log(Level.SEVERE,
                    "Node id {0} is not a folder node", _nodeId);
            return;
        }
        if (dbUtil.isDeleted(con, _nodeId)) {
            LOGGER.log(Level.SEVERE,
                    "Node id {0} is a deleted node", _nodeId);
            return;
        }

        String exportDir = Constants.getProperty("exportDir");
        if (exportDir == null || exportDir.length() == 0) {
            exportDir = ".";
        } else {
            exportDir = exportDir;//.toLowerCase();
        }
        if (!exportDir.endsWith(File.separator)) {
            exportDir += File.separator;
        }
        String parentPath = dbUtil.getBrainPath(con, _nodeId, null);//.toLowerCase();
        String folderName = dbUtil.getBrainName(con, _nodeId).toLowerCase();
        int idx = parentPath.lastIndexOf(folderName);
        if (idx >= 0) {
            parentPath = parentPath.substring(0, idx);
        }

        LOGGER.log(Level.INFO,
                "Parent path: {0}", parentPath);
        LOGGER.log(Level.INFO,
                "Top-level folder name: {0}", folderName);
        LOGGER.log(Level.INFO,
                "Top-level folder directory: {0}", exportDir + folderName);

        _folderMap = new HashMap<Integer, String>();
        //_folderMap.put(_nodeId, folderName);
        _folderMap.put(_nodeId, parentPath.toLowerCase());
        _folderMap = dbUtil.getFolderMap(con, _nodeId, parentPath, _folderMap);
        

        LOGGER.log(Level.INFO, "File export BEGIN");
        dbUtil.exportFolderFiles(
                con, exportDir, _folderMap, _getVersions, _getDeleted);
        LOGGER.log(Level.INFO, "File export END");
    }
}
