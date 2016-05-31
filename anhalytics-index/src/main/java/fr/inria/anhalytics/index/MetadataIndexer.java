package fr.inria.anhalytics.index;

import fr.inria.anhalytics.commons.exceptions.ElasticSearchConfigurationException;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.dao.AbstractDAOFactory;
import fr.inria.anhalytics.dao.AddressDAO;
import fr.inria.anhalytics.dao.Conference_EventDAO;
import fr.inria.anhalytics.dao.DocumentDAO;
import fr.inria.anhalytics.dao.In_SerialDAO;
import fr.inria.anhalytics.dao.MonographDAO;
import fr.inria.anhalytics.dao.PersonDAO;
import fr.inria.anhalytics.dao.PublicationDAO;
import fr.inria.anhalytics.index.properties.IndexProperties;
import fr.inria.anhalytics.kb.dao.anhalytics.DAOFactory;
import fr.inria.anhalytics.kb.dao.anhalytics.OrganisationDAO;
import fr.inria.anhalytics.kb.dao.biblio.AbstractBiblioDAOFactory;
import fr.inria.anhalytics.kb.dao.biblio.BiblioDAOFactory;
import fr.inria.anhalytics.kb.entities.Address;
import fr.inria.anhalytics.kb.entities.Affiliation;
import fr.inria.anhalytics.kb.entities.Conference_Event;
import fr.inria.anhalytics.kb.entities.Document;
import fr.inria.anhalytics.kb.entities.In_Serial;
import fr.inria.anhalytics.kb.entities.Organisation;
import fr.inria.anhalytics.kb.entities.PART_OF;
import fr.inria.anhalytics.kb.entities.Person;
import fr.inria.anhalytics.kb.entities.Publication;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author achraf
 */
public class MetadataIndexer {

    private static final Logger logger = LoggerFactory.getLogger(MetadataIndexer.class);

    private static final AbstractDAOFactory adf = AbstractDAOFactory.getFactory(AbstractDAOFactory.DAO_FACTORY);
    private static final AbstractBiblioDAOFactory biblioadf = AbstractBiblioDAOFactory.getFactory(AbstractBiblioDAOFactory.DAO_FACTORY);

    private Client client;

