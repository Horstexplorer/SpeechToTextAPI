package de.netbeacon.xsttapi;

import java.io.OutputStream;
import java.io.PrintStream;

public class Init {

    static boolean error = false;

    public static void main(String[]args){
        //null
        System.setErr(new PrintStream(new OutputStream() {
            public void write(int b) {
            }
        }));

        System.out.println("----------------------------------------[ XSTTAPI ]----------------------------------------");
        Config config = new Config();
        System.out.println("[INFO] Version: "+config.version());
        new STT("","", "");
        System.out.println("-------------------------------------------------------------------------------------------");

        if(Boolean.parseBoolean(config.load("activated")) && !error){
            System.out.println("[INFO] OK");
            //Socket
            Thread api = new Thread(new APISocket());
            api.start();
        }else{
            System.out.println("[INFO] Deactivated, please check files/config");
        }

    }

}
