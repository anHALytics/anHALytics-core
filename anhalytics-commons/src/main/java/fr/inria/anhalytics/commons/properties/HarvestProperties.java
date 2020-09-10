package fr.inria.anhalytics.commons.properties;

import fr.inria.anhalytics.commons.exceptions.PropertyException;
import fr.inria.anhalytics.commons.utilities.Utilities;
import org.apache.commons.lang3.StringUtils;

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

    private static String tmpPath;

    private static boolean reset;

    private static int nbThreads = 1;

    private static String listFile;

    private static String crossrefId;
    private static String crossrefPwd;
    private static String crossrefHost;
    private static String inputDirectory;
    private static boolean local;
    private static String metadataDirectory;

    private static boolean grobidConsolidateHeader;

    public static void init(String properties_filename) {
        Properties props = new Properties();
        try {
            File file = new File(System.getProperty("user.dir"));
            props.load(new FileInputStream(file.getAbsolutePath() + File.separator + "config" + File.separator + properties_filename));
        } catch (Exception exp) {
            throw new PropertyException("Cannot open file " + properties_filename, exp);
        }

        setSource(props.getProperty("harvest.source"));
        setGrobidHost(props.getProperty("harvest.grobid_host"));
        parseGrobidConsolidateHeader(props.getProperty("harvest.grobid.headerConsolidation"));
        setTmpPath(props.getProperty("harvest.tmpPath"));
        Utilities.checkPath(HarvestProperties.getTmpPath());
        String threads = props.getProperty("harvest.nbThreads");

        setCrossrefId(props.getProperty("harvest.crossref_id"));
        setCrossrefPwd(props.getProperty("harvest.crossref_pw"));
        setCrossrefHost(props.getProperty("harvest.crossref_host"));
        try {
            setNbThreads(Integer.parseInt(threads));
        } catch (NumberFormatException e) {
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

    public static void setProcessName(String processname) {
        processName = processname;
    }

    public static String getFromDate() {
        return fromDate;
    }

    public static void setFromDate(String fromdate) {
        fromDate = fromdate;
    }

    public static String getUntilDate() {
        return untilDate;
    }

    public static void setUntilDate(String untildate) {
        untilDate = untildate;
    }

    public static String getGrobidHost() {
        return grobidHost;
    }

    public static void setGrobidHost(String grobid_host) {
        grobidHost = grobid_host;
    }

    public static String getTmpPath() {
        return tmpPath;
    }

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

    public static void setInputDirectory(String inputDirectory) {
        HarvestProperties.inputDirectory = inputDirectory;
    }

    public static String getInputDirectory() {
        return inputDirectory;
    }

    public static void setLocal(boolean local) {
        HarvestProperties.local = local;
    }

    public static boolean getLocal() {
        return local;
    }

    public static void setMetadataDirectory(String metadataDirectory) {
        HarvestProperties.metadataDirectory = metadataDirectory;
    }

    public static String getMetadataFile() {
        return metadataDirectory;
    }

    public static boolean getGrobidConsolidateHeader() {
        return grobidConsolidateHeader;
    }

    public static void parseGrobidConsolidateHeader(String grobidConsolidateHeader) {
        if (StringUtils.equalsIgnoreCase(grobidConsolidateHeader, "true")) {
            HarvestProperties.grobidConsolidateHeader = true;
        } else {
            HarvestProperties.grobidConsolidateHeader = false;
        }
    }
}
