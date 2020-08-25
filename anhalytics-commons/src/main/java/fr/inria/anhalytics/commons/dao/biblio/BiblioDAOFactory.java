/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.anhalytics.commons.dao.biblio;

import fr.inria.anhalytics.commons.exceptions.PropertyException;
import fr.inria.anhalytics.commons.properties.CommonsProperties;
import fr.inria.anhalytics.commons.dao.AddressDAO;
import fr.inria.anhalytics.commons.dao.Conference_EventDAO;
import fr.inria.anhalytics.commons.dao.DatabaseConnection;
import fr.inria.anhalytics.commons.dao.DAO;
import fr.inria.anhalytics.commons.dao.DocumentDAO;
import fr.inria.anhalytics.commons.dao.In_SerialDAO;
import fr.inria.anhalytics.commons.dao.MonographDAO;
import fr.inria.anhalytics.commons.dao.PersonDAO;
import fr.inria.anhalytics.commons.dao.PublicationDAO;
import fr.inria.anhalytics.commons.dao.PublisherDAO;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author achraf
 */
public class BiblioDAOFactory extends AbstractBiblioDAOFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(BiblioDAOFactory.class);
    protected static Connection conn = null;

    public static void initConnection() {
        if (conn == null) {
            try {
                CommonsProperties.init("anhalytics.properties", false);
            } catch (Exception exp) {
                throw new PropertyException("Cannot open file of harvest properties ingest.properties", exp);
            }
            conn = DatabaseConnection.getBiblioDBInstance();
        }
    }

    public DAO getDocumentDAO() {
        return new DocumentDAO(conn);
    }

    public DAO getAddressDAO() {
        return new AddressDAO(conn);
    }

    public DAO getConference_EventDAO() {
        return new Conference_EventDAO(conn);
    }

    public DAO getIn_SerialDAO() {
        return new In_SerialDAO(conn);
    }

    public DAO getMonographDAO() {
        return new MonographDAO(conn);
    }

    public DAO getPersonDAO() {
        return new PersonDAO(conn);
    }

    public DAO getPublicationDAO() {
        return new PublicationDAO(conn);
    }

    public DAO getPublisherDAO() {
        return new PublisherDAO(conn);
    }

    public void openTransaction() {
        try {
            conn.setAutoCommit(false);
            LOGGER.info("Storing entry");
        } catch (SQLException e) {
            LOGGER.error("There was an error disabling autocommit");
        }
    }

    public void endTransaction() {
        try {
            conn.commit();
            LOGGER.info("Entry stored");
        } catch (SQLException ex) {
            LOGGER.error("Error happened while commiting the changes.");
        }
    }

    public void rollback() {
        try {
            // We rollback the transaction, to the last SavePoint!
            conn.rollback();
            LOGGER.info("The transaction was rollback.");
        } catch (SQLException e1) {
            LOGGER.error("There was an error making a rollback");

        }
    }

    public static void closeConnection() {
        try {
            conn.close();
        } catch (SQLException ex) {
            LOGGER.error("Error: ", ex);
        }
    }
}
