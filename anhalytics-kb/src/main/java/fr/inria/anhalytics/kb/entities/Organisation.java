package fr.inria.anhalytics.kb.entities;

import fr.inria.anhalytics.commons.utilities.Utilities;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author azhar
 */
public class Organisation {

    private Long organisationId;
    private String type = "";
    private String url = "";
    private String structure = "";
    private String status = "";
    private List<Organisation_Name> names = null;
    private Date publication_date;
    private List<PART_OF> rels = null;

    public Organisation() {
    }

    public Organisation(Long organisationId, String type, String status, String url, String structure, List<Organisation_Name> names, List<PART_OF> rels, Date publication_date) {
        this.organisationId = organisationId;
        this.type = type;
        this.url = url;
        this.structure = structure;
        this.names = names;
        this.rels = rels;
        this.publication_date = publication_date;
    }

    /**
     * @return the organisationId
     */
    public Long getOrganisationId() {
        return organisationId;
    }

    /**
     * @param organisationId the organisationId to set
     */
    public void setOrganisationId(Long organisationId) {
        this.organisationId = organisationId;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        if(type.length() > 45)
            type = type.substring(0, 44);
        this.type = type;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        if(url.length() > 255)
            url = url.substring(0, 254);
        this.url = url;
    }

    /**
     * @return the structure
     */
    public String getStructure() {
        return structure;
    }

    /**
     * @param structure the structure to set
     */
    public void setStructure(String structure) {
        if(structure.length() > 45)
            structure = structure.substring(0, 44);
        this.structure = structure;
    }

    /**
     * @return the name
     */
    public List<Organisation_Name> getNames() {
        if (this.names == null) {
            this.names = new ArrayList<Organisation_Name>();
        }
        return names;
    }

    /**
     * @param name the name to set
     */
    public void addName(Organisation_Name name) {
        if (this.names == null) {
            this.names = new ArrayList<Organisation_Name>();
        }
        if(name.getName().length() > 150)
            name.setName(name.getName().substring(0, 149));
        this.names.add(name);
    }

    /**
     * @return the rels
     */
    public List<PART_OF> getRels() {
        if (this.rels == null) {
            this.rels = new ArrayList<PART_OF>();
        }
        return rels;
    }

    /**
     * @param rels the orgs to set
     */
    public void addRel(PART_OF rel) {
        if (this.rels == null) {
            this.rels = new ArrayList<PART_OF>();
        }
        this.rels.add(rel);
    }

    public Map<String, Object> getOrganisationDocument() {
        Map<String, Object> organisationDocument = new HashMap<String, Object>();
        List<Map<String, Object>> organisationNamesDocument = new ArrayList<Map<String, Object>>();
        organisationDocument.put("organisationId", this.getOrganisationId());
        for(Organisation_Name name:getNames()){
            organisationNamesDocument.add(name.getOrganisationNameDocument());
        }
        organisationDocument.put("names", organisationNamesDocument);
        organisationDocument.put("type", this.getType());
        organisationDocument.put("structId", this.getStructure());
        organisationDocument.put("url", this.getUrl());
        //organisationDocument.put("orgs", this.getRels());
        return organisationDocument;

    }

    /**
     * @return the publication_date
     */
    public Date getPublication_date() {
        return publication_date;
    }

    /**
     * @param publication_date the publication_date to set
     */
    public void setPublication_date(Date publication_date) {
        this.publication_date = publication_date;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

}
