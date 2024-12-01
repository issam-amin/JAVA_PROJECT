package org.example.java_project.Service;

import java.sql.*;

public class ComplaintService {

    public static ResultSet getComplainte(int limit) {
        Connection connection = DbConnection.getConnection();
        try {
            // SQL query with a placeholder for the limit
            String sql = "SELECT CONCAT(client.Nom, ' ', client.Prenom) AS Client, client.CNE, rec.Rec_Text, rec.date_Reclamation, typerec.NomType " +
                    "FROM client " +
                    "JOIN rec ON client.id = rec.id_C " +
                    "JOIN typerec ON typerec.id = rec.id_T " +
                    "ORDER BY rec.date_Reclamation DESC " +
                    "LIMIT ?";

          //  System.out.println("Executing SQL: " + sql);

            // Using PreparedStatement to bind the limit parameter
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, limit);

            // Execute the query without passing the SQL again
            ResultSet resultSet = statement.executeQuery();

            return resultSet;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Overloaded method with default limit value
    public static ResultSet getComplainte() {
        return getComplainte(20); // Default limit is 20
    }

    public void closeConnection() {
        DbConnection.close();
    }
}
