package massexport.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains methods for getting an Oracle database connection to the HP Brain
 * Oracle PRO and ITG databases.
 */
public class DBUtil {

    static final Logger LOGGER = Logger.getLogger(DBUtil.class.getName());

    /**
     * Takes a date of the form 15-MAR-2010 and returns the corresponding
     * java.sql.Timestamp object.
     *
     * @param date
     * @return java.sql.Timestamp object equivalent to the date string
     */
    public java.sql.Timestamp convertDate(String date) {
        Calendar cal = new GregorianCalendar();

        int count = 1;
        StringTokenizer st = new StringTokenizer(date, "-");
        while (st.hasMoreTokens()) {
            if (count == 4) {
                break;
            }
            String datePart = st.nextToken();
            if (count == 1) {  // 1st token is day of month
                int d = Integer.parseInt(datePart);
                cal.set(Calendar.DAY_OF_MONTH, d);
            } else if (count == 2) {  // 2nd token is month abbreviation
                int m = 0;
                if (datePart.equals("JAN")) {
                    m = 0;
                } else if (datePart.equals("FEB")) {
                    m = 1;
                } else if (datePart.equals("MAR")) {
                    m = 2;
                } else if (datePart.equals("APR")) {
                    m = 3;
                } else if (datePart.equals("MAY")) {
                    m = 4;
                } else if (datePart.equals("JUN")) {
                    m = 5;
                } else if (datePart.equals("JUL")) {
                    m = 6;
                } else if (datePart.equals("AUG")) {
                    m = 7;
                } else if (datePart.equals("SEP")) {
                    m = 8;
                } else if (datePart.equals("OCT")) {
                    m = 9;
                } else if (datePart.equals("NOV")) {
                    m = 10;
                } else if (datePart.equals("DEC")) {
                    m = 11;
                }
                cal.set(Calendar.MONTH, m);
            } else if (count == 3) {  // 3rd token is year
                int y = Integer.parseInt(datePart);
                cal.set(Calendar.YEAR, y);
            }
            count++;
        }

        return new java.sql.Timestamp(cal.getTimeInMillis());
    }

