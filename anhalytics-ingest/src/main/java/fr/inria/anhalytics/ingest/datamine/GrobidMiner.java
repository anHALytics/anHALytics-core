package fr.inria.anhalytics.ingest.datamine;

import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.dao.AbstractDAOFactory;
import fr.inria.anhalytics.dao.AddressDAO;
import fr.inria.anhalytics.dao.Conference_EventDAO;
import fr.inria.anhalytics.dao.DocumentDAO;
import fr.inria.anhalytics.dao.In_SerialDAO;
import fr.inria.anhalytics.dao.MonographDAO;
import fr.inria.anhalytics.dao.PersonDAO;
import fr.inria.anhalytics.dao.PublicationDAO;
import fr.inria.anhalytics.ingest.dao.biblio.AbstractBiblioDAOFactory;
import fr.inria.anhalytics.ingest.dao.biblio.BiblioDAOFactory;
import fr.inria.anhalytics.ingest.entities.Address;
import fr.inria.anhalytics.ingest.entities.Collection;
import fr.inria.anhalytics.ingest.entities.Conference;
import fr.inria.anhalytics.ingest.entities.Conference_Event;
import fr.inria.anhalytics.ingest.entities.Country;
import fr.inria.anhalytics.ingest.entities.Editor;
import fr.inria.anhalytics.ingest.entities.In_Serial;
import fr.inria.anhalytics.ingest.entities.Journal;
import fr.inria.anhalytics.ingest.entities.Monograph;
import fr.inria.anhalytics.ingest.entities.Person;
import fr.inria.anhalytics.ingest.entities.Publication;
import fr.inria.anhalytics.ingest.entities.Publisher;
import fr.inria.anhalytics.ingest.entities.Serial_Identifier;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author azhar
 */
public class GrobidMiner extends Miner {

    private static final Logger logger = LoggerFactory.getLogger(GrobidMiner.class);

    private static final AbstractBiblioDAOFactory abdf = AbstractBiblioDAOFactory.getFactory(AbstractBiblioDAOFactory.DAO_FACTORY);

    private static XPath xPath = XPathFactory.newInstance().newXPath();

    public GrobidMiner() throws UnknownHostException {
        super();
    }

    public static String docID = "";

    public static String getDocId() {
        return docID;
    }

    /**
     *
     */
    public void processCitations() {
        BiblioDAOFactory.initConnection();
        DocumentDAO dd = (DocumentDAO) abdf.getDocumentDAO();

        for (String date : Utilities.getDates()) {
            if (mm.initTeis(date)) {
                while (mm.hasMoreTeis()) {
                    String teiString = mm.nextTeiDocument();
                    String uri = mm.getCurrentRepositoryDocId();
                    if (!mm.isWithFulltext(uri)) {
                        //No interest to index docs without fulltext.
                        continue;
                    }
                    if (!dd.isCitationsMined(uri)) {
                        logger.info("Extracting metadata from :" + uri);
                        abdf.openTransaction();
                        try {
                            InputStream teiStream = new ByteArrayInputStream(teiString.getBytes());
                            Document teiDoc = getDocument(teiStream);
                            teiStream.close();
                            Node citations = (Node) xPath.compile("/teiCorpus/TEI/text/back/div[@type='references']/listBibl").evaluate(teiDoc, XPathConstants.NODE);
                            NodeList references = citations.getChildNodes();
                            fr.inria.anhalytics.ingest.entities.Document doc = new fr.inria.anhalytics.ingest.entities.Document(null, Utilities.getVersionFromURI(uri), Utilities.innerXmlToString(citations), uri);
                            dd.create(doc);

                            System.out.println(doc.getDocID());
                            for (int j = 0; j < references.getLength() - 1; j++) {
                                if (references.item(j).getNodeType() == Node.ELEMENT_NODE) {
                                    processBiblStruct((Element) references.item(j), doc);
                                }
                            }

                        } catch (Exception xpe) {
                            xpe.printStackTrace();
                            abdf.rollback();
                        }
                        abdf.endTransaction();
                        logger.info("Done.");
                    }
                }
            }
        }
        BiblioDAOFactory.closeConnection();
    }

