package de.netbeacon.xsttapi;

import java.net.ServerSocket;
import java.net.Socket;

class APISocket implements Runnable{

    private ServerSocket serverSocket;
    private Config config;
    private int port;

    APISocket(){
        config = new Config();
        port = Integer.parseInt(config.load("api_port"));
    }

    public void run(){
        try{
            serverSocket = new ServerSocket(port);
            System.out.println("[INFO][API] Using port: "+port);

            while(true){
                Socket clientSocket = serverSocket.accept();
                Thread xpihandler = new Thread(new APIConnectionHandler(clientSocket));
                xpihandler.start();
            }
        }catch (Exception e){
            System.err.println("[ERROR]"+e);
        }finally{
            try{
                serverSocket.close();
            }catch (Exception e){
                System.err.println("[ERROR]"+e);
            }
        }
    }
}