    public MetadataIndexer() {

        DAOFactory.initConnection();
        BiblioDAOFactory.initConnection();
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", IndexProperties.getElasticSearchClusterName()).build();
        this.client = new TransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(IndexProperties.getElasticSearch_host(), 9300));
    }

    public int indexAuthors() {
        PersonDAO pdao = (PersonDAO) adf.getPersonDAO();
        OrganisationDAO odao = (OrganisationDAO) adf.getOrganisationDAO();
        AddressDAO adao = (AddressDAO) adf.getAddressDAO();
        DocumentDAO ddao = (DocumentDAO) adf.getDocumentDAO();
        Map<Long, List<Person>> persons = pdao.findAllAuthors();
        int p = 0;
        Iterator it = persons.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Long personId = (Long) pair.getKey();
            List<Person> pers = (List<Person>) pair.getValue();
            Map<String, Object> jsonDocument = pers.get(0).getPersonDocument();
            List<Map<String, Object>> fullnames = new ArrayList<Map<String, Object>>();
            for (int i = 0; i <= pers.size() - 1; i++) {
                Map<String, Object> fullname = new HashMap<String, Object>();
                fullname.put("fullname", pers.get(i).getFullname());
                fullname.put("date", Utilities.formatDate(pers.get(i).getPublication_date()));
                fullnames.add(fullname);
            }
            jsonDocument.put("fullnames", fullnames);
            List<Affiliation> affs = odao.getAffiliationByPersonID(pers.get(0));
            List<Map<String, Object>> organisations = new ArrayList<Map<String, Object>>();
            for (Affiliation aff : affs) {

                Map<String, Object> orgDocument = aff.getOrganisations().get(0).getOrganisationDocument();

                orgDocument.put("begin_date", Utilities.formatDate(aff.getBegin_date()));
                orgDocument.put("end_date", Utilities.formatDate(aff.getBegin_date()));
                Address addr = (adao.getOrganisationAddress(aff.getOrganisations().get(0).getOrganisationId()));
                Map<String, Object> orgAddress = null;
                if (addr != null) {
                    orgAddress = addr.getAddressDocument();
                }
                orgDocument.put("address", orgAddress);
                organisations.add(orgDocument);
            }
            List<Map<String, Object>> publications = new ArrayList<Map<String, Object>>();
            for (Document doc : ddao.getDocumentsByAuthorId(personId)) {
                publications.add(doc.getDocumentDocument());
            }
            jsonDocument.put("publications", publications);
            jsonDocument.put("affiliations", organisations);
            client.prepareIndex(IndexProperties.getKbIndexName(), "authors", "" + personId)
                    .setSource(jsonDocument).execute().actionGet();
            p++;
        }
        return p;
    }

    public int indexPublications() {
        int p = 0;
        DAOFactory.initConnection();
        PublicationDAO pubdao = (PublicationDAO) adf.getPublicationDAO();
        PublicationDAO bibliopubdao = (PublicationDAO) biblioadf.getPublicationDAO();
        DocumentDAO ddao = (DocumentDAO) adf.getDocumentDAO();
        DocumentDAO biblioddao = (DocumentDAO) biblioadf.getDocumentDAO();
        In_SerialDAO bibliinsddao = (In_SerialDAO) biblioadf.getIn_SerialDAO();
        PersonDAO pdao = (PersonDAO) adf.getPersonDAO();
        PersonDAO bibliopdao = (PersonDAO) biblioadf.getPersonDAO();
        MonographDAO mdao = (MonographDAO) adf.getMonographDAO();
        Conference_EventDAO ced = (Conference_EventDAO) adf.getConference_EventDAO();
        List<Document> documents = ddao.findAllDocuments();
        for (Document doc : documents) {
            Map<String, Object> documentDocument = doc.getDocumentDocument();

            Map<Long, List<Person>> authors = pdao.getAuthorsByDocId(doc.getDocID());
            List<Map<String, Object>> authorsDocument = new ArrayList<Map<String, Object>>();
            Iterator it = authors.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                Long personId = (Long) pair.getKey();
                List<Person> pers = (List<Person>) pair.getValue();
                Map<String, Object> jsonDocument = pers.get(0).getPersonDocument();
                List<Map<String, Object>> fullnames = new ArrayList<Map<String, Object>>();
                for (int i = 0; i <= pers.size() - 1; i++) {
                    Map<String, Object> fullname = new HashMap<String, Object>();
                    fullname.put("fullname", pers.get(i).getFullname());
                    fullname.put("date", Utilities.formatDate(pers.get(i).getPublication_date()));
                    fullnames.add(fullname);
                }
                jsonDocument.put("fullnames", fullnames);
                authorsDocument.add(jsonDocument);
            }
            documentDocument.put("authors", authorsDocument);
            List<Publication> pubs = pubdao.findByDocId(doc.getDocID());
            Map<String, Object> publicationDocument = pubs.get(0).getPublicationDocument();
            Map<String, Object> monographDocument = (HashMap<String, Object>) publicationDocument.get("monograph");
            Conference_Event conf = ced.findByMonograph(pubs.get(0).getMonograph().getMonographID());
            if (conf != null) {
                monographDocument.put("conference", conf.getConference_EventDocument());
            }
            documentDocument.put("publication", publicationDocument);
            Map<Long, List<Person>> editors = pdao.getEditorsByPubId(pubs.get(0).getPublicationID());//suppose one-one relation..
            List<Map<String, Object>> editorsDocument = new ArrayList<Map<String, Object>>();
            Iterator it1 = editors.entrySet().iterator();
            while (it1.hasNext()) {
                Map.Entry pair = (Map.Entry) it1.next();
                Long personId = (Long) pair.getKey();
                List<Person> pers = (List<Person>) pair.getValue();
                Map<String, Object> jsonDocument = pers.get(0).getPersonDocument();
                List<Map<String, Object>> fullnames = new ArrayList<Map<String, Object>>();
                for (int i = 0; i <= pers.size() - 1; i++) {
                    Map<String, Object> fullname = new HashMap<String, Object>();
                    fullname.put("fullname", pers.get(i).getFullname());
                    fullname.put("date", Utilities.formatDate(pers.get(i).getPublication_date()));
                    fullnames.add(fullname);
                }
                jsonDocument.put("fullnames", fullnames);
                editorsDocument.add(jsonDocument);
            }
            documentDocument.put("editors", editorsDocument);

            //document_organisation
            //biblioadf  references
            Document docRef = biblioddao.find(doc.getDocID());

            List<Map<String, Object>> referencesPubDocument = new ArrayList<Map<String, Object>>();
            if (docRef != null) {
                List<Publication> referencesPub = bibliopubdao.findByDocId(docRef.getDocID());

                for (Publication referencePub : referencesPub) {
                    In_Serial in = bibliinsddao.find(referencePub.getMonograph().getMonographID());
                    Map<String, Object> referencePubDocument = referencePub.getPublicationDocument();
                    referencePubDocument.put("journal", in.getJ().getJournalDocument());
                    referencePubDocument.put("collection", in.getC().getCollectionDocument());
                    referencePubDocument.put("issue", in.getIssue());
                    referencePubDocument.put("volume", in.getVolume());
                    Map<Long, List<Person>> referenceAuthors = bibliopdao.getEditorsByPubId(referencePub.getPublicationID());

                    List<Map<String, Object>> referenceAuthorsDocument = new ArrayList<Map<String, Object>>();
                    Iterator it2 = referenceAuthors.entrySet().iterator();
                    while (it2.hasNext()) {
                        Map.Entry pair = (Map.Entry) it2.next();
                        Long personId = (Long) pair.getKey();
                        List<Person> pers = (List<Person>) pair.getValue();
                        Map<String, Object> jsonDocument = pers.get(0).getPersonDocument();
                        List<Map<String, Object>> fullnames = new ArrayList<Map<String, Object>>();
                        for (int i = 0; i <= pers.size() - 1; i++) {
                            Map<String, Object> fullname = new HashMap<String, Object>();
                            fullname.put("fullname", pers.get(i).getFullname());
                            fullname.put("date", Utilities.formatDate(pers.get(i).getPublication_date()));
                            fullnames.add(fullname);
                        }
                        jsonDocument.put("fullnames", fullnames);
                        referenceAuthorsDocument.add(jsonDocument);
                    }

                    referencePubDocument.put("authors", referenceAuthorsDocument);
                    referencesPubDocument.add(referencePubDocument);
                }
            }
            documentDocument.put("references", referencesPubDocument);
            client.prepareIndex(IndexProperties.getKbIndexName(), "publications", "" + doc.getDocID())
                    .setSource(documentDocument).execute().actionGet();
            p++;
        }
        return p;
    }

    public int indexOrganisations() {
        int p = 0;
        OrganisationDAO odao = (OrganisationDAO) adf.getOrganisationDAO();
        List<Organisation> organisations = odao.findAllOrganisations();
        DocumentDAO ddao = (DocumentDAO) adf.getDocumentDAO();
        AddressDAO adao = (AddressDAO) adf.getAddressDAO();
        PersonDAO pdao = (PersonDAO) adf.getPersonDAO();
        for (Organisation org : organisations) {
            Map<String, Object> organisationDocument = org.getOrganisationDocument();
            Address addr = (adao.getOrganisationAddress(org.getOrganisationId()));
            Map<String, Object> addressDocument = null;
            if (addr != null) {
                addressDocument = addr.getAddressDocument();
            }
            organisationDocument.put("address", addressDocument);
            List<Map<String, Object>> orgRelationsDocument = new ArrayList<Map<String, Object>>();
            for (PART_OF partOf : org.getRels()) {
                Map<String, Object> motherOrganisationDocument = partOf.getOrganisation_mother().getOrganisationDocument();
                Address motheraddr = (adao.getOrganisationAddress(partOf.getOrganisation_mother().getOrganisationId()));
                Map<String, Object> motheraddressDocument = null;
                if (motheraddr != null) {
                    motheraddressDocument = motheraddr.getAddressDocument();
                }
                motherOrganisationDocument.put("address", motheraddressDocument);
                orgRelationsDocument.add(motherOrganisationDocument);
            }

            organisationDocument.put("relations", orgRelationsDocument);
            List<Document> docs = ddao.getDocumentsByOrgId(org.getOrganisationId());
            List<Map<String, Object>> orgDocumentsDocument = new ArrayList<Map<String, Object>>();
            for (Document doc : docs) {
                orgDocumentsDocument.add(doc.getDocumentDocument());
            }
            organisationDocument.put("publications", orgDocumentsDocument);

            Map<Long, List<Person>> authors = pdao.getPersonsByOrgID(org.getOrganisationId());
            List<Map<String, Object>> authorsDocument = new ArrayList<Map<String, Object>>();

            Iterator it2 = authors.entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry pair = (Map.Entry) it2.next();
                Long personId = (Long) pair.getKey();
                List<Person> pers = (List<Person>) pair.getValue();
                Map<String, Object> jsonDocument = pers.get(0).getPersonDocument();
                List<Map<String, Object>> fullnames = new ArrayList<Map<String, Object>>();
                for (int i = 0; i <= pers.size() - 1; i++) {
                    Map<String, Object> fullname = new HashMap<String, Object>();
                    fullname.put("fullname", pers.get(i).getFullname());
                    fullname.put("date", Utilities.formatDate(pers.get(i).getPublication_date()));
                    fullnames.add(fullname);
                }
                jsonDocument.put("fullnames", fullnames);

                authorsDocument.add(jsonDocument);
            }
            organisationDocument.put("authors", authorsDocument);
            client.prepareIndex(IndexProperties.getKbIndexName(), "organisations", "" + org.getOrganisationId())
                    .setSource(organisationDocument).execute().actionGet();
            p++;
        }
        return p;
    }

    /**
     * set-up ElasticSearch by loading the mapping and river json for the HAL
     * document database
     */
    public void setUpIndex(String indexName) {
        try {
            // delete previous index
            deleteIndex(indexName);

            // create new index and load the appropriate mapping
            createIndex(indexName);
            loadMapping(indexName);
        } catch (Exception e) {
            logger.error("Sep-up of ElasticSearch failed for HAL index.", e);
            e.printStackTrace();
        }
    }

    /**
     *
     */
    private boolean deleteIndex(String indexName) throws Exception {
        boolean val = false;
        try {
            String urlStr = "http://" + IndexProperties.getElasticSearch_host() + ":" + IndexProperties.getElasticSearch_port() + "/" + indexName;
            URL url = new URL(urlStr);
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setDoOutput(true);
            httpCon.setRequestProperty(
                    "Content-Type", "application/x-www-form-urlencoded");
            httpCon.setRequestMethod("DELETE");
            httpCon.connect();
            logger.info("ElasticSearch Index " + indexName + " deleted: status is "
                    + httpCon.getResponseCode());
            if (httpCon.getResponseCode() == 200) {
                val = true;
            }
            httpCon.disconnect();
        } catch (Exception e) {
            throw new Exception("Cannot delete index for " + indexName);
        }
        return val;
    }

    /**
     *
     */
    private boolean createIndex(String indexName) throws IOException {
        boolean val = false;

        // create index
        String urlStr = "http://" + IndexProperties.getElasticSearch_host() + ":" + IndexProperties.getElasticSearch_port() + "/" + indexName;
        URL url = null;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException ex) {
            java.util.logging.Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
        }
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setDoOutput(true);
        httpCon.setRequestProperty(
                "Content-Type", "application/x-www-form-urlencoded");
        try {
            httpCon.setRequestMethod("PUT");
        } catch (ProtocolException ex) {
            java.util.logging.Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
        }

        /*System.out.println("ElasticSearch Index " + indexName + " creation: status is " + 
         httpCon.getResponseCode());
         if (httpCon.getResponseCode() == 200) {
         val = true;
         }*/
        // load custom analyzer
        String analyserStr = null;
        try {
            ClassLoader classLoader = Indexer.class.getClassLoader();
            analyserStr = IOUtils.toString(classLoader.getResourceAsStream("elasticSearch/analyzer.json"));
        } catch (Exception e) {
            throw new ElasticSearchConfigurationException("Cannot read analyzer for " + indexName);
        }

        httpCon.setDoOutput(true);
        httpCon.setRequestMethod("PUT");
        httpCon.addRequestProperty("Content-Type", "text/json");
        OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
        out.write(analyserStr);
        out.close();

        logger.info("ElasticSearch analyzer for " + indexName + " : status is "
                + httpCon.getResponseCode());
        if (httpCon.getResponseCode() == 200) {
            val = true;
        }

        httpCon.disconnect();
        return val;
    }

    /**
     *
     */
    private boolean loadMapping(String indexName) throws Exception {
        boolean val = false;

        String urlStr = "http://" + IndexProperties.getElasticSearch_host() + ":" + IndexProperties.getElasticSearch_port() + "/" + indexName;
        if (indexName.contains("annotation")) {
            urlStr += "/annotation/_mapping";
        } else {
            urlStr += "/npl/_mapping";
        }

        URL url = new URL(urlStr);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setDoOutput(true);
        httpCon.setRequestProperty(
                "Content-Type", "application/x-www-form-urlencoded");
        httpCon.setRequestMethod("PUT");
        String mappingStr = null;
        try {
            ClassLoader classLoader = Indexer.class.getClassLoader();
            if (indexName.contains("annotation")) {
                mappingStr = IOUtils.toString(classLoader.getResourceAsStream("elasticSearch/annotation.json"));
            } else {
                mappingStr = IOUtils.toString(classLoader.getResourceAsStream("elasticSearch/npl.json"));
            }
        } catch (Exception e) {
            throw new ElasticSearchConfigurationException("Cannot read mapping for " + indexName);
        }
        logger.info(urlStr);

        httpCon.setDoOutput(true);
        httpCon.setRequestMethod("PUT");
        httpCon.addRequestProperty("Content-Type", "text/json");
        OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
        out.write(mappingStr);
        out.close();

        logger.info("ElasticSearch mapping for " + indexName + " : status is "
                + httpCon.getResponseCode());
        if (httpCon.getResponseCode() == 200) {
            val = true;
        }
        return val;
    }

    public void close() {

        BiblioDAOFactory.closeConnection();
        DAOFactory.closeConnection();
        this.client.close();
    }

}
