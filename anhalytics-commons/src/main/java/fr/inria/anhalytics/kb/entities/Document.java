package fr.inria.anhalytics.kb.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author azhar
 */
public class Document {

    private String docID;
    private String version = "";
    private String uri = "";
    private List<Document_Identifier> document_Identifiers = null;

    public Document(String docID, String version, String uri, List<Document_Identifier> document_Identifiers) {
        this.docID = docID;
        this.version = version;
        this.uri = uri;
        this.document_Identifiers = document_Identifiers;
    }

    public Document() {
    }

    /**
     * @return the docID
     */
    public String getDocID() {
        return docID;
    }

    /**
     * @param docID the docID to set
     */
    public void setDocID(String docID) {
        this.docID = docID;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        if(version.length() > 45)
            version = version.substring(0, 44);
        this.version = version;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        if(uri.length() > 45)
            uri = uri.substring(0, 44);
        this.uri = uri;
    }

    /**
     * The user ID is unique for each User. So this should compare User by ID
     * only.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        return (other instanceof Document) && (docID != null)
                ? docID.equals(((Document) other).docID)
                : (other == this);
    }

    /**
     * The user ID is unique for each User. So User with same ID should return
     * same hashcode.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (docID != null)
                ? (this.getClass().hashCode() + docID.hashCode())
                : super.hashCode();
    }

    /**
     * Returns the String representation of this User. Not required, it just
     * pleases reading logs.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("User[docID=%d,version=%s,uri=%s]",
                docID, version, uri);
    }

    public Map<String, Object> getDocumentDocument() {
        Map<String, Object> documentDocument = new HashMap<String, Object>();
        documentDocument.put("docID", this.getDocID());
        documentDocument.put("version", this.getVersion());
        documentDocument.put("uri", this.getUri());
        return documentDocument;
    }

    /**
     * @return the document_Identifiers
     */
    public List<Document_Identifier> getDocument_Identifiers() {
        if (this.document_Identifiers == null) {
            this.document_Identifiers = new ArrayList<Document_Identifier>();
        }
        return document_Identifiers;
    }

    /**
     * @param document_Identifiers the document_Identifiers to set
     */
    public void setDocument_Identifiers(List<Document_Identifier> document_Identifiers) {
        this.document_Identifiers = document_Identifiers;
    }
}
