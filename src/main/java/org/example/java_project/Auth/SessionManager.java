package org.example.java_project.Auth;



import org.example.java_project.Service.DbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SessionManager {

    private static SessionManager instance;
    private String username; // Store user details
    private  int UserId; // Example of additional session info

    private SessionManager() {
        // Private constructor to prevent instantiation
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }


    public void clearSession() {
        username = null;
        UserId = 0;
    }

    public int getUserId() {
        return UserId;
    }

    public void setUserId(int userId) {
        UserId = userId;
    }


    public ResultSet GetUserProfile(){

        try {
            Connection  connection =DbConnection.getConnection();
            String sql = " SELECT * from admin where id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1,getUserId());
            return preparedStatement.executeQuery();
        } catch (SQLException e) {
           throw new RuntimeException(e);
        }

    }
}
