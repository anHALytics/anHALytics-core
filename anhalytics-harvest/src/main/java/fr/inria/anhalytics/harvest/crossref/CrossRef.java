package fr.inria.anhalytics.harvest.crossref;

import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.harvest.properties.HarvestProperties;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Class for managing the extraction of bibliographical informations from pdf
 * documents.
 *
 * @author Achraf Azhar
 */
public class CrossRef {

    private static final Logger logger = LoggerFactory.getLogger(CrossRef.class);

    /**
     * Lookup by DOI - 3 parameters are id, password, doi.
     */
    private static final String DOI_BASE_QUERY
            = "openurl?url_ver=Z39.88-2004&pid=%s:%s&rft_id=info:doi/%s&noredirect=true&format=unixref";

    /**
     * Lookup by journal title, volume and first page - 6 parameters are id,
     * password, journal title, author, volume, firstPage.
     */
    private static final String JOURNAL_AUTHOR_BASE_QUERY
            = //"query?usr=%s&pwd=%s&type=a&format=unixref&qdata=|%s||%s||%s|||KEY|";
            "query?usr=%s&pwd=%s&type=a&format=unixref&qdata=|%s|%s|%s||%s|||KEY|";

    // ISSN|TITLE/ABBREV|FIRST AUTHOR|VOLUME|ISSUE|START PAGE|YEAR|RESOURCE TYPE|KEY|DOI
    /**
     * Lookup by journal title, volume and first page - 6 parameters are id,
     * password, journal title, volume, firstPage.
     */
    private static final String JOURNAL_BASE_QUERY
            = //"query?usr=%s&pwd=%s&type=a&format=unixref&qdata=|%s||%s||%s|||KEY|";
            "query?usr=%s&pwd=%s&type=a&format=unixref&qdata=|%s||%s||%s|||KEY|";

    /**
     * Lookup first author surname and article title - 4 parameters are id,
     * password, title, author.
     */
    private static final String TITLE_BASE_QUERY
            = "query?usr=%s&pwd=%s&type=a&format=unixref&qdata=%s|%s||key|";

    private MongoFileManager mm;

    private DocumentBuilder docBuilder;

    private XPath xPath = XPathFactory.newInstance().newXPath();

