package fr.inria.anhalytics.harvest;

import fr.inria.anhalytics.commons.data.PubFile;
import fr.inria.anhalytics.commons.data.TEI;
import fr.inria.anhalytics.commons.exceptions.BinaryNotAvailableException;
import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author Achraf
 */
abstract class Harvester {
    protected static final Logger logger = LoggerFactory.getLogger(Harvester.class);
    
    public Harvester() throws UnknownHostException {
        this.mm = MongoFileManager.getInstance(false);
    }
    
    protected MongoFileManager mm;
    /**
     * Harvests the documents submitted on the given date.
     */
    public void fetchDocumentsByDate(String date) throws IOException, SAXException, ParserConfigurationException, ParseException{}
    /**
     * Harvests all the repository.
     */
    public void fetchAllDocuments() throws IOException, SAXException, ParserConfigurationException, ParseException{}
    
    /**
     * Stores the given teis and downloads attachements(main file(s), annexes ..) . 
     */
    protected void processTeis(List<TEI> teis, String date, boolean withAnnexes) {
        for (TEI tei : teis) {
            try {
                String teiFilename = tei.getId() + ".tei.xml";
                    logger.debug("\t\t Extracting tei.... for " + tei.getId());
                    String teiString = tei.getTei();
                    if (teiString.length() > 0) {
                        logger.debug("\t\t\t\t Storing tei : " + tei.getId());
                        mm.addDocument(new ByteArrayInputStream(teiString.getBytes()), teiFilename, MongoCollectionsInterface.ADDITIONAL_TEIS, date);
                        String filename = tei.getId() + ".pdf";
                        //binary processing.
                        if (tei.getFile() != null) {
                            System.out.println(filename);
                            downloadFile(tei.getFile(), tei.getId(), date);
                        } else {
                            mm.save(tei.getId(), "harvestProcess", "no file url", null);
                            logger.debug("\t\t\t PDF not found !");
                        }
                        if(withAnnexes)
                            downloadAnnexes(tei.getAnnexes(), tei.getId(), date);
                } else {
                    logger.debug("\t\t\t Tei not found !!!");
                }
            } catch (BinaryNotAvailableException bna) {
                mm.save(tei.getId(), "harvestProcess", "file not available", null);
            } catch (Exception e) {
                mm.save(tei.getId(), "harvestProcess", "harvest error", null);
                e.printStackTrace();
            }
        }
    }
        
    /**
     * Downloads publication annexes and stores them.
     */
    protected void downloadAnnexes(List<PubFile> annexes, String id, String date) throws ParseException, IOException {
        //annexes
         for (PubFile file : annexes) {
             downloadFile(file, id, date);
             // diagnose annexes (not found)?
         }
    }
    
    /**
     * Downloads the given file and classify it either as main file or as an
     * annex.
     */
    protected void downloadFile(PubFile file, String id, String date) throws ParseException, IOException {
        InputStream inBinary = null;
        Date embDate = Utilities.parseStringDate(file.getEmbargoDate());
        Date today = new Date();
        if (embDate.before(today) || embDate.equals(today)) {
            logger.debug("\t\t\t Downloading: " + file.getUrl());
            inBinary = Utilities.request(file.getUrl(), false);
            if (inBinary == null) {
                mm.save(id, "no stream/"+file.getType(), file.getUrl(), date);
            } else {
                if ((file.getType()).equals("file")) {
                    mm.addDocument(inBinary, id + ".pdf", MongoCollectionsInterface.BINARIES, date);
                } else {
                    int n = file.getUrl().lastIndexOf("/");
                    String filename = file.getUrl().substring(n + 1);
                    System.out.println(filename);
                    mm.addAnnexDocument(inBinary, file.getType(), id, filename, MongoCollectionsInterface.PUB_ANNEXES, date);
                }
                inBinary.close();
            }
        } else {
            mm.save(id, "embargo", file.getUrl(), file.getEmbargoDate());
            logger.debug("\t\t\t file under embargo !");
        }
    }
}
