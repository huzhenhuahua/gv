package massexport;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class for mass export program.
 */
public class MassExport {

    static final Logger LOGGER = Logger.getLogger(MassExport.class.getName());

    /**
     * Main entry point for mass export program. Takes the system type as the
     * first argument. Takes an HP Brain folder node id as the second argument.
     * If the node id doesn't match a folder node id, the program exits immediately.
     * An optional third argument can be used to tell the program to also export
     * all versions of files that are found. An optional fourth argument can be
     * used to tell the program to also export deleted files. If exporting deleted
     * files, and versions of files are also being exported, then deleted versions
     * will also be exported.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    	/*args = new String[4];
    	args[0] = "itg";
    	args[1] = "146841044";//"218550";//"716";
    	args[2] = "yes";
    	args[3] = "yes";
   
    	*/
    	
    	
        if (args.length < 2) {
            System.out.println("Parameters required:");
            System.out.println("[pro|itg] [folder node id] [no|yes] [no|yes]");
            System.out.println("e.g. pro 12345 no no");
            System.exit(-1);
        }

        if (args.length >= 2) {
            if (args[0].toLowerCase().startsWith("pro")) {
                setSys("pro");
            } // else it's itg

            try {
                setFolderId(Integer.parseInt(args[1]));
            } catch (NumberFormatException e) {
                System.out.println("Folder node id " + args[1] + " is not a number");
                System.exit(-1);
            }

            if (args.length >= 2) {
                boolean exportVersions = (args[2].toLowerCase().startsWith("y")) ? true : false;
                setExportVersions(exportVersions);

                if (args.length >= 3) {
                    boolean exportDeleted = (args[3].toLowerCase().startsWith("y")) ? true : false;
                    setExportDeleted(exportDeleted);
                }
            }
        }

        execute();
    }

    private MassExport() {
    }

    public static void setSys(String sys) {
        _sys = sys;
    }
    public static String getSys() {
        return _sys;
    }
    private static String _sys = "itg";

    public static void setFolderId(int folderId) {
        _folderId = folderId;
    }
    public static int getFolderId() {
        return _folderId;
    }
    private static int _folderId = 0;

    public static void setExportVersions(boolean exportVersions) {
        _exportVersions = exportVersions;
    }
    public static boolean getExportVersions() {
        return _exportVersions;
    }
    private static boolean _exportVersions = false;

    public static void setExportDeleted(boolean exportDeleted) {
        _exportDeleted = exportDeleted;
    }
    public static boolean getExportDeleted() {
        return _exportDeleted;
    }
    private static boolean _exportDeleted = false;

    /**
     * Executes the export.
     * @return 0 if success, -1 if failed
     */
    public static void execute() {
        DumpTree dt = new DumpTree(_folderId, _exportVersions, _exportDeleted);
        dt.doit();
    }
}
