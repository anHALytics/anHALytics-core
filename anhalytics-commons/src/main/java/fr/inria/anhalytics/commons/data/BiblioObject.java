package fr.inria.anhalytics.commons.data;

import java.util.List;

/**
 * Utility bibliographic object.
 * @author azhar
 */
public class BiblioObject {

    public BiblioObject() {
    }

    public BiblioObject(String anhalyticsId, String source, String repositoryDocId, String metadata) {
        this.anhalyticsId = anhalyticsId;
        this.source = source;
        this.repositoryDocId = repositoryDocId;
        this.metadata = metadata;
    }

    //UID mongoDB generated
    private String anhalyticsId="";
    //the URL of the metadata
    private String metadataURL="";
    //the fulltext binary file
    private BinaryFile pdf;
    //metadata string
    private String metadata="";
    //the generated teiCorpus
    private String teiCorpus="";
    //the extracted Grobid TEI
    private String grobidTei="";
    //the id of the document in the source repository
    private String repositoryDocId="";
    // the doc version in the repository
    private String repositoryDocVersion="";
    // the source repository
    private String source="";
    //
    private String doi="";
    //The publication type.
    private String publicationType="";
    //annexes
    private List<BinaryFile> annexes;
    //the domains of the publication
    private List<String> domains;
    //flag to check if the metadata formatting is done and TEICorpus is built.
    private Boolean isProcessedByPub2TEI = false;
    //flag to check if the fultext is appended to TEICorpus.
    private Boolean isFulltextAppended = false;
    //flag to check if the fultext is available.
    private Boolean isWithFulltext = false;
    //flag to check if the metadata is save to the knowledge base.
    private Boolean isMined = false;
    //flag to check if the document is indexed.
    private Boolean isIndexed = false;

    /**
     * @return the anhalyticsId
     */
    public String getAnhalyticsId() {
        return anhalyticsId;
    }

    /**
     * @param anhalyticsId the anhalyticsId to set
     */
    public void setAnhalyticsId(String anhalyticsId) {
        this.anhalyticsId = anhalyticsId;
    }

    /**
     * @return the metadata
     */
    public String getMetadata() {
        return metadata;
    }

    /**
     * @param metadata the metadata to set
     */
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    /**
     * @return the pdf
     */
    public BinaryFile getPdf() {
        return pdf;
    }

    /**
     * @param pdf the pdf to set
     */
    public void setPdf(BinaryFile pdf) {
        if(pdf != null)
            this.isWithFulltext = true;
        this.pdf = pdf;
    }

    /**
     * @return the repositoryDocId
     */
    public String getRepositoryDocId() {
        return repositoryDocId;
    }

    /**
     * @param repositoryDocId the repositoryDocId to set
     */
    public void setRepositoryDocId(String repositoryDocId) {
        this.repositoryDocId = repositoryDocId;
    }

    /**
     * @return the repositoryDocVersion
     */
    public String getRepositoryDocVersion() {
        return repositoryDocVersion;
    }

    /**
     * @param repositoryDocVersion the repositoryDocVersion to set
     */
    public void setRepositoryDocVersion(String repositoryDocVersion) {
        this.repositoryDocVersion = repositoryDocVersion;
    }

    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * @return the doi
     */
    public String getDoi() {
        return doi;
    }

    /**
     * @param doi the doi to set
     */
    public void setDoi(String doi) {
        this.doi = doi;
    }

    /**
     * @return the publicationType
     */
    public String getPublicationType() {
        return publicationType;
    }

    /**
     * @param publicationType the publicationType to set
     */
    public void setPublicationType(String publicationType) {
        this.publicationType = publicationType;
    }

    /**
     * @return the annexes
     */
    public List<BinaryFile> getAnnexes() {
        return annexes;
    }

    /**
     * @param annexes the annexes to set
     */
    public void setAnnexes(List<BinaryFile> annexes) {
        this.annexes = annexes;
    }

    /**
     * @return the isProcessedByPub2TEI
     */
    public Boolean getIsProcessedByPub2TEI() {
        return isProcessedByPub2TEI;
    }

    /**
     * @param isProcessedByPub2TEI the isProcessedByPub2TEI to set
     */
    public void setIsProcessedByPub2TEI(Boolean isProcessedByPub2TEI) {
        this.isProcessedByPub2TEI = isProcessedByPub2TEI;
    }

    /**
     * @return the domains
     */
    public List<String> getDomains() {
        return domains;
    }

    /**
     * @param domains the domains to set
     */
    public void setDomains(List<String> domains) {
        this.domains = domains;
    }

    /**
     * @return the metadataURL
     */
    public String getMetadataURL() {
        return metadataURL;
    }

    /**
     * @param metadataURL the metadataURL to set
     */
    public void setMetadataURL(String metadataURL) {
        this.metadataURL = metadataURL;
    }

    /**
     * @return the isWithFulltext
     */
    public Boolean getIsWithFulltext() {
        return isWithFulltext;
    }

    /**
     * @param isWithFulltext the isWithFulltext to set
     */
    public void setIsWithFulltext(Boolean isWithFulltext) {
        this.isWithFulltext = isWithFulltext;
    }

    /**
     * @return the teiCorpus
     */
    public String getTeiCorpus() {
        return teiCorpus;
    }

    /**
     * @param teiCorpus the teiCorpus to set
     */
    public void setTeiCorpus(String teiCorpus) {
        this.teiCorpus = teiCorpus;
    }

    /**
     * @return the grobidTei
     */
    public String getGrobidTei() {
        return grobidTei;
    }

    /**
     * @param grobidTei the grobidTei to set
     */
    public void setGrobidTei(String grobidTei) {
        this.grobidTei = grobidTei;
    }

    /**
     * @return the isIndexed
     */
    public Boolean getIsIndexed() {
        return isIndexed;
    }

    /**
     * @param isIndexed the isIndexed to set
     */
    public void setIsIndexed(Boolean isIndexed) {
        this.isIndexed = isIndexed;
    }

    /**
     * @return the isMined
     */
    public Boolean getIsMined() {
        return isMined;
    }

    /**
     * @param isMined the isMined to set
     */
    public void setIsMined(Boolean isMined) {
        this.isMined = isMined;
    }
    
    @Override
    public String toString() {
        return "BiblioObject{" + "anhalyticsId=" + anhalyticsId + ",metadataURL=" + metadataURL + ",metadata=" + metadata + ",teiCorpus=" + teiCorpus+ ",doi=" + doi+ ",publicationType=" + publicationType+ ",isWithFulltext=" + isWithFulltext +",domains=" + domains + '}';
    }

    /**
     * @return the isFulltextAppended
     */
    public Boolean getIsFulltextAppended() {
        return isFulltextAppended;
    }

    /**
     * @param isFulltextAppended the isFulltextAppended to set
     */
    public void setIsFulltextAppended(Boolean isFulltextAppended) {
        this.isFulltextAppended = isFulltextAppended;
    }
}