    public CrossRef() throws UnknownHostException {
        this.mm = MongoFileManager.getInstance(false);

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);
        //docFactory.setNamespaceAware(true);
        try {
            docBuilder = docFactory.newDocumentBuilder();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Try to consolidate some uncertain bibliographical data with crossref web
     * service based on core metadata
     */
    public void findDois() {
        String doi = "";
        String aut = "";
        String title = "";
        String journalTitle = "";
        String volume = "";
        String firstPage = "";
        String pageRange = "";
        int beginPage;
        String subpath = "";
        int i = 0;
        for (String date : Utilities.getDates()) {
            if (!HarvestProperties.isProcessByDate()) {
                date = null;
            }
            if (mm.initMetadataTeis(date)) {
                while (mm.hasMoreTeis()) {

                    String metadataString = mm.nextTeiDocument();
                    String currentRepositoryDocId = mm.getCurrentRepositoryDocId();
                    String currentAnhalyticsId = mm.getCurrentAnhalyticsId();

                    InputStream metadataStream = new ByteArrayInputStream(metadataString.getBytes());

                    Document metadata = null;

                    try {
                        System.out.println("###################" + currentRepositoryDocId + "#######################");
                        System.out.println("isWithoutDoi");
                        metadata = docBuilder.parse(metadataStream);
                        metadataStream.close();
                        Element rootElement = metadata.getDocumentElement();
                        Node node = (Node) xPath.compile("text/body/listBibl/biblFull/titleStmt/title")
                                .evaluate(rootElement, XPathConstants.NODE);
                        if (node != null) {
                            title = node.getTextContent();
                        }
                        node = (Element) xPath.compile("text/body/listBibl/biblFull/titleStmt/author/persName/surname").evaluate(rootElement, XPathConstants.NODE);
                        if (node != null) {
                            aut = node.getTextContent();
                        }
                        if (aut != null) {
                            aut = Utilities.removeAccents(aut);
                        }
                        if (title != null) {
                            title = Utilities.removeAccents(title);
                        }

                        if (StringUtils.isNotBlank(title)
                                && StringUtils.isNotBlank(aut)) {
                            System.out.println("test retrieval per title, author");
                            System.out.println(String.format("persName=%s, title=%s", aut, title));
                            subpath = String.format(TITLE_BASE_QUERY,
                                    HarvestProperties.getCrossrefId(),
                                    HarvestProperties.getCrossrefPwd(),
                                    URLEncoder.encode(title, "UTF-8"),
                                    URLEncoder.encode(aut, "UTF-8"));
                            doi = queryCrossref(subpath);
                        }
                        if (doi.isEmpty()) {
                            node = (Element) xPath.compile("text/body/listBibl/biblFull/sourceDesc/biblStruct/monogr/title")
                                    .evaluate(rootElement, XPathConstants.NODE);
                            if (node != null) {
                                journalTitle = node.getTextContent();
                                if (journalTitle != null) {
                                    journalTitle = Utilities.removeAccents(journalTitle);
                                }
                            }
                            node = (Element) xPath.compile("text/body/listBibl/biblFull/sourceDesc/biblStruct/monogr/imprint/biblScope[@unit='pp']")
                                    .evaluate(rootElement, XPathConstants.NODE);
                            if (node != null) {
                                pageRange = node.getTextContent();
                            }
                            if (pageRange != null) {
                                pageRange = pageRange.replaceAll("[A-Za-z.,\\s+]", "");
                                StringTokenizer st = new StringTokenizer(pageRange, "-");
                                if (st.countTokens() == 2) {
                                    firstPage = st.nextToken();
                                } else if (st.countTokens() == 1) {
                                    firstPage = pageRange;
                                }
                            }
                            node = (Element) xPath.compile("text/body/listBibl/biblFull/sourceDesc/biblStruct/monogr/imprint/biblScope[@unit='volume']")
                                    .evaluate(rootElement, XPathConstants.NODE);
                            if (node != null) {
                                volume = node.getTextContent();
                            }
                        }

                        if (doi.isEmpty() && StringUtils.isNotBlank(journalTitle)
                                && StringUtils.isNotBlank(volume)
                                //&& StringUtils.isNotBlank(aut)
                                && StringUtils.isNotBlank(firstPage)) {
                            // retrieval per journal title, author, volume, first page
                            System.out.println("test retrieval per journal title, author, volume, first page");
                            System.out.println(String.format("aut=%s, firstPage=%s, journalTitle=%s, volume=%s",
                                    aut, firstPage, journalTitle, volume));
                            if (StringUtils.isNotBlank(aut)) {
                                subpath = String.format(JOURNAL_AUTHOR_BASE_QUERY,
                                        HarvestProperties.getCrossrefId(),
                                        HarvestProperties.getCrossrefPwd(),
                                        URLEncoder.encode(journalTitle, "UTF-8"),
                                        URLEncoder.encode(aut, "UTF-8"),
                                        URLEncoder.encode(volume, "UTF-8"),
                                        firstPage);
                            } else {
                                subpath = String.format(JOURNAL_BASE_QUERY,
                                        HarvestProperties.getCrossrefId(),
                                        HarvestProperties.getCrossrefPwd(),
                                        URLEncoder.encode(journalTitle, "UTF-8"),
                                        URLEncoder.encode(volume, "UTF-8"),
                                        firstPage);
                            }
                            doi = queryCrossref(subpath);
                        }
                        if (!doi.isEmpty()) {
                            i++;
                            mm.updateDoi(doi);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (!HarvestProperties.isProcessByDate()) {
                    break;
                }
            }
            logger.info("Done");
        }
        System.out.println("nb of found doi : " + i);

    }

    /**
     * Try to consolidate some uncertain bibliographical data with crossref web
     * service based on title and first author.
     *
     * @param biblio the biblio item to be consolidated
     * @param biblioList the list of biblio items found as consolidations
     * @return Returns a boolean indicating whether at least one bibliographical
     * object has been retrieved.
     */
    private String queryCrossref(String query) throws Exception {

        String doi = "";
        // we check if the entry is not already in the DB

        URL url = new URL("http://" + HarvestProperties.getCrossrefHost() + "/" + query);

        System.out.println("Sending: " + url.toString());
        HttpURLConnection urlConn = null;
        try {
            urlConn = (HttpURLConnection) url.openConnection();
        } catch (Exception e) {
            try {
                urlConn = (HttpURLConnection) url.openConnection();
            } catch (Exception e2) {
//						e2.printStackTrace();
                urlConn = null;
                throw new Exception("An exception occured while running Grobid.", e2);
            }
        }
        if (urlConn != null) {
            try {
                urlConn.setDoOutput(true);
                urlConn.setDoInput(true);
                urlConn.setRequestMethod("GET");

                urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                InputStream in = urlConn.getInputStream();

                Document response = docBuilder.parse(in);
                Element root = response.getDocumentElement();
                Element fulltext_metadata = Utilities.getElementByAttribute("publication_type", "full_text", root);
                NodeList nl = fulltext_metadata.getElementsByTagName("doi");
                if (nl != null) {
                    doi = nl.item(0).getTextContent();
                }
                in.close();
                System.out.println("DOI : " + doi);
                urlConn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return doi;
    }
}
