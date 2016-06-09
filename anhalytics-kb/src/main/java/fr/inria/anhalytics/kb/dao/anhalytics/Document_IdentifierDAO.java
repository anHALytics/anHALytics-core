package fr.inria.anhalytics.kb.dao.anhalytics;

import fr.inria.anhalytics.dao.DocumentDAO;
import fr.inria.anhalytics.dao.DAO;
import fr.inria.anhalytics.kb.entities.Document_Identifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author azhar
 */
public class Document_IdentifierDAO extends DAO<Document_Identifier, Long> {

    private static final String SQL_INSERT
            = "INSERT INTO DOCUMENT_IDENTIFIER (docID, ID, Type) VALUES (?, ?, ?)";

    public Document_IdentifierDAO(Connection conn) {
        super(conn);
    }

    @Override
    public boolean create(Document_Identifier obj) throws SQLException {
        boolean result = false;
        if (obj.getDoc_identifierID() != null) {
            throw new IllegalArgumentException("Document_Identifier is already created, the Document_Identifier ID is not null.");
        }

        PreparedStatement statement;
        statement = connect.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, obj.getDoc().getDocID());
        statement.setString(2, obj.getId());
        statement.setString(3, obj.getType());
        int code = statement.executeUpdate();
        ResultSet rs = statement.getGeneratedKeys();

        if (rs.next()) {
            obj.setDoc_identifierID(rs.getLong(1));
        }
        statement.close();
        result = true;
        return result;
    }

    @Override
    public boolean delete(Document_Identifier obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean update(Document_Identifier obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Document_Identifier find(Long id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
