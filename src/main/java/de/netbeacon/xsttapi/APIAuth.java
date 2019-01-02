package de.netbeacon.xsttapi;

import org.springframework.security.crypto.bcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

class APIAuth {

    private Config config;
    private String mysqlserver;
    private String mysqlserverport;
    private String mysqldb;
    private String mysqltable;
    private String mysqluser;
    private String  mysqlpassword;

    APIAuth(){
        config = new Config();
        mysqlserver = config.load("auth_sqlserver");
        mysqlserverport = config.load("auth_mysqlserverport");
        mysqldb = config.load("auth_sqldb");
        mysqltable = config.load("auth_sqltable");
        mysqluser = config.load("auth_sqluser");
        mysqlpassword = config.load("auth_sqlpass");
    }

    boolean authenticate(String username, String password){
        boolean reeturn = false;

        try{
            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = "jdbc:mysql://"+mysqlserver+":"+mysqlserverport+"/";
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl, mysqluser, mysqlpassword);

            String query = "SELECT password FROM "+mysqltable+" WHERE username=?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, username);

            ResultSet rs = preparedStatement.executeQuery();

            String passwordhash = "";
            while (rs.next()){
                passwordhash = rs.getString("passwordhash");
            }

            //Check if pass and hash match
            if(BCrypt.checkpw(password, passwordhash)){
                reeturn = true;
            }

        }catch (Exception e){
            System.out.println("[ERROR] "+e);
        }

        return reeturn;
    }

}
