package massexport.util;

import java.util.Properties;

/**
 * Read constants set in the pro.properties or itg.properties file.
 * Also a few that don't ever change.
 */
public class Constants {

    // never change
    public static String DOCUMENT = "DOCUMENT";
    public static String DOCUMENT_PERMISSION = "2";
    public static String FOLDER = "FOLDER";
    public static String FOLDER_PERMISSION = "3";
    // must be set in the conf.properties file
    public static String EXPORT_DIR = getProperty("exportDir");
    public static String SERVER_ID = getProperty("serverID");
    public static String DB_DRIVER = getProperty("dbDriver");
    public static String DB_URL = getProperty("dbURL");
    public static String BRAIN_USER = getProperty("brainUser");
    public static String BRAIN_PWD = getProperty("brainPwd");
    public static String BSM_USER = getProperty("bsmUser");
    public static String BSM_PWD = getProperty("bsmPwd");
    public static String TOOLS_USER = getProperty("toolsUser");
    public static String TOOLS_PWD = getProperty("toolsPwd");

    private Constants() {
    }

    /**
     * Get a property from the pro.properties or itg.properties file.
     *
     * @param Property key
     * @return Property value
     */
    public static String getProperty(String key) {
        if (confProps == null) {
            if (massexport.MassExport.getSys().equals("pro")) {
                confProps = PropertyLoader.loadProperties("pro.properties");
            }
            else {
                confProps = PropertyLoader.loadProperties("itg.properties");
            }
        }
        if (confProps != null) {
            return confProps.getProperty(key);
        }
        return null;
    }
    public static Properties confProps = null;
}
