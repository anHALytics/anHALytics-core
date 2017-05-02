package fr.inria.anhalytics.annotate;

import fr.inria.anhalytics.annotate.services.KeyTermExtractionService;
import fr.inria.anhalytics.commons.data.BiblioObject;
import fr.inria.anhalytics.commons.data.Processings;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.IOUtils;

/**
 * Runnable that uses the NERD REST service for annotating HAL TEI
 * documents.Resulting JSON annotations are then stored in MongoDB as persistent
 * storage.
 *
 * The content of every TEI elements having an attribute @xml:id randomly
 * generated will be annotated. The annotations follow a stand-off
 * representation that is using the @xml:id as base and offsets to identified
 * the annotated chunk of text.
 *
 * @author Achraf, Patrice
 */
public class KeyTermAnnotatorWorker extends AnnotatorWorker {

    private static final Logger logger = LoggerFactory.getLogger(KeyTermAnnotatorWorker.class);

    public KeyTermAnnotatorWorker(MongoFileManager mongoManager,
            BiblioObject biblioObject) {
        super(mongoManager, biblioObject, MongoCollectionsInterface.KEYTERM_ANNOTATIONS);
    }

    @Override
    protected void processCommand() {
        try {
            boolean inserted = mm.insertAnnotation(annotateDocument(), annotationsCollection);

            if (inserted) {
                mm.updateBiblioObjectStatus(biblioObject, Processings.KEYTERM, false);
                logger.info("\t\t " + Thread.currentThread().getName() + ": " + biblioObject.getRepositoryDocId() + " annotated by the KeyTerm extraction and disambiguation service.");
            } else {
                logger.info("\t\t " + Thread.currentThread().getName() + ": "
                        + biblioObject.getRepositoryDocId() + " error occured trying to annotate Keyterms.");
            }
        } catch (Exception ex) {
            logger.error("\t\t " + Thread.currentThread().getName() + ": TEI could not be processed by the keyterm extractor: " + biblioObject.getRepositoryDocId());
            ex.printStackTrace();
        }
    }

    /**
     * Annotation of a complete document with extracted disambiguated key terms
     */
    @Override
    protected String annotateDocument() {
        // NOTE: the part bellow should be used in the future for improving the keyterm disambiguation 
        // by setting a custom domain context which helps the disambiguation (so don't remove it ;)

        /*List<String> halDomainTexts = new ArrayList<String>();
        List<String> halDomains = new ArrayList<String>();
        List<String> meSHDescriptors = new ArrayList<String>();

        // get the HAL domain 
        NodeList classes = docTei.getElementsByTagName("classCode");
        for (int p = 0; p < classes.getLength(); p++) {
            Node node = classes.item(p);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) (node);
                // filter on attribute @scheme="halDomain"
                String scheme = e.getAttribute("scheme");
                if ((scheme != null) && scheme.equals("halDomain")) {
                    halDomainTexts.add(e.getTextContent());
                    String n_att = e.getAttribute("n");
                    halDomains.add(n_att);
                } else if ((scheme != null) && scheme.equals("mesh")) {
                    meSHDescriptors.add(e.getTextContent());
                }
            }
        }*/
        StringBuffer json = new StringBuffer();
        try {
            json.append("{ \"repositoryDocId\" : \"" + biblioObject.getRepositoryDocId()
                    + "\",\"anhalyticsId\" : \"" + biblioObject.getAnhalyticsId()
                    + "\",\"isIndexed\" : \"" + false
                    + "\", \"keyterm\" : ");
            String jsonText = null;
            KeyTermExtractionService keyTermService = new KeyTermExtractionService(IOUtils.toInputStream(biblioObject.getGrobidTei(), "UTF-8"));
            jsonText = keyTermService.runKeyTermExtraction();
            if (jsonText != null) {
                json.append(jsonText).append("}");
            } else {
                json.append("{} }");
            }
        } catch (IOException e) {
            logger.error("\t\t " + Thread.currentThread().getName() + ": TEI could not be processed by the keyterm extractor: " + biblioObject.getRepositoryDocId());
            e.printStackTrace();
        }
        return json.toString();
    }
}