    private void processBiblStruct(Element reference, fr.inria.anhalytics.ingest.entities.Document doc) {
        PublicationDAO pd = (PublicationDAO) abdf.getPublicationDAO();
        MonographDAO md = (MonographDAO) abdf.getMonographDAO();
        In_SerialDAO isd = (In_SerialDAO) abdf.getIn_SerialDAO();
        PersonDAO persd = (PersonDAO) abdf.getPersonDAO();
        Conference_EventDAO ced = (Conference_EventDAO) abdf.getConference_EventDAO();

        Conference_Event ce = null;
        In_Serial is = null;
        Publication pub = new Publication();
        Monograph mn = new Monograph();

        pub.setDocument(doc);
        List<Person> prss = new ArrayList<Person>();
        Node analytic = (reference.getElementsByTagName("analytic")).item(0);

        Node monogr = (reference.getElementsByTagName("monogr")).item(0);
        NodeList subNodes = null;
        if (analytic != null && analytic.getNodeType() == Node.ELEMENT_NODE) {
            subNodes = analytic.getChildNodes();
            for (int z = subNodes.getLength() - 1; z >= 0; z--) {
                Node node1 = subNodes.item(z);
                if (node1.getNodeName().equals("title")) {
                    pub.setDoc_title(node1.getTextContent());
                    NamedNodeMap nnm = node1.getAttributes();
                    for (int j = nnm.getLength() - 1; j >= 0; j--) {
                        if (nnm.item(j).getNodeName().equals("level")) {
                            pub.setType(nnm.item(j).getTextContent());
                        }
                    }
                } else if (subNodes.item(z).getNodeName().equals("author")) {
                    Person prs = new Person();
                    NodeList author = subNodes.item(z).getChildNodes();
                    for (int y = author.getLength() - 1; y >= 0; y--) {
                        Node persName = author.item(y);
                        if (persName.getNodeType() == Node.ELEMENT_NODE) {
                            if (persName.getNodeName().equals("persName")) {
                                NodeList persNameNodes = persName.getChildNodes();
                                for (int o = persNameNodes.getLength() - 1; o >= 0; o--) {
                                    if (persNameNodes.item(o).getNodeName().equals("forename")) {
                                        prs.setForename(persNameNodes.item(o).getTextContent());
                                    } else if (persNameNodes.item(o).getNodeName().equals("surname")) {
                                        prs.setSurname(persNameNodes.item(o).getTextContent());
                                    }
                                }

                            } else if (persName.getNodeName().equals("email")) {
                                prs.setEmail(persName.getTextContent());
                            } else if (persName.getNodeName().equals("ptr")) {
                                Element ptr = (Element) persName;
                                if (ptr.getAttribute("type").equals("url")) {
                                    prs.setUrl(ptr.getAttribute("type"));
                                }
                            }
                        }
                    }
                    prss.add(prs);
                }
            }
        }
        if (monogr != null && monogr.getNodeType() == Node.ELEMENT_NODE) {
            subNodes = monogr.getChildNodes();

            Journal journal = null;
            Collection collection = null;
            Serial_Identifier serial_identifier = new Serial_Identifier();
            for (int c = subNodes.getLength() - 1; c >= 0; c--) {
                Node node2 = subNodes.item(c);
                if (node2.getNodeName().equals("title")) {
                    NamedNodeMap nnm = node2.getAttributes();
                    mn.setTitle(node2.getTextContent());
                    if (analytic == null) {
                        pub.setDoc_title(node2.getTextContent());
                    }
                    for (int j = nnm.getLength() - 1; j >= 0; j--) {
                        if (nnm.item(j).getNodeName().equals("level")) {
                            mn.setType(nnm.item(j).getTextContent());
                            if (analytic == null) {
                                pub.setType(nnm.item(j).getTextContent());
                            }
                            if (node2.getTextContent() != null) {
                                if (nnm.item(j).getTextContent().equals("j")) {
                                    journal = new Journal(null, node2.getTextContent());
                                } else {
                                    collection = new Collection(null, node2.getTextContent());
                                }
                            }
                        }
                    }
                } else if (node2.getNodeName().equals("imprint")) {
                    NodeList imprint = node2.getChildNodes();
                    is = new In_Serial();
                    for (int j = imprint.getLength() - 1; j >= 0; j--) {
                        Node entry = imprint.item(j);
                        if (entry.getNodeName().equals("publisher")) {
                            Publisher pls = new Publisher(null, entry.getTextContent());
                            abdf.getPublisherDAO().create(pls);
                            pub.setPublisher(pls);
                        } else if (entry.getNodeName().equals("date")) {
                            if (entry.getNodeType() == Node.ELEMENT_NODE) {
                                Element dateElt = (Element) entry;
                                String type = dateElt.getAttribute("type");
                                String date = dateElt.getAttribute("when");
                                pub.setDate_eletronic(date);
                            }
                        } else if (entry.getNodeName().equals("biblScope")) {
                            Element ptr = (Element) entry;
                            String unit = ptr.getAttribute("unit");
                            if (unit.equals("serie")) {
                                collection.setTitle(entry.getTextContent());
                            } else if (unit.equals("volume")) {
                                is.setVolume(entry.getTextContent());
                            } else if (unit.equals("issue")) {
                                is.setNumber(entry.getTextContent());
                            } else if (unit.equals("page")) {
                                String start = ptr.getAttribute("from");
                                String end = ptr.getAttribute("to");
                                pub.setStart_page(start);
                                pub.setEnd_page(end);
                            }
                        }
                    }
                } else if (node2.getNodeName().equals("meeting")) {
                    ce = new Conference_Event();
                    ce.setConference(new Conference());
                    Address addr = new Address();
                    AddressDAO ad = (AddressDAO) abdf.getAddressDAO();
                    NodeList meeting = node2.getChildNodes();
                    
                    for (int j = meeting.getLength() - 1; j >= 0; j--) {
                        Node entry = meeting.item(j);
                        if (entry.getNodeName().equals("title")) {
                            ce.getConference().setTitle(entry.getTextContent());
                        } else if (entry.getNodeName().equals("date")) {
                            if (entry.getNodeType() == Node.ELEMENT_NODE) {
                                Element dateElt = (Element) entry;
                                String type = dateElt.getAttribute("type");
                                String date = dateElt.getAttribute("when");
                                if (type.equals("start")) {
                                    ce.setStart_date(date);
                                } else if (type.equals("end")) {
                                    ce.setEnd_date(date);
                                }
                            }
                        } else if (entry.getNodeName().equals("settlement")) {
                            addr.setSettlement(entry.getTextContent());
                        } else if (entry.getNodeName().equals("country")) {
                            Country country = new Country();
                            addr.setCountryStr(entry.getTextContent());
                            NamedNodeMap nnm = entry.getAttributes();
                            for (int p = nnm.getLength() - 1; p >= 0; p--) {
                                if (nnm.item(p).getNodeName().equals("key")) {
                                    country.setIso(nnm.item(p).getTextContent());
                                }
                            }
                            addr.setCountry(country);
                        } else if (entry.getNodeName().equals("region")) {
                            addr.setRegion(entry.getTextContent());
                        }

                    }
                    ad.create(addr);
                    ce.setAddress(addr);
                }
                if (collection != null) {
                    is.setC(collection);
                }
                if (journal != null) {
                    is.setJ(journal);
                }

            }
        }

        md.create(mn);

        if (ce != null) {
            ce.setMongoraph(mn);
            ced.create(ce);
        }
        if (is != null) {
            is.setMg(mn);
            isd.create(is);
        }

        pub.setMonograph(mn);
        pd.create(pub);
        for (Person p : prss) {
            persd.createEditor(new Editor(0, p, pub));
        }
    }

