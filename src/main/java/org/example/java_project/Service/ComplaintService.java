package org.example.java_project.Service;

import java.sql.*;

public class ComplaintService {

    public static ResultSet getComplainte() {
        Connection connection = DbConnection.getConnection();
        try {
            // SQL query to fetch all complaints without a limit
            String sql = "SELECT CONCAT(client.Nom, ' ', client.Prenom) AS Client, client.CNE, rec.Rec_Text, rec.date_Reclamation, typerec.NomType " +
                    "FROM client " +
                    "JOIN rec ON client.id = rec.id_C " +
                    "JOIN typerec ON typerec.id = rec.id_T " +
                    "ORDER BY rec.date_Reclamation DESC";  // No limit, fetch all

            // Create a statement to execute the query
            Statement statement = connection.createStatement();

            // Execute the query
            ResultSet resultSet = statement.executeQuery(sql);

            return resultSet;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void closeConnection() {
        DbConnection.close();
    }
}
