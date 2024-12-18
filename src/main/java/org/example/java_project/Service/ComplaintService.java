package org.example.java_project.Service;

import java.sql.*;

public class ComplaintService {

    public static ResultSet getComplainte() {
        Connection connection = null;
        ResultSet resultSet = null;
        try {
            connection = DbConnection.getConnection();
            if (connection == null || connection.isClosed()) {
                throw new SQLException("Connection is closed or null.");
            }

            String sql = "SELECT CONCAT(client.firstName, ' ', client.lastName) AS Client, rec.Rec_Text, rec.date_Reclamation, typerec.NomType, rec.status_Rec, rec.id_C, rec.id_R " +
                    "FROM customer AS client " +
                    "JOIN rec ON client.id = rec.id_C " +
                    "JOIN typerec ON typerec.id = rec.id_T " +
                    "ORDER BY rec.date_Reclamation DESC, rec.id_C DESC";


            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultSet;
    }

    public void closeConnection() {
        try {
            Connection conn = DbConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                DbConnection.close(); // Close the connection when done
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