    private Document getDocument(InputStream in) throws IOException, ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);

        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = null;
        try {
            doc = docBuilder.parse(in);
        } catch (SAXException e) {
            e.printStackTrace();

        }
        return doc;
    }

    /*
     public static Document mine(Document docGrobid, String id) throws ParserConfigurationException, IOException, XPathExpressionException {
     PublicationDAO pd = (PublicationDAO)adf.getPublicationDAO();
     DocumentDAO dd = (DocumentDAO)adf.getDocumentDAO();

     Publication pub = new Publication();

     Node title = (Node) xPath.compile("/TEI/teiHeader/fileDesc/titleStmt/title").evaluate(docGrobid, XPathConstants.NODE);
     Node language = (Node) xPath.compile("/TEI/teiHeader/@lang").evaluate(docGrobid, XPathConstants.NODE);
     NodeList editors = (NodeList) xPath.compile("/TEI/teiHeader/fileDesc/sourceDesc/biblStruct/analytic/editor").evaluate(docGrobid, XPathConstants.NODESET);
     NodeList authors = (NodeList) xPath.compile("/TEI/teiHeader/fileDesc/sourceDesc/biblStruct/analytic/author").evaluate(docGrobid, XPathConstants.NODESET);
     Node metadata = (Node) xPath.compile("/TEI/teiHeader").evaluate(docGrobid, XPathConstants.NODE);
     NodeList monogr = (NodeList) xPath.compile("/TEI/teiHeader/fileDesc/sourceDesc/biblStruct/monogr").evaluate(docGrobid, XPathConstants.NODESET);

     NodeList ids = (NodeList) xPath.compile("/TEI/teiHeader/fileDesc/sourceDesc/biblStruct/idno").evaluate(docGrobid, XPathConstants.NODESET);

     fr.inria.anhalytics.entities.Document doc = new fr.inria.anhalytics.entities.Document(null, Utilities.getVersionFromURI(id), Utilities.innerXmlToString(metadata), id);

     dd.create(doc);
     docID = Long.toString(doc.getDocID());
     pub.setDocument(doc);

     pub.setDoc_title(title.getTextContent());
     pub.setLanguage(language.getTextContent());

     processMonogr(monogr, pub);

     pd.create(pub);
     processPersons(authors, "author", pub);
     processPersons(editors, "editor", pub);
     processIdentifiers(ids, doc);

     return docGrobid;
     }

     private static void processIdentifiers(NodeList ids, fr.inria.anhalytics.entities.Document doc) {
     String type = null;
     String id = null;
     Document_IdentifierDAO did = (Document_IdentifierDAO)adf.getDocument_IdentifierDAO();
     for (int i = ids.getLength() - 1; i >= 0; i--) {

     Node node = ids.item(i);

     NamedNodeMap nnm = node.getAttributes();
     for (int j = nnm.getLength() - 1; j >= 0; j--) {
     if (nnm.item(j).getNodeName().equals("type")) {
     type = nnm.item(j).getTextContent();
     }
     }
     id = node.getTextContent();
     fr.inria.anhalytics.entities.Document_Identifier di = new fr.inria.anhalytics.entities.Document_Identifier(null, id, type, doc);
     did.create(di);
     }
     fr.inria.anhalytics.entities.Document_Identifier dihal = new fr.inria.anhalytics.entities.Document_Identifier(null, doc.getUri(), "hal", doc);
     did.create(dihal);
     }

     private static void processMonogr(NodeList monogr, Publication pub) {
     MonographDAO md = (MonographDAO)adf.getMonographDAO();
     Conference_EventDAO ced = (Conference_EventDAO)adf.getConference_EventDAO();
     In_SerialDAO isd = (In_SerialDAO)adf.getIn_SerialDAO();

     Monograph mn = new Monograph();
     Conference_Event ce = null;
     In_Serial is = new In_Serial();
     Journal journal = new Journal();
     Collection collection = new Collection();
     Serial_Identifier serial_identifier = new Serial_Identifier();
     NodeList content = monogr.item(0).getChildNodes();
     for (int i = content.getLength() - 1; i >= 0; i--) {
     Node node = content.item(i);
     if (node.getNodeName().equals("idno")) {
     NamedNodeMap nnm = node.getAttributes();
     for (int j = nnm.getLength() - 1; j >= 0; j--) {
     if (nnm.item(j).getNodeName().equals("type")) {
     if (nnm.item(j).getTextContent().equals("issn")) {
     is.setNumber(node.getTextContent());
     } else if (nnm.item(j).getTextContent().equals("isbn")) {
     is.setNumber(node.getTextContent());
     }
     }
     }
     } else if (node.getNodeName().equals("title")) {
     NamedNodeMap nnm = node.getAttributes();
     mn.setTitle(node.getTextContent());
     for (int j = nnm.getLength() - 1; j >= 0; j--) {
     if (nnm.item(j).getNodeName().equals("level")) {
     pub.setType(nnm.item(j).getTextContent());
     mn.setType(nnm.item(j).getTextContent());
     if (nnm.item(j).getTextContent().equals("j")) {
     journal.setTitle(node.getTextContent());
     } else {
     collection.setTitle(node.getTextContent());
     }
     }
     }
     } else if (node.getNodeName().equals("imprint")) {
     NodeList imprint = node.getChildNodes();

     for (int j = imprint.getLength() - 1; j >= 0; j--) {
     Node entry = imprint.item(j);
     if (entry.getNodeName().equals("publisher")) {
     Publisher pls = new Publisher(null, entry.getTextContent());
     ((PublisherDAO)adf.getPublisherDAO()).create(pls);
     pub.setPublisher(pls);
     } else if (entry.getNodeName().equals("date")) {
     String type = null;
     String date = null;
     NamedNodeMap nnm = entry.getAttributes();
     for (int p = nnm.getLength() - 1; p >= 0; p--) {
     if (nnm.item(p).getNodeName().equals("type")) {
     type = nnm.item(p).getTextContent();
     }
     }
     date = entry.getTextContent();
     pub.setDate_eletronic(date);

     } else if (entry.getNodeName().equals("biblScope")) {
     NamedNodeMap nnm = entry.getAttributes();

     for (int p = nnm.getLength() - 1; p >= 0; p--) {
     if (nnm.item(p).getNodeName().equals("unit")) {
     if (nnm.item(p).getTextContent().equals("serie")) {
     collection.setTitle(entry.getTextContent());
     } else if (nnm.item(p).getTextContent().equals("volume")) {
     is.setVolume(entry.getTextContent());
     } else if (nnm.item(p).getTextContent().equals("page")) {
     Element biblscopePage = (Element) entry;
     pub.setStart_page(biblscopePage.getAttribute("from"));
     pub.setEnd_page(biblscopePage.getAttribute("to"));
     }
     }
     }
     }

     }
     } else if (node.getNodeName().equals("meeting")) {
     ce = new Conference_Event();
     Address addr = new Address();
     AddressDAO ad = (AddressDAO)adf.getAddressDAO();
     NodeList meeting = node.getChildNodes();
     for (int j = meeting.getLength() - 1; j >= 0; j--) {
     Node entry = meeting.item(j);
     if (entry.getNodeName().equals("title")) {
     ce.setConference((new Conference(null, entry.getTextContent())));
     } else if (entry.getNodeName().equals("date")) {
     NamedNodeMap nnm = entry.getAttributes();
     for (int p = nnm.getLength() - 1; p >= 0; p--) {
     if (nnm.item(p).getNodeName().equals("type")) {
     if (nnm.item(p).getTextContent().equals("start")) {
     ce.setStart_date(entry.getTextContent());
     } else if (nnm.item(p).getTextContent().equals("end")) {
     ce.setEnd_date(entry.getTextContent());
     }
     }
     }
     } else if (entry.getNodeName().equals("settlement")) {
     addr.setSettlement(entry.getTextContent());
     } else if (entry.getNodeName().equals("country")) {
     Country c = new Country();
     addr.setCountryStr(entry.getTextContent());
     NamedNodeMap nnm = entry.getAttributes();
     for (int p = nnm.getLength() - 1; p >= 0; p--) {
     if (nnm.item(p).getNodeName().equals("key")) {
     c.setIso(nnm.item(p).getTextContent());
     }
     }
     addr.setCountry(c);
     } else if (entry.getNodeName().equals("region")) {
     addr.setRegion(entry.getTextContent());
     }
     }
     ad.create(addr);
     ce.setAddress(addr);
     }
     }

     md.create(mn);
     if (ce != null) {
     ce.setMongoraph(mn);
     ced.create(ce);
     }
     pub.setMonograph(mn);
     is.setMg(mn);
     is.setJ(journal);
     is.setC(collection);
     isd.createSerial(is, serial_identifier);
     }

     private static void processPersons(NodeList persons, String type, Publication pub) {
     Node person = null;
     PersonDAO pd = (PersonDAO)adf.getPersonDAO();
     Person prs = new Person();
     Affiliation affiliation = null;
     AffiliationDAO affd = (AffiliationDAO)adf.getAffiliationDAO();
     OrganisationDAO od = (OrganisationDAO)adf.getOrganisationDAO();
     List<Person_Identifier> pis = new ArrayList<Person_Identifier>();
     for (int i = persons.getLength() - 1; i >= 0; i--) {
     person = persons.item(i);
     prs = new Person();
     affiliation = new Affiliation();
     NodeList theNodes = person.getChildNodes();
     NodeList nodes = null;
     for (int y = theNodes.getLength() - 1; y >= 0; y--) {
     Node node = theNodes.item(y);
     if (node.getNodeType() == Node.ELEMENT_NODE) {
     if (node.getNodeName().equals("persName")) {
     nodes = node.getChildNodes();
     for (int z = nodes.getLength() - 1; z >= 0; z--) {
     if (nodes.item(z).getNodeName().equals("forename")) {
     prs.setForename(nodes.item(z).getTextContent());} else if (nodes.item(z).getNodeName().equals("surname")) {
     prs.setSurname(nodes.item(z).getTextContent());}
     }
     } else if (node.getNodeName().equals("affiliation")) {

     Address addr = new Address();
     if (node.getNodeType() == Node.ELEMENT_NODE) {
     Element aff = (Element) node;
     String addressStr = "";
     if (aff.getElementsByTagName("address").item(0) != null) {
     NodeList address = aff.getElementsByTagName("address").item(0).getChildNodes();

     for (int j = address.getLength() - 1; j >= 0; j--) {
     if (address.item(j).getNodeName().equals("addrLine")) {
     addr.setPostCode(address.item(j).getTextContent());
     addressStr += address.item(j).getTextContent() + " ";
     } else if (address.item(j).getNodeName().equals("postCode")) {
     addr.setPostCode(address.item(j).getTextContent());
     addressStr += address.item(j).getTextContent() + " ";
     } else if (address.item(j).getNodeName().equals("settlement")) {
     addr.setSettlement(address.item(j).getTextContent());
     addressStr += address.item(j).getTextContent() + " ";
     } else if (address.item(j).getNodeName().equals("country")) {
     addr.setCountryStr(address.item(j).getTextContent());
     NamedNodeMap nnm = address.item(j).getAttributes();
     for (int m = nnm.getLength() - 1; m >= 0; m--) {
     if (nnm.item(m).getNodeName().equals("key")) {
     addr.setCountry(new Country(null, nnm.item(m).getTextContent()));
     }
     }
     }
     }
     AddressDAO ad = (AddressDAO)adf.getAddressDAO();
     ad.create(addr);
     }

     NodeList orgNames = aff.getElementsByTagName("orgName");
     Organisation org = null;
     Location location = null;
     LocationDAO ld = (LocationDAO)adf.getLocationDAO();
     for (int p = orgNames.getLength() - 1; p >= 0; p--) {
     org = new Organisation();
     location = new Location();
     org.addName(orgNames.item(p).getTextContent());
     NamedNodeMap nnm = orgNames.item(p).getAttributes();
     for (int j = nnm.getLength() - 1; j >= 0; j--) {
     if (nnm.item(j).getNodeName().equals("type")) {
     org.setType(nnm.item(j).getTextContent());
     }
     }
     od.create(org);
     affiliation.addOrganisation(org);
     if (addr.getAddressId() != null && org.getOrganisationId() != null) {
     location.setAddress(addr);
     location.setOrganisation(org);
     ld.create(location);
     }
     }
     }
     } else if (node.getNodeName().equals("idno")) {
     Person_Identifier pi = new Person_Identifier();
     NamedNodeMap nnm = node.getAttributes();
     String id_type = null;
     String id_value = node.getTextContent();
     for (int p = nnm.getLength() - 1; p >= 0; p--) {
     if (nnm.item(p).getNodeName().equals("type")) {
     id_type = nnm.item(p).getTextContent();
     }
     }
     pi.setId(id_value);
     pi.setType(id_type);
     pis.add(pi);
     }
     }
     }
     prs.setPerson_identifiers(pis);

     if (type.equals("author")) {
     Author author = new Author(pub.getDocument(), prs, 0, 0);
     pd.createAuthor(author);
     if (affiliation.getOrganisations() != null) {
     affiliation.setPerson(prs);
     affd.create(affiliation);
     }
     } else if (type.equals("editor")) {
     Editor editor = new Editor(0, prs, pub);
     pd.createEditor(editor);
     }
     }

     }

     public void mine() throws IOException, ParserConfigurationException {
     InputStream grobid_tei = null;
     Document grobid_doc;
     for (String date : Utilities.getDates()) {
     if (mm.initGrobidTeis(date)) {
     while (mm.hasMoreBinaryDocuments()) {
     String tei_doc = mm.nextTeiDocument();
     String id = mm.getCurrentRepositoryDocId();
     grobid_tei = new ByteArrayInputStream(tei_doc.getBytes());
     grobid_doc = getDocument(grobid_tei);
     try {
     //Extract teis Header metadata
     grobid_doc = mine(grobid_doc, id);
     } catch (Exception e) {
     e.printStackTrace();
     }
                
     }}
     }
     }
     */
}
