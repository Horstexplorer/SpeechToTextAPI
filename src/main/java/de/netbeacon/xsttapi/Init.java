package de.netbeacon.xsttapi;

public class Init {

    static boolean error = false;

    public static void main(String[]args){
        System.out.println("----------------------------------------[ XSTTAPI ]----------------------------------------");
        Config config = new Config();
        System.out.println("[INFO] Version: "+config.version());
        STT stt = new STT("","");
        if(stt.checkerror() == 0){
            System.out.println("[INFO] STT: OK");
        }else{
            System.out.println("[ERROR] STT: Files missing");
            error = true;
        }
        System.out.println("-------------------------------------------------------------------------------------------");

        if(Boolean.parseBoolean(config.load("activated")) && error){
            System.out.println("[INFO] OK");
            //Socket
            Thread api = new Thread(new APISocket());
            api.start();
        }else{
            System.out.println("[INFO] Deactivated, please check files/config");
        }

    }

}
