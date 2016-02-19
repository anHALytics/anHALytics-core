package fr.inria.anhalytics.harvest.properties;

import fr.inria.anhalytics.commons.exceptions.PropertyException;
import java.io.File;
import java.util.Properties;

/**
 * Represents the properties used for the harvesting and tei extraction process.
 *
 * @author achraf
 */
public class HarvestProperties {

    private static String processName;

    private static String fromDate;

    private static String untilDate;

    private static String oaiUrl;

    private static String grobidHost;
    private static String grobidPort;
    
    private static String grobidHome;
    private static String grobidProperties;

    private static String tmpPath;

    private static boolean reset;

    private static int nbThreads = 1;

    public static void init(String properties_filename) {
        Properties props = new Properties();
        try {
            ClassLoader classLoader = HarvestProperties.class.getClassLoader();
            props.load(classLoader.getResourceAsStream(properties_filename));
        } catch (Exception exp) {
            throw new PropertyException("Cannot open file "+properties_filename, exp);
        }
        setOaiUrl(props.getProperty("harvest.oaipmhurl"));
        setGrobidHost(props.getProperty("harvest.grobid_host"));
        setGrobidPort(props.getProperty("harvest.grobid_port"));
        setGrobidHome(props.getProperty("harvest.grobid_home"));
        setGrobidProperties(props.getProperty("harvest.grobid_properties"));
        //check path
        setTmpPath(props.getProperty("harvest.tmpPath"));
        // As grobid process may take a long time we can continue on previous works
        setReset(Boolean.valueOf(props.getProperty("harvest.reset")));
        setTmpPath(props.getProperty("harvest.tmpPath"));
        String threads = props.getProperty("harvest.nbThreads");
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
     * @return the oaiUrl
     */
    public static String getOaiUrl() {
        return oaiUrl;
    }

    /**
     * @param oaiUrl the oaiUrl to set
     */
    public static void setOaiUrl(String oaiurl) {
        oaiUrl = oaiurl;
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
     * @return the reset
     */
    public static boolean isReset() {
        return reset;
    }

    /**
     * @param reset the reset to set
     */
    public static void setReset(boolean reset) {
        reset = reset;
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
     * @return the grobidHome
     */
    public static String getGrobidHome() {
        return grobidHome;
    }

    /**
     * @param aGrobidHome the grobidHome to set
     */
    public static void setGrobidHome(String aGrobidHome) {
        grobidHome = aGrobidHome;
    }

    /**
     * @return the grobidProperties
     */
    public static String getGrobidProperties() {
        return grobidProperties;
    }

    /**
     * @param aGrobidProperties the grobidProperties to set
     */
    public static void setGrobidProperties(String aGrobidProperties) {
        grobidProperties = aGrobidProperties;
    }

}