    /**
     * Returns true if the node is a folder node.
     *
     * @param con
     * @param nodeId
     * @return true if the node is marked as a FOLDER
     */
    public boolean isFolder(Connection con, int nodeId) {
        try {
            boolean isFolder = false;

            Statement s1 = con.createStatement();
            ResultSet r1 = s1.executeQuery(
                    "SELECT NODE_TYPE_ID FROM NODES WHERE NODE_ID = " + nodeId);
            if (r1.next()) {
                String type = r1.getString(1);
                if (type != null) {
                    if (type.equals("FOLDER")) {
                        isFolder = true;
                    }
                    if (type.equals("KDB")) {
                        isFolder = true;
                    }
                    // very few of these
                    //if (type.equals("SHORTCUTFOLDER")) {
                    //    isFolder = true;
                    //}
                }
            }
            r1.close();
            s1.close();

            return isFolder;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE,
                    "SQL error: {0}", ex.getMessage());
            LOGGER.log(Level.SEVERE,
                    "Unable to get node type for node: {0}", nodeId);
        }
        return false;
    }

    /**
     * Returns true if the node is a folder node.
     *
     * @param con
     * @param nodeId
     * @return true if the node is marked as a FOLDER
     */
    public boolean isDocument(Connection con, int nodeId) {
        try {
            boolean isDoc = false;

            Statement s1 = con.createStatement();
            ResultSet r1 = s1.executeQuery(
                    "SELECT NODE_TYPE_ID FROM NODES WHERE NODE_ID = " + nodeId);
            if (r1.next()) {
                String type = r1.getString(1);
                if (type != null) {
                    if (type.equals("DOCUMENT")) {
                        isDoc = true;
                    }
                    if (type.equals("ATTACHMENT")) {
                        isDoc = true;
                    }
                    // very few of these
                    //if (type.equals("DOCURL")) {
                    //    isFolder = true;
                    //}
                    //if (type.equals("SHORTCUT")) {
                    //    isFolder = true;
                    //}
                }
            }
            r1.close();
            s1.close();

            return isDoc;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE,
                    "SQL error: {0}", ex.getMessage());
            LOGGER.log(Level.SEVERE,
                    "Unable to get node type for node: {0}", nodeId);
        }
        return false;
    }

    /**
     * Returns true if the node has been previously deleted.
     *
     * @param con
     * @param nodeId
     * @return true if the node is marked as DELETED
     */
    public boolean isDeleted(Connection con, int nodeId) {
        try {
            boolean isDeleted = false;

            Statement s1 = con.createStatement();
            ResultSet r1 = s1.executeQuery(
                    "SELECT STATUS FROM NODES WHERE NODE_ID = " + nodeId);
            if (r1.next()) {
                String status = r1.getString(1);
                if (status != null && status.equals("DELETED")) {
                    isDeleted = true;
                }
            }
            r1.close();
            s1.close();

            return isDeleted;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE,
                    "SQL error: {0}", ex.getMessage());
            LOGGER.log(Level.SEVERE,
                    "Unable to get node status for node: {0}", nodeId);
        }
        return false;
    }

    /**
     * Get a folder map of all sub-folders beneath the parent folder. The map
     * contains the sub-folder id and a relative path, where the path is the
     * full HP Brain path after removing the parent path from the beginning of
     * the path, and then adding the "export" path to back to the beginning of
     * the path.
     *
     * @param con
     * @param parentId
     * @return HashMap containing a complete hierarchy of sub-folders
     */
    public HashMap getFolderMap(Connection con, int parentId,
            String parentPath, HashMap<Integer, String> folderMap) {
        try {
            if (folderMap == null) {
                folderMap = new HashMap<Integer, String>();
            }
            ArrayList<Integer> subfolderList = new ArrayList<Integer>();

            Statement s1 = con.createStatement();
            ResultSet r1 = s1.executeQuery(
                    "SELECT NODE_ID FROM NODES WHERE PARENT_ID = " + parentId
                    + " AND NODE_TYPE_ID LIKE 'FOLDER'");
            while (r1.next()) {
                int fid = r1.getInt(1);
                String fpath = getBrainPath(con, fid, null).toLowerCase();
                if (fpath.indexOf(parentPath) >= 0) {
                    fpath = fpath.substring(parentPath.length());
                }

                folderMap.put(fid, fpath);
                LOGGER.log(Level.INFO,
                        "Folder id {0} mapped to path: {1}", new Object[]{fid, fpath});

                subfolderList.add(fid);
            }
            r1.close();
            s1.close();

            for (Iterator<Integer> it = subfolderList.iterator(); it.hasNext();) {
                Integer fid = (Integer) it.next();
                folderMap = getFolderMap(con, fid, parentPath, folderMap);
            }

            return folderMap;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE,
                    "SQL error: {0}", ex.getMessage());
            LOGGER.log(Level.SEVERE,
                    "Unable to get folder map for parent node: {0}", parentId);
        }
        return null;
    }

    /**
     * Get the HP Brain path for the node. This is the path the user would see
     * in the user interface.
     *
     * @param con
     * @param nodeId
     * @return full HP Brain pathname
     */
    public String getBrainPath(Connection con, int nodeId, String path) {
        try {
            String name = null;
            int parentId = 0;

            Statement s1 = con.createStatement();
            ResultSet r1 = s1.executeQuery(
                    "SELECT NAME, PARENT_ID FROM NODES WHERE NODE_ID = " + nodeId);
            if (r1.next()) {
                name = r1.getString(1);
                parentId = r1.getInt(2);
            }
            r1.close();
            s1.close();

            if (name == null) {
                return null;
            }

            if (path == null || path.length() == 0) {
                path = File.separator + name;
            } else {
                path = File.separator + name + path;
            }

            if (parentId > 0) {
                path = getBrainPath(con, parentId, path);
            }
            return path;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE,
                    "SQL error: {0}", ex.getMessage());
            LOGGER.log(Level.SEVERE,
                    "Unable to get Brain path for node: {0}", nodeId);
        }
        return null;
    }

    /**
     * Get the HP Brain name for the node.
     *
     * @param con
     * @param nodeId
     * @return full HP Brain pathname
     */
    public String getBrainName(Connection con, int nodeId) {
        try {
            String name = null;

            Statement s1 = con.createStatement();
            ResultSet r1 = s1.executeQuery(
                    "SELECT NAME FROM NODES WHERE NODE_ID = " + nodeId);
            if (r1.next()) {
                name = r1.getString(1);
            }
            r1.close();
            s1.close();

            return name;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE,
                    "SQL error: {0}", ex.getMessage());
            LOGGER.log(Level.SEVERE,
                    "Unable to get name for node: {0}", nodeId);
        }
        return null;
    }

    /**
     * Get metadata information for the node.
     *
     * @param con
     * @param nodeId
     * @return HashMap containing all metadata for the node
     */
    public HashMap getMetadata(Connection con, int nodeId) {
        HashMap<String, String> nodeMap = new HashMap<String, String>();

        try {
            Statement s1 = con.createStatement();
            ResultSet r1 = s1.executeQuery(
                    "SELECT NAME, DESCRIPTION, OWNER, PERMISSION_ID, "
                    + "STATUS, VERSION, VERSION_DATE, DATE_CREATED, "
                    + "EXPIRATION_DATE "
                    + "FROM NODES WHERE NODE_ID = " + nodeId);
            if (r1.next()) {
                nodeMap.put("NAME", r1.getString(1));
                nodeMap.put("DESCRIPTION", r1.getString(2));
                nodeMap.put("OWNER", r1.getString(3));
                nodeMap.put("LABEL", r1.getString(4));
                nodeMap.put("STATUS", r1.getString(5));
                nodeMap.put("VERSION", Integer.toString(r1.getInt(6)));
                nodeMap.put("VERSION_DATE", r1.getTimestamp(7).toString());
                nodeMap.put("DATE_CREATED", r1.getTimestamp(8).toString());
                nodeMap.put("EXPIRATION_DATE", r1.getTimestamp(9) == null?"":r1.getTimestamp(9).toString());
            }
            r1.close();
            s1.close();

            int count = 0;
            Statement s2 = con.createStatement();
            ResultSet r2 = s2.executeQuery(
                    "SELECT NT_USER, ACCESS_TYPE_ID "
                    + "FROM NODE_ACCESS WHERE NODE_ID = " + nodeId);
            while (r2.next()) {
                nodeMap.put("USER_WITH_ACCESS_" + count, r2.getString(1));
                nodeMap.put("ACCESS_TYPE_" + count, r2.getString(2));
                count++;
            }
            r2.close();
            s2.close();

            count = 0;
            Statement s3 = con.createStatement();
            ResultSet r3 = s3.executeQuery(
                    "SELECT TAXO_ATTRIBUTES.ATTRIBUTE, TAXO_ATTRIBUTES.VALUE, "
                    + "NODE_TAXONOMIES.VALUE "
                    + "FROM TAXO_ATTRIBUTES, NODE_TAXONOMIES "
                    + "WHERE TAXO_ATTRIBUTES.TAXO_ID = NODE_TAXONOMIES.TAXO_ID "
                    + "AND NODE_TAXONOMIES.NODE_ID = " + nodeId);
            while (r3.next()) {
                nodeMap.put("TAXO_TYPE_" + count, r3.getString(1));
                nodeMap.put("TAXO_NAME_" + count, r3.getString(2));
                nodeMap.put("TAXO_VALUE_" + count, r3.getString(3));
                count++;
            }
            r3.close();
            s3.close();

            return nodeMap;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE,
                    "SQL error: {0}", ex.getMessage());
            LOGGER.log(Level.SEVERE,
                    "Unable to get metadata information for node: {0}", nodeId);
        }
        return null;
    }

    /**
     * Get the local path for the file.
     *
     * @param con
     * @param nodeId
     * @return full local file pathname
     */
    public String getLocalPath(Connection con, int nodeId) {
        try {
            int dirId = 0;
            String serverId = null, path = null;

            Statement s1 = con.createStatement();
            ResultSet r1 = s1.executeQuery(
                    "SELECT SERVER_ID, DIRECTORY_ID "
                    + "FROM SERVER_NODES WHERE NODE_ID = " + nodeId
                     // + " AND SKIP_DRIVE = 'N' " 
                    +" ORDER BY DIRECTORY_ID ASC");
            if (r1.next()) {
                serverId = r1.getString(1);
                dirId = r1.getInt(2);
            }
            r1.close();
            s1.close();

            // query to get the current HP Brain drive information;
            // just get the info for the most recently added drive
            // which is the one likely to have the needed space
            Statement s2 = con.createStatement();
            ResultSet r2 = s2.executeQuery(
                    "SELECT PATH FROM SERVER_DIRECTORIES "
                    + "WHERE SERVER_ID LIKE '" + serverId
                    + "' AND DIRECTORY_ID = " + dirId);
            if (r2.next()) {
                path = r2.getString(1);
            }
            r2.close();
            s2.close();

            // look up local path matching the HP Brain pathname
            // e.g. if the production HP Brain pathname was:
            // \\gvw1582.americas.hpqcorp.net\f$\hpbrain\datafiles\
            // and this is mapped to:
            // /opt/webhost/apps/mnt/drv_f/hpbrain/datafiles/
            // in pro.properties, the return that as the local pathname
            String lpath = Constants.getProperty(path);
            if (lpath == null || lpath.length() == 0) {
                LOGGER.log(Level.SEVERE, "Unable to get local path for path: {0}", path);
                return null;
            }

            // the path for the file must be computed according to the following:
            // (1) if the node id is 4 digits, add the 1st digit to the local
            //     pathname as a subdirectory, and a file will with name = node id will be found there:
            //     e.g. for node id = 3597:
            //     local path = /opt/webhost/apps/mnt/drv_f/hpbrain/datafiles/
            //     local file = /opt/webhost/apps/mnt/drv_f/hpbrain/datafiles/3/3597
            // (2) if the node id is 5 digits, add the 1st and 2nd digits to the local
            //     pathname as subdirectories, and a file will with name = node id will be found there:<br>
            //     e.g. for node id = 35971:
            //     local path = /opt/webhost/apps/mnt/drv_f/hpbrain/datafiles/
            //     local file = /opt/webhost/apps/mnt/drv_f/hpbrain/datafiles/3/5/35971
            // (3) if the node id is 6 digits, add the 1st, 2nd, and 3rd digits to the local
            //     pathname as subdirectories, and a file will with name = node id will be found there:
            //     e.g. for node id = 359716
            //     local path = /opt/webhost/apps/mnt/drv_f/hpbrain/datafiles/
            //     local file = /opt/webhost/apps/mnt/drv_f/hpbrain/datafiles/3/5/9/359716
            // (4) if the node id is 7 digits, add the 1st, 2nd, 3rd, and 4th digits to the local
            //     pathname as subdirectories, and a file will with name = node id will be found there:
            //     e.g. for node id = 3597162
            //     local path = /opt/webhost/apps/mnt/drv_f/hpbrain/datafiles/
            //     local file = /opt/webhost/apps/mnt/drv_f/hpbrain/datafiles/3/5/9/7/3597162
            // (5) if the node id is 8 digits, add the 1st, 2nd, 3rd, 4th, and 5th digits to the local
            //     pathname as subdirectories, and a file will with name = node id will be found there:
            //     e.g. for node id = 35971624
            //     local path = /opt/webhost/apps/mnt/drv_f/hpbrain/datafiles/
            //     local file = /opt/webhost/apps/mnt/drv_f/hpbrain/datafiles/3/5/9/7/1/35971624
            String nid = Integer.toString(nodeId);
            StringBuilder spath = new StringBuilder();
            /*
            if (nid.length() >= 4) {
                spath.append(nid.charAt(0));
                spath.append(File.separatorChar);
            }
            if (nid.length() >= 5) {
                spath.append(nid.charAt(1));
                spath.append(File.separatorChar);
            }
            if (nid.length() >= 6) {
                spath.append(nid.charAt(2));
                spath.append(File.separatorChar);
            }
            if (nid.length() >= 7) {
                spath.append(nid.charAt(3));
                spath.append(File.separatorChar);
            }
            if (nid.length() >= 8) {
                spath.append(nid.charAt(4));
                spath.append(File.separatorChar);
            }
            if (nid.length() >= 9) {
                spath.append(nid.charAt(5));
                spath.append(File.separatorChar);
            }
            if (nid.length() >= 10) {
                spath.append(nid.charAt(6));
                spath.append(File.separatorChar);
            }
            if (nid.length() >= 11) {
                spath.append(nid.charAt(7));
                spath.append(File.separatorChar);
            }
            */
            int n = 4; int index = 0;
            while(nid.length() >= n) {
            	spath.append(nid.charAt(index));
            	spath.append(File.separatorChar);
            	index++;n++;
            }
            return (lpath + spath.toString() + nid);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE,
                    "SQL error: {0}", ex.getMessage());
            LOGGER.log(Level.SEVERE,
                    "Unable to get local path for node: {0}", nodeId);
        }
        return null;
    }

    /**
     * Exports all files found in every folder in the folder map to the export
     * directory.
     *
     * @param folderMap
     * @return Count of files exported
     */
    public int exportFolderFiles(
            Connection con, String exportDir, HashMap<Integer, String> folderMap,
            boolean getVersions, boolean getDeleted) {
        int count = 0, n, nodeId, pnodeId, orig_nodeId;
        String status, pstatus;
        boolean more;

        try {
            for (Iterator<Integer> it = folderMap.keySet().iterator(); it.hasNext();) {
                Integer fid = (Integer) it.next();

                File folder = FileUtil.createDir(exportDir, folderMap.get(fid));
                
                if (folder == null) {
                    LOGGER.log(Level.SEVERE,
                            "Unable to create export folder for folder node {0}", fid);
                    break;
                }

                Statement s1 = con.createStatement();
                ResultSet r1 = s1.executeQuery(
                        "SELECT NODE_ID, STATUS FROM NODES WHERE "
                        + "(NODE_TYPE_ID LIKE 'DOCUMENT' OR NODE_TYPE_ID LIKE 'ATTACHMENT') "
                        + "AND PARENT_ID = " + fid);
                while (r1.next()) {
                    n = 0; // this is the current version
                    nodeId = r1.getInt(1);
                    status = r1.getString(2);

                    // skip if the node is not marked READY and we don't want
                    // deleted files.
                    // note that we do this check first, because if we don't
                    // want deleted files, we don't want old versions of
                    // deleted files.
                    if (!getDeleted && !status.equals("READY")) {
                        continue;
                    }

                    // Export the file to the export folder and add its metadata
                    // to the metadata file in the export folder
                    if (!exportFile(con, nodeId, folder, n)) {
                        continue;
                    }
                    count++;  // export successful

                    // the node selected above is the most recent version
                    // since it is linked directly to the folder node.
                    // to find the previous version, we try to find a DOCUMENT
                    // node where the parent of that node is this node.
                    // then we will need to check if its parent is also a
                    // DOCUMENT node, indicating an even earlier version.
                    // we will work the chain backward thusly, going from most
                    // recent version to oldest version, and at the end of
                    // this chain there should be a DOCUMENT node that has no
                    // parent.
                    if (getVersions) {
                        n++;
                        more = true;
                        orig_nodeId = nodeId;
                        try {
                            do {
                                Statement s2 = con.createStatement();
                                ResultSet r2 = s2.executeQuery(
                                        "SELECT NODE_ID, STATUS FROM NODES WHERE "
                                        + "(NODE_TYPE_ID LIKE 'DOCUMENT' OR NODE_TYPE_ID LIKE 'ATTACHMENT') "
                                        + "AND PARENT_ID = " + nodeId);
                                if (r2.next()) {  // it's a singly linked list, so only one
                                    pnodeId = r2.getInt(1);
                                    pstatus = r2.getString(2);

                                    // skip if the node is not marked READY and we don't want
                                    // deleted files; in other words, we don't want deleted
                                    // old versions
                                    if (!getDeleted && !pstatus.equals("READY")) {
                                        continue;
                                    }

                                    // Export the file to the export folder and add its metadata
                                    // to the metadata file in the export folder
                                    if (!exportFile(con, pnodeId, folder, n)) {
                                        continue;
                                    }
                                    count++;  // export successful
                                    
                                    nodeId = pnodeId;
                                } else {
                                    more = false;
                                }

                                n++;
                                if (n > 25) {
                                    more = false;
                                }

                                r2.close();
                                s2.close();
                            } while (more);
                        } catch (SQLException ex) {
                            LOGGER.log(Level.SEVERE,
                                    "SQL error {0}", ex.getMessage());
                            LOGGER.log(Level.SEVERE,
                                    "Unable to export all old versions of node {0} to export directory: {1}",
                                    new Object[]{orig_nodeId, exportDir});
                        }
                    }
                }
                r1.close();
                s1.close();
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE,
                    "SQL error {0}", ex.getMessage());
            LOGGER.log(Level.SEVERE,
                    "Unable to export all files to export directory: {0}", exportDir);
        }
        return count;
    }
    

    private static String getHeaders(){
    	StringBuilder headers = new StringBuilder();
    	headers.append("NAME,DESCRIPTION,OWNER,LABEL,STATUS,VERSION,VERSION_DATE,DATE_CREATED,EXPIRATION_DATE,SOURCE_PATH");
    	
    	for(int i = 0; i < 10; i++)
    	{
    		headers.append(",");
    		headers.append("USER_WITH_ACCESS_"+i);
    		headers.append(",");
    		headers.append("ACCESS_TYPE_"+i);
    	}
    	
    	for(int i = 0; i < 25; i++)
    	{
    		headers.append(",");
    		headers.append("TAXO_TYPE_"+i);
    		headers.append(",");
    		headers.append("TAXO_NAME_"+i);
    		headers.append(",");
    		headers.append("TAXO_VALUE_"+i);
    	}
    	headers.append("\n");
    	return headers.toString();
    }


    /**
     * Copy a file to the export folder and add its metadata to the metadata
     * file in the export folder. The file name will be exactly the same as
     * NODE.NAME, preserving case.
     *
     * @param con
     * @param nodeId
     * @param folder
     * @param ver
     * @return true if the export was successful
     */
    public boolean exportFile(Connection con, int nodeId, File folder, int ver) {
        String localPath = getLocalPath(con, nodeId);
        if (localPath == null) {
            LOGGER.log(Level.SEVERE,
                    "Unable to get local path for node {0}", nodeId);
            return false;
        }
        File localFile = new File(localPath);

        HashMap<String, String> metadata = getMetadata(con, nodeId);
        if (metadata == null) {
            LOGGER.log(Level.SEVERE,
                    "Unable to get metadata for node {0}", nodeId);
            return false;
        }
        String name = metadata.get("NAME");

        String version = Integer.toString(ver);
        if (version.length() == 1) {
            version = "0" + version;
        }
        name = version + "_" + name;

        File exportFile = new File(folder, name);
        
        File mdFile = new File(folder, "METADATA.CSV");
        
        if(!mdFile.exists()) {
        	try {
				mdFile.createNewFile();
				StringBuilder mdBuffer = new StringBuilder();
	        	mdBuffer.append(getHeaders());
				FileWriter mdWriter = new FileWriter(mdFile, true);
	            mdWriter.write(mdBuffer.toString());
	            mdWriter.close();
	        	
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        //FileUtil.writeMetadata(mdFile, name, metadata); // to be deleted
        if (FileUtil.copyFile(localFile, exportFile)) {
            if (!FileUtil.writeMetadata(mdFile, name, metadata)) {
                LOGGER.log(Level.SEVERE,
                        "Unable to write metadata for node: {0} to: {1}",
                        new Object[]{nodeId, mdFile.getPath()});
            }
        } else {
            LOGGER.log(Level.SEVERE,
                    "Unable to copy file from: {0} to: {1}",
                    new Object[]{localPath, exportFile.getPath()});
            return false;
        }
        return true;
    }
}
