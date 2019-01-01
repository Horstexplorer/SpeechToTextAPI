package de.netbeacon.xsttapi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;

class Config {


    Config(){
        //Check if config file exist
        File configfile = new File("sys.config");
        if (!configfile.exists()) {
            //Create the file
            createconfigfile();
        }
    }

    private void createconfigfile(){
        Properties prop = new Properties();

        try {
            prop.setProperty("activated", "false");

            prop.setProperty("api_port", "9999");
            prop.setProperty("api_maxuploadsize","5000000");
            prop.setProperty("api_defaultstoragelocation","./upload");

            prop.setProperty("stt_acousticmodelpath","./sttresources");
            prop.setProperty("stt_dictionary","dictonary.dic");
            prop.setProperty("stt_languagemodel","languagemodel.lm");

            prop.store(new FileOutputStream("sys.config"), null);
        }catch(Exception e) {
            System.err.println("[ERROR] "+e);
            System.exit(1);
        }
    }

    String load(String value) {

        Properties prop = new Properties();
        InputStream input;
        String result = "";

        try {
            input = new FileInputStream("sys.config");
            prop.load(input);
            result = prop.getProperty(value);
        }catch(Exception e) {
            System.err.println("[ERROR] "+e);
            System.exit(1);
        }

        return result;
    }

    String version() { //should be a file in jar, mb later
        String vers= "1.0.0.1";
        String build = "001685";
        String release = "a";
        return vers+"-"+build+"_"+release;
    }

}


