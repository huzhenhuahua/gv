package massexport.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains methods for getting an Oracle database connection to the HP Brain
 * Oracle PRO and ITG databases.
 */
public class DBConnection {

    static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());

    /**
     * Close a connection to the Oracle database.
     *
     * @param con
     */
    public static void closeConnection(Connection con) {
        try {
            if (con != null) {
                con.commit();
                con.close();
            }
        } catch (SQLException ex) {
            // ignore
        }
        con = null;
    }

    /**
     * Get a connection to the Oracle database HPBRAIN schema.
     *
     * @return The database connection
     * @throws java.lang.Exception
     */
    public static Connection getConnection() {
        Connection con = null;
        try {
            Class.forName(Constants.DB_DRIVER);
            con = DriverManager.getConnection(
                    Constants.DB_URL, Constants.BRAIN_USER, Constants.BRAIN_PWD);
            con.setAutoCommit(false);
            LOGGER.log(Level.INFO, "{0}: Connection Established",
                    Calendar.getInstance().getTime().toString());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "{0}: Connection Failed : {1}",
                    new Object[]{Calendar.getInstance().getTime().toString(), ex.toString()});
            con = null;
        }
        return con;
    }

    /**
     * Get a connection to the Oracle database HPBRAIN schema.
     *
     * @return The database connection
     * @throws java.lang.Exception
     */
    public static Connection getConnectionWait() {
        Connection con = null;
        do {
            LOGGER.info("Trying to get database connection...");
            con = DBConnection.getConnection();
            if (con == null) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                }
            }
        } while (con == null);
        return con;
    }

    /**
     * Get a connection to the Oracle database BSM schema.
     *
     * @return The database connection
     * @throws java.lang.Exception
     */
    public static Connection getBSMConnection() {
        Connection con = null;
        try {
            Class.forName(Constants.DB_DRIVER);
            con = DriverManager.getConnection(
                    Constants.DB_URL, Constants.BSM_USER, Constants.BSM_PWD);
            con.setAutoCommit(false);
            LOGGER.log(Level.INFO, "{0}: BSM Connection Established",
                    Calendar.getInstance().getTime().toString());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "{0}: BSM Connection Failed : {1}",
                    new Object[]{Calendar.getInstance().getTime().toString(), ex.toString()});
            con = null;
        }
        return con;
    }

    /**
     * Get a connection to the Oracle database BSM schema.
     *
     * @return The database connection
     * @throws java.lang.Exception
     */
    public static Connection getBSMConnectionWait() {
        Connection con = null;
        do {
            LOGGER.info("Trying to get database connection...");
            con = DBConnection.getBSMConnection();
            if (con == null) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                }
            }
        } while (con == null);
        return con;
    }

    /**
     * Get a connection to the Oracle database SYS_TOOLS schema.
     *
     * @return The database connection
     * @throws java.lang.Exception
     */
    public static Connection getTOOLSConnection() {
        Connection con = null;
        try {
            Class.forName(Constants.DB_DRIVER);
            con = DriverManager.getConnection(
                    Constants.DB_URL, Constants.TOOLS_USER, Constants.TOOLS_PWD);
            con.setAutoCommit(false);
            LOGGER.log(Level.INFO, "{0}: BSM Connection Established",
                    Calendar.getInstance().getTime().toString());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "{0}: BSM Connection Failed : {1}",
                    new Object[]{Calendar.getInstance().getTime().toString(), ex.toString()});
            con = null;
        }
        return con;
    }

    /**
     * Get a connection to the Oracle database SYS_TOOLS schema.
     *
     * @return The database connection
     * @throws java.lang.Exception
     */
    public static Connection getTOOLSConnectionWait() {
        Connection con = null;
        do {
            LOGGER.info("Trying to get database connection...");
            con = DBConnection.getTOOLSConnection();
            if (con == null) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                }
            }
        } while (con == null);
        return con;
    }
}
