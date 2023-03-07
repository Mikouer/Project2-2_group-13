package com.example.chatbot.tools;

import java.awt.event.TextEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public class DB {
    //TODO: these are my credentials, you have to change yours since we don't have a real server
    private final String schemaName ="timetable";
    private final String username="root";
    private final String password="Project22@";
    String connectionString;
    Connection connection = null;
    private final String initPath = "chatBot/src/loadingData.sql";

    public DB() throws FileNotFoundException, SQLException {
        connect(this.schemaName, this.username, this.password);
        init();
        System.out.println(whatActivity("Monday", 14));
    }


    // What course is there on <day> at <time>?
    private ArrayList<String> whatActivity(String day, int time) throws SQLException {
        ArrayList<String> activities = new ArrayList<>();
        String sql ="SELECT a.activity_name " +
                    "FROM dacs_timetable AS t " +
                    "INNER JOIN activities AS a " +
                    "ON t.activity_id = a.activity_id " +
                    "WHERE t.day_name = " + "\"" + day + "\"" + " " +
                    "AND t.start_time <= " + time + " " +
                    "AND t.end_time >= " + time;
        //System.out.println("query-> " + sql);
        ResultSet rs=null;

        try{
            rs = connection.createStatement().executeQuery(sql);
            while(rs.next()) {
                String activity = rs.getString("activity_name");
                activities.add(activity);
            }

        } catch(SQLException ex){
            System.out.println("Smt wrong with executing the query");
            System.out.println(ex.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) { }
                rs = null;
            }
        }
        return activities;
    }

    //TODO: maybe we could try to loop through all of our credentials so that nobody has to switch anything or
    //      all of us have to have the same user on our local MySQL server
    //Just a method to connect to the local server
    private void connect(String dbName, String username, String password) {
        try {
            connectionString = "jdbc:mysql://localhost:3306/"+dbName+"?user="+username+"&password="+password+
                    "&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
            connection = DriverManager.getConnection(connectionString);

        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }
    //TODO: change the loadingData.sql such that evb agrees with what they see
    //a method to initialize the db for anybody if they set up their credentials correctly, subject to change
    private void init() throws FileNotFoundException, SQLException {
        String init = "";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(this.initPath));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        try{
            String currentLine;
            StringBuilder sb = new StringBuilder();
            while ((currentLine=br.readLine())!=null) {
                sb.append(currentLine);
            }
            init = sb.toString();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        try {
            for(String sql : init.split(";")){
                connection.createStatement().executeUpdate(sql);
            }
        } catch (SQLException ex) {
            ex.getErrorCode();
        }
    }

    public static void main(String[] args) throws FileNotFoundException, SQLException {
        DB db = new DB();
    }
}
