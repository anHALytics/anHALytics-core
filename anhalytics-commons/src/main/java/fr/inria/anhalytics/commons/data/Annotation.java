package fr.inria.anhalytics.commons.data;

/**
 *
 * @author azhar
 */
public class Annotation {
    private String json;
    private String repositoryDocId;
    private String anhalyticsId;
    private boolean isIndexed;
    
    public Annotation(String json, String repositoryDocId, String anhalyticsId, boolean isIndexed) {
        this.json = json;
        this.repositoryDocId = repositoryDocId;
        this.anhalyticsId = anhalyticsId;
        this.isIndexed = isIndexed;
    }

    /**
     * @return the doi
     */
    public String getJson() {
        return json;
    }

    /**
     * @param doi the doi to set
     */
    public void setJson(String json) {
        this.json = json;
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
     * @return the isIndexed
     */
    public boolean isIsIndexed() {
        return isIndexed;
    }

    /**
     * @param isIndexed the isIndexed to set
     */
    public void setIsIndexed(boolean isIndexed) {
        this.isIndexed = isIndexed;
    }
}
