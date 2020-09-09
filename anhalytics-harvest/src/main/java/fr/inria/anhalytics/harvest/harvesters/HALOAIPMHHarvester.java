package fr.inria.anhalytics.harvest.harvesters;

import fr.inria.anhalytics.commons.data.BiblioObject;
import fr.inria.anhalytics.commons.exceptions.ServiceException;
import fr.inria.anhalytics.commons.properties.HarvestProperties;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.harvest.parsers.HALOAIPMHDomParser;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * HAL OAI-PMH harvester implementation.
 *
 * @author Achraf
 */
public class HALOAIPMHHarvester extends Harvester {

    private static String OAI_FORMAT = "xml-tei";

    // the api url
    protected String oai_url = "http://api.archives-ouvertes.fr/oai/hal";
    
    // hal url for harvesting from a file list
    private static String halUrl = "https://hal.archives-ouvertes.fr/";
    
    private HALOAIPMHDomParser oaiDom;

    public HALOAIPMHHarvester() {
        super();
        this.oaiDom = new HALOAIPMHDomParser();
    }

    /**
    * Gets results given a date as suggested by OAI-PMH.
    */
    protected void fetchDocumentsByDate(String date) throws MalformedURLException {
        boolean stop = false;
        String tokenn = null;
        while (!stop) {
            String request = String.format("%s/?verb=ListRecords&metadataPrefix=%s&from=%s&until=%s",
                    this.oai_url, OAI_FORMAT, date, date);
            if (HarvestProperties.getCollection() != null) {
                request += String.format("&set=collection:%s", HarvestProperties.getCollection());
            }

            if (tokenn != null) {
                request = String.format("%s/?verb=ListRecords&resumptionToken=%s", this.oai_url, tokenn);
            }
            LOGGER.info("\t Sending: " + request);

            InputStream in = Utilities.request(request);
            grabbedObjects = this.oaiDom.getGrabbedObjects(in);
            saveObjects();

            // token if any:
            tokenn = oaiDom.getToken();
            if (tokenn == null) {
                stop = true;
            }
            try {
                in.close();
            } catch (IOException ioex) {
                throw new ServiceException("Couldn't close opened harvesting stream source.", ioex);
            }
        }
    }

    @Override
    public void fetchAllDocuments() {
        String currentDate = "";
        try {
            for (String date : Utilities.getDates()) {
                LOGGER.info("Extracting publications TEIs for : " + date);
                currentDate = date;
                fetchDocumentsByDate(date);
            }
        } catch (MalformedURLException mue) {
            LOGGER.error(mue.getMessage(), mue);
        } catch (ServiceException se) {
            LOGGER.error(se.getMessage(), se);
            mm.save(currentDate, "blockedHarvestProcess", se.getMessage());
        }
    }
    
     /**
     * Get a list of HAL documents based on a list of HAL ID given in a file.
     * The ID file path is available in the HarvestProperties object.
     */
    /* note: add a source parameter to identify the target repository and
	 * the id type */
    @Override
    public void fetchListDocuments() {
        BufferedReader br = null;
        try {
            // read the file with one hal id per line
            String docID = null;
            br = new BufferedReader(new FileReader(HarvestProperties.getListFile()));
            while ((docID = br.readLine()) != null) {
                if (docID.trim().length() == 0) {
                    continue;
                }
                if (!HarvestProperties.isReset() && mm.isSavedObject(docID, null)) {
                    LOGGER.info("\t\t Already grabbed, Skipping...");
                    continue;
                }
                String teiUrl = halUrl + docID + "/tei";

                // get TEI file 
                String teiString = IOUtils.toString(new URL(teiUrl), "UTF-8");
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                docFactory.setValidating(false);
                Document teiDoc = null;
                try {
                    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                    teiDoc = docBuilder.parse(new ByteArrayInputStream(teiString.getBytes()));
                } catch (SAXException | ParserConfigurationException | IOException e) {
                    e.printStackTrace();
                }
                Element rootElement = teiDoc.getDocumentElement();
                BiblioObject biblioObject = this.oaiDom.processRecord((Element) rootElement);
                if (biblioObject != null) {
                    grabbedObjects.add(biblioObject);
                }
            }
            saveObjects();
        } catch (MalformedURLException mue) {
            LOGGER.error(mue.getMessage(), mue);
        } catch (ServiceException se) {
            LOGGER.error(se.getMessage(), se);
            mm.save("", "blockedHarvestProcess", se.getMessage());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        
    }

    @Override
    public void sample() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
