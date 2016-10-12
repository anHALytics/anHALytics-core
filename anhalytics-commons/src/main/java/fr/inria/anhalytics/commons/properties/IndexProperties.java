package fr.inria.anhalytics.commons.properties;

import fr.inria.anhalytics.commons.exceptions.PropertyException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 *
 * @author achraf
 */
public class IndexProperties {

    private static String processName;

    private static String elasticSearch_host;
    private static String elasticSearch_port;

    private static String elasticSearchClusterName;

    private static String nerdAnnotsIndexName = "annotations_nerd";
    private static String keytermAnnotsIndexName = "annotations_keyterm";
    private static String fulltextTeisIndexName  = "anhalytics_fulltextteis_in";
    private static String metadataTeisIndexName  = "anhalytics_metadatateis";
    private static String kbIndexName = "anhalytics_kb"; // to be rename, it's not metadata but KB

    private static String fromDate;

    private static String untilDate;

    private static boolean processByDate = true;

    public static void init(String properties_filename) {
        Properties props = new Properties();
        try {
            File file = new File(System.getProperty("user.dir"));
            props.load(new FileInputStream(file.getAbsolutePath()+File.separator+"config"+File.separator+properties_filename));
        } catch (Exception exp) {
            throw new PropertyException("Cannot open file " + properties_filename, exp);
        }
        setElasticSearch_host(props.getProperty("index.elasticSearch_host"));
        setElasticSearch_port(props.getProperty("index.elasticSearch_port"));
        setElasticSearchClusterName(props.getProperty("index.elasticSearch_cluster"));
        System.out.println(elasticSearchClusterName);
        setNerdAnnotsIndexName(props.getProperty("index.elasticSearch_nerdAnnotsIndexName"));
        setKeytermAnnotsIndexName(props.getProperty("index.elasticSearch_keytermAnnotsIndexName"));
        setFulltextTeisIndexName(props.getProperty("index.elasticSearch_fulltextTeisIndexName"));
        setMetadataTeisIndexName(props.getProperty("index.elasticSearch_metadataTeisIndexName"));
        setKbIndexName(props.getProperty("index.elasticSearch_kbIndexName"));
    }

    private static void checkPath(String path) {
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
     * @return the elasticSearch_host
     */
    public static String getElasticSearch_host() {
        return elasticSearch_host;
    }

    /**
     * @param aElasticSearch_host the elasticSearch_host to set
     */
    public static void setElasticSearch_host(String aElasticSearch_host) {
        elasticSearch_host = aElasticSearch_host;
    }

    /**
     * @return the elasticSearch_port
     */
    public static String getElasticSearch_port() {
        return elasticSearch_port;
    }

    /**
     * @param aElasticSearch_port the elasticSearch_port to set
     */
    public static void setElasticSearch_port(String aElasticSearch_port) {
        elasticSearch_port = aElasticSearch_port;
    }

    /**
     * @return the ElasticSearch cluster name
     */
    public static String getElasticSearchClusterName() {
        return elasticSearchClusterName;
    }

    /**
     * @param aElasticSearchClusterName the ElasticSearch cluster name to set
     */
    public static void setElasticSearchClusterName(String aElasticSearchClusterName) {
        elasticSearchClusterName = aElasticSearchClusterName;
    }

    /**
     * @return the NERD annotation index name
     */
    public static String getNerdAnnotsIndexName() {
        return nerdAnnotsIndexName;
    }

    /**
     * @param aNerdAnnotsIndexName the NERD annotation index name to set
     */
    public static void setNerdAnnotsIndexName(String aNerdAnnotsIndexName) {
        nerdAnnotsIndexName = aNerdAnnotsIndexName;
    }
	
    /**
     * @return the keyterm annotation index name
     */
    public static String getKeytermAnnotsIndexName() {
        return keytermAnnotsIndexName;
    }

    /**
     * @param aKeytermAnnotsIndexName the keyterm annotation index name to set
     */
    public static void setKeytermAnnotsIndexName(String aKeytermAnnotsIndexName) {
        keytermAnnotsIndexName = aKeytermAnnotsIndexName;
    }


    /**
     * @return the fromDate
     */
    public static String getFromDate() {
        return fromDate;
    }

    /**
     * @param aFromDate the fromDate to set
     */
    public static void setFromDate(String aFromDate) {
        fromDate = aFromDate;
    }

    /**
     * @return the untilDate
     */
    public static String getUntilDate() {
        return untilDate;
    }

    /**
     * @param aUntilDate the untilDate to set
     */
    public static void setUntilDate(String aUntilDate) {
        untilDate = aUntilDate;
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
     * @return the fulltextTeisIndexName
     */
    public static String getFulltextTeisIndexName() {
        return fulltextTeisIndexName;
    }

    /**
     * @param aFulltextTeisIndexName the fulltextTeisIndexName to set
     */
    public static void setFulltextTeisIndexName(String aFulltextTeisIndexName) {
        fulltextTeisIndexName = aFulltextTeisIndexName;
    }

    /**
     * @return the metadataTeisIndexName
     */
    public static String getMetadataTeisIndexName() {
        return metadataTeisIndexName;
    }

    /**
     * @param aMetadataTeisIndexName the metadataTeisIndexName to set
     */
    public static void setMetadataTeisIndexName(String aMetadataTeisIndexName) {
        metadataTeisIndexName = aMetadataTeisIndexName;
    }

    /**
     * @return the kbIndexName
     */
    public static String getKbIndexName() {
        return kbIndexName;
    }

    /**
     * @param aKbIndexName the kbIndexName to set
     */
    public static void setKbIndexName(String aKbIndexName) {
        kbIndexName = aKbIndexName;
    }
    

}
