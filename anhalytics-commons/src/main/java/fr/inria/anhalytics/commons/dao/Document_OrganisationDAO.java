package fr.inria.anhalytics.commons.dao;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import fr.inria.anhalytics.commons.entities.Document_Organisation;
import fr.inria.anhalytics.commons.entities.Organisation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author achraf
 */
public class Document_OrganisationDAO extends DAO<Document_Organisation, Long> {

    private static final String SQL_INSERT
            = "INSERT INTO DOCUMENT_ORGANISATION (docID, organisationID) VALUES (?, ?)";

    public Document_OrganisationDAO(Connection conn) {
        super(conn);
    }

    @Override
    public boolean create(Document_Organisation obj) throws SQLException {
        boolean result = false;
        if (obj.getDoc() == null || obj.getOrgs() == null) {
            throw new IllegalArgumentException("No Document nor organisation is already created, the Affiliation ID is not null.");
        }

        PreparedStatement statement = null;

        try {
            statement = connect.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);


            for (Organisation org : obj.getOrgs()) {
                try {
                    statement.setString(1, obj.getDoc().getDocID());
                    statement.setLong(2, org.getOrganisationId());
                    statement.executeUpdate();

                    result = true;
                } catch (MySQLIntegrityConstraintViolationException e) {
                }
            }
        } finally {
            closeQuietly(statement);
        }

        return result;
    }

    @Override
    public boolean delete(Document_Organisation obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean update(Document_Organisation obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Document_Organisation find(Long id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
