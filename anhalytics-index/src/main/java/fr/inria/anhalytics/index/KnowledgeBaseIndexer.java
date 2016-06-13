package fr.inria.anhalytics.index;

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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author achraf
 */
public class KnowledgeBaseIndexer extends Indexer {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseIndexer.class);

    private static final AbstractDAOFactory adf = AbstractDAOFactory.getFactory(AbstractDAOFactory.DAO_FACTORY);
    private static final AbstractBiblioDAOFactory biblioadf = AbstractBiblioDAOFactory.getFactory(AbstractBiblioDAOFactory.DAO_FACTORY);

    public KnowledgeBaseIndexer() {
        super();
        DAOFactory.initConnection();
        BiblioDAOFactory.initConnection();
    }

    public int indexAuthors() throws SQLException {
        PersonDAO pdao = (PersonDAO) adf.getPersonDAO();
        OrganisationDAO odao = (OrganisationDAO) adf.getOrganisationDAO();
        AddressDAO adao = (AddressDAO) adf.getAddressDAO();
        DocumentDAO ddao = (DocumentDAO) adf.getDocumentDAO();
        Map<Long, Person> persons = pdao.findAllAuthors();
        int nb = 0, i = 0;
        Iterator it = persons.entrySet().iterator();
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        bulkRequest.setRefresh(true);
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Long personId = (Long) pair.getKey();
            Person pers = (Person) pair.getValue();
            Map<String, Object> jsonDocument = pers.getPersonDocument();
            List<Affiliation> affs = odao.getAffiliationByPersonID(pers);
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
            System.out.println("#############################################################");
            // index the json in ElasticSearch
            // beware the document type bellow and corresponding mapping!
            bulkRequest.add(client.prepareIndex(IndexProperties.getKbIndexName(), "authors", "" + personId).setSource(jsonDocument));
            if (i >= 100) {
                BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                if (bulkResponse.hasFailures()) {
                    // process failures by iterating through each bulk response item	
                    logger.error(bulkResponse.buildFailureMessage());
                }
                bulkRequest = client.prepareBulk();
                bulkRequest.setRefresh(true);
                i = 0;
                System.out.print(".");
                System.out.flush();
            }
            i++;
            nb++;
        }
        // last bulk
        if (i != 0) {
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            System.out.print(".");
            if (bulkResponse.hasFailures()) {
                // process failures by iterating through each bulk response item	
                logger.error(bulkResponse.buildFailureMessage());
            }
        }
        return nb;
    }

    public int indexPublications() throws SQLException {
        int i = 0, nb = 0;
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
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        bulkRequest.setRefresh(true);
        for (Document doc : documents) {
            Map<String, Object> documentDocument = doc.getDocumentDocument();

            Map<Long, Person> authors = pdao.getAuthorsByDocId(doc.getDocID());
            List<Map<String, Object>> authorsDocument = new ArrayList<Map<String, Object>>();
            Iterator it = authors.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                Long personId = (Long) pair.getKey();
                Person pers = (Person) pair.getValue();
                Map<String, Object> jsonDocument = pers.getPersonDocument();
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
            Map<Long, Person> editors = pdao.getEditorsByPubId(pubs.get(0).getPublicationID());//suppose one-one relation..
            List<Map<String, Object>> editorsDocument = new ArrayList<Map<String, Object>>();
            Iterator it1 = editors.entrySet().iterator();
            while (it1.hasNext()) {
                Map.Entry pair = (Map.Entry) it1.next();
                Long personId = (Long) pair.getKey();
                Person pers = (Person) pair.getValue();
                Map<String, Object> jsonDocument = pers.getPersonDocument();
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
                    Map<Long, Person> referenceAuthors = bibliopdao.getEditorsByPubId(referencePub.getPublicationID());

                    List<Map<String, Object>> referenceAuthorsDocument = new ArrayList<Map<String, Object>>();
                    Iterator it2 = referenceAuthors.entrySet().iterator();
                    while (it2.hasNext()) {
                        Map.Entry pair = (Map.Entry) it2.next();
                        Long personId = (Long) pair.getKey();
                        Person pers = (Person) pair.getValue();
                        Map<String, Object> jsonDocument = pers.getPersonDocument();
                        referenceAuthorsDocument.add(jsonDocument);
                    }

                    referencePubDocument.put("authors", referenceAuthorsDocument);
                    referencesPubDocument.add(referencePubDocument);
                }
            }
            documentDocument.put("references", referencesPubDocument);
            System.out.println("#############################################################");
            // index the json in ElasticSearch
            // beware the document type bellow and corresponding mapping!
            bulkRequest.add(client.prepareIndex(IndexProperties.getKbIndexName(), "publications", "" + doc.getDocID()).setSource(documentDocument));
            if (i >= 100) {
                BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                if (bulkResponse.hasFailures()) {
                    // process failures by iterating through each bulk response item	
                    logger.error(bulkResponse.buildFailureMessage());
                }
                bulkRequest = client.prepareBulk();
                bulkRequest.setRefresh(true);
                i = 0;
                System.out.print(".");
                System.out.flush();
            }
            i++;
            nb++;
        }
        // last bulk
        if (i != 0) {
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            System.out.print(".");
            if (bulkResponse.hasFailures()) {
                // process failures by iterating through each bulk response item	
                logger.error(bulkResponse.buildFailureMessage());
            }
        }
        return nb;
    }

    public int indexOrganisations() throws SQLException {
        int nb = 0, i = 0;
        OrganisationDAO odao = (OrganisationDAO) adf.getOrganisationDAO();
        List<Organisation> organisations = odao.findAllOrganisations();
        DocumentDAO ddao = (DocumentDAO) adf.getDocumentDAO();
        AddressDAO adao = (AddressDAO) adf.getAddressDAO();
        PersonDAO pdao = (PersonDAO) adf.getPersonDAO();
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        bulkRequest.setRefresh(true);
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

            Map<Long, Person> authors = pdao.getPersonsByOrgID(org.getOrganisationId());
            List<Map<String, Object>> authorsDocument = new ArrayList<Map<String, Object>>();

            Iterator it2 = authors.entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry pair = (Map.Entry) it2.next();
                Long personId = (Long) pair.getKey();
                Person pers = (Person) pair.getValue();
                Map<String, Object> jsonDocument = pers.getPersonDocument();
                authorsDocument.add(jsonDocument);
            }
            organisationDocument.put("authors", authorsDocument);
            System.out.println("#############################################################");
            // index the json in ElasticSearch
            // beware the document type bellow and corresponding mapping!
            bulkRequest.add(client.prepareIndex(IndexProperties.getKbIndexName(), "organisations", "" + org.getOrganisationId()).setSource(organisationDocument));
            if (i >= 100) {
                BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                if (bulkResponse.hasFailures()) {
                    // process failures by iterating through each bulk response item	
                    logger.error(bulkResponse.buildFailureMessage());
                }
                bulkRequest = client.prepareBulk();
                bulkRequest.setRefresh(true);
                i = 0;
                System.out.print(".");
                System.out.flush();
            }
            i++;
            nb++;
        }
        // last bulk
        if (i != 0) {
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            System.out.print(".");
            if (bulkResponse.hasFailures()) {
                // process failures by iterating through each bulk response item	
                logger.error(bulkResponse.buildFailureMessage());
            }
        }
        return nb;
    }

    @Override
    public void close() {
        super.close();
        BiblioDAOFactory.closeConnection();
        DAOFactory.closeConnection();
    }

}
