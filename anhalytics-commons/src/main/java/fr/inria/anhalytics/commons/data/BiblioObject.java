package fr.inria.anhalytics.commons.data;

import java.io.InputStream;
import java.util.List;

/**
 *
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

    private String anhalyticsId="";
    private String metadataURL="";
    private BinaryFile pdf;
    private String metadata="";
    private String teiCorpus="";
    private String grobidTei="";
    private String repositoryDocId="";
    private String repositoryDocVersion="";
    private String source="";
    private String doi="";
    private String publicationType="";
    private List<BinaryFile> annexes;
    private List<String> domains;
    private Boolean isProcessedByPub2TEI = false;
    private Boolean isWithFulltext = false;
    private Boolean isProcessedByGrobid = false;
    private Boolean isProcessedByNerd = false;
    private Boolean isProcessedByKeyterm = false;
    private Boolean isProcessedByTextQuantities = false;
    private Boolean isProcessedByPDFQuantities = false;
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
     * @return the isProcessedByGrobid
     */
    public Boolean getIsProcessedByGrobid() {
        return isProcessedByGrobid;
    }

    /**
     * @param isProcessedByGrobid the isProcessedByGrobid to set
     */
    public void setIsProcessedByGrobid(Boolean isProcessedByGrobid) {
        this.isProcessedByGrobid = isProcessedByGrobid;
    }

    /**
     * @return the isProcessedByNerd
     */
    public Boolean getIsProcessedByNerd() {
        return isProcessedByNerd;
    }

    /**
     * @param isProcessedByNerd the isProcessedByNerd to set
     */
    public void setIsProcessedByNerd(Boolean isProcessedByNerd) {
        this.isProcessedByNerd = isProcessedByNerd;
    }

    /**
     * @return the isProcessedByKeyterm
     */
    public Boolean getIsProcessedByKeyterm() {
        return isProcessedByKeyterm;
    }

    /**
     * @param isProcessedByKeyterm the isProcessedByKeyterm to set
     */
    public void setIsProcessedByKeyterm(Boolean isProcessedByKeyterm) {
        this.isProcessedByKeyterm = isProcessedByKeyterm;
    }

    /**
     * @return the isProcessedByTextQuantities
     */
    public Boolean getIsProcessedByTextQuantities() {
        return isProcessedByTextQuantities;
    }

    /**
     * @param isProcessedByTextQuantities the isProcessedByTextQuantities to set
     */
    public void setIsProcessedByTextQuantities(Boolean isProcessedByTextQuantities) {
        this.isProcessedByTextQuantities = isProcessedByTextQuantities;
    }

    /**
     * @return the isProcessedByPDFQuantities
     */
    public Boolean getIsProcessedByPDFQuantities() {
        return isProcessedByPDFQuantities;
    }

    /**
     * @param isProcessedByPDFQuantities the isProcessedByPDFQuantities to set
     */
    public void setIsProcessedByPDFQuantities(Boolean isProcessedByPDFQuantities) {
        this.isProcessedByPDFQuantities = isProcessedByPDFQuantities;
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
    
    
}
