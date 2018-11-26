package fr.inria.anhalytics.commons.properties;

import fr.inria.anhalytics.commons.exceptions.PropertyException;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Represents the properties used for the harvesting and tei extraction process.
 *
 * @author achraf
 */
public class HarvestProperties {

    private static String processName;
    
    private static String collection;

    private static String fromDate;

    private static String untilDate;

    private static boolean processByDate = true;

    private static String source;
    
    private static String grobidHost;
    private static String grobidPort;

    private static String tmpPath;

    private static boolean reset;

    private static int nbThreads = 1;
    
    private static String listFile;

    private static String crossrefId;
    private static String crossrefPwd;
    private static String crossrefHost;

    public static void init(String properties_filename) {
        Properties props = new Properties();
        try {
            File file = new File(System.getProperty("user.dir"));
            props.load(new FileInputStream(file.getAbsolutePath()+File.separator+"config"+File.separator+properties_filename));
        } catch (Exception exp) {
            throw new PropertyException("Cannot open file "+properties_filename, exp);
        }
        
        setSource(props.getProperty("harvest.source"));
        setGrobidHost(props.getProperty("harvest.grobid_host"));
        setGrobidPort(props.getProperty("harvest.grobid_port"));
        setTmpPath(props.getProperty("harvest.tmpPath"));
        Utilities.checkPath(HarvestProperties.getTmpPath());
        String threads = props.getProperty("harvest.nbThreads");
        
        setCrossrefId(props.getProperty("harvest.crossref_id"));
        setCrossrefPwd(props.getProperty("harvest.crossref_pw"));
        setCrossrefHost(props.getProperty("harvest.crossref_host"));
        try {
            setNbThreads(Integer.parseInt(threads));
        } catch (java.lang.NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public static void checkPath(String path) {
        File propertyFile = new File(path);

        // exception if prop file does not exist
        if (!propertyFile.exists()) {
            throw new PropertyException("Could not read harvest.properties, the file '" + path + "' does not exist.");
        }
    }

    /**
     * @return the processName
     */
    public static String getProcessName() {
        return processName;
    }

    /**
     * @param processName the processName to set
     */
    public static void setProcessName(String processname) {
        processName = processname;
    }

    /**
     * @return the fromDate
     */
    public static String getFromDate() {
        return fromDate;
    }

    /**
     * @param fromDate the fromDate to set
     */
    public static void setFromDate(String fromdate) {
        fromDate = fromdate;
    }

    /**
     * @return the untilDate
     */
    public static String getUntilDate() {
        return untilDate;
    }

    /**
     * @param untilDate the untilDate to set
     */
    public static void setUntilDate(String untildate) {
        untilDate = untildate;
    }

    /**
     * @return the grobid_host
     */
    public static String getGrobidHost() {
        return grobidHost;
    }

    /**
     * @param grobid_host the grobid_host to set
     */
    public static void setGrobidHost(String grobid_host) {
        grobidHost = grobid_host;
    }

    /**
     * @return the grobid_port
     */
    public static String getGrobidPort() {
        return grobidPort;
    }

    /**
     * @param grobid_port the grobid_port to set
     */
    public static void setGrobidPort(String grobid_port) {
        grobidPort = grobid_port;
    }

    /**
     * @return the tmpPath
     */
    public static String getTmpPath() {
        return tmpPath;
    }

    /**
     * @param tmpPath the tmpPath to set
     */
    public static void setTmpPath(String tmppath) {
        tmpPath = tmppath;
    }

    /**
     * @return the path of a file giving the list of HAL ID to be harvested
     */
    public static String getListFile() {
        return listFile;
    }

    /**
     * @param list the path of a file giving the list of HAL ID to be harvested
     */
    public static void setListFile(String list) {
        listFile = list;
    }

    /**
     * @return the reset
     */
    public static boolean isReset() {
        return reset;
    }

    /**
     * @param isreset the reset to set
     */
    public static void setReset(boolean isreset) {
        reset = isreset;
    }

    /**
     * @return the nbThreads
     */
    public static int getNbThreads() {
        return nbThreads;
    }

    /**
     * @param aNbThreads the nbThreads to set
     */
    public static void setNbThreads(int aNbThreads) {
        nbThreads = aNbThreads;
    }

    /**
     * @return the processByDate
     */
    public static boolean isProcessByDate() {
        return processByDate;
    }

    /**
     * @param aProcessByDate the processByDate to set
     */
    public static void setProcessByDate(boolean aProcessByDate) {
        processByDate = aProcessByDate;
    }

    /**
     * @return the source
     */
    public static String getSource() {
        return source;
    }

    /**
     * @param aSource the source to set
     */
    public static void setSource(String aSource) {
        source = aSource;
    }

    /**
     * @return the collection
     */
    public static String getCollection() {
        return collection;
    }

    /**
     * @param aCollection the collection to set
     */
    public static void setCollection(String aCollection) {
        collection = aCollection;
    }

        /**
     * @return the crossrefId
     */
    public static String getCrossrefId() {
        return crossrefId;
    }

    /**
     * @param aCrossrefId the crossrefId to set
     */
    public static void setCrossrefId(String aCrossrefId) {
        crossrefId = aCrossrefId;
    }

    /**
     * @return the crossrefPwd
     */
    public static String getCrossrefPwd() {
        return crossrefPwd;
    }

    /**
     * @param aCrossrefPwd the crossrefPwd to set
     */
    public static void setCrossrefPwd(String aCrossrefPwd) {
        crossrefPwd = aCrossrefPwd;
    }

    /**
     * @return the crossrefHost
     */
    public static String getCrossrefHost() {
        return crossrefHost;
    }

    /**
     * @param aCrossrefHost the crossrefHost to set
     */
    public static void setCrossrefHost(String aCrossrefHost) {
        crossrefHost = aCrossrefHost;
    }
}
