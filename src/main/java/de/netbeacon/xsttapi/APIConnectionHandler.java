package de.netbeacon.xsttapi;

import java.io.*;
import java.net.Socket;
import java.util.Date;

class APIConnectionHandler implements Runnable {

    private Socket clientSocket;
    private Config config;

    APIConnectionHandler(Socket c){
        clientSocket = c;
        config = new Config();
    }

    public void run(){

        //default storage values
        String defaultstorage = config.load("api_defaultstoragelocation");
        String storagepath = defaultstorage+"/unknown/";
        String filename = "unknownuser";
        String username = "unknown";

        try{
            //stream
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            DataInputStream din = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            //input vars
            String input;
            int filesize = 0;
            boolean authorized = false;
            boolean infilesize = false;


            //check for right method
            input = in.readLine();

            if(input.contains("PUT")){
                //get the headers
                while((input = in.readLine()) != null){

                    //get the username and password to authorize access
                    if(input.contains("Auth:")){
                        boolean authaccepted = false;
                        String userpass = input.replace("Auth: ", "").trim();
                        username = userpass.substring(0,userpass.indexOf(":")); //part before :
                        String pass = userpass.substring(userpass.indexOf(":")); //part after :

                        //Authentificator
                        if (!username.isEmpty() && !pass.isEmpty()){
                            if (!Boolean.parseBoolean(config.load("auth_testmode"))){
                                APIAuth apiauth = new APIAuth();
                                authaccepted = apiauth.authenticate(username, pass);
                            }else{
                                authaccepted = true;
                            }
                        }

                        if(authaccepted){
                            authorized = true;
                            //get dir and file ready
                            File rootdirectory = new File(defaultstorage);
                            if (!rootdirectory.exists()) {
                                rootdirectory.mkdir();
                            }
                            File userdirectory = new File(defaultstorage+"/"+username+"/");
                            if (!userdirectory.exists()) {
                                userdirectory.mkdir();
                            }
                            storagepath = defaultstorage+"/"+username+"/";
                            filename = "record_"+(userdirectory.list().length+1)+"";

                        }
                    }

                    //get the filesize and check if its not larger than allowed
                    if(input.contains("Content-Length:")){
                        filesize = Integer.parseInt(input.replace("Content-Length: ", ""));
                        if (filesize <= Integer.parseInt(config.load("api_maxuploadsize")) && filesize > 0){
                            infilesize = true;
                        }
                    }

                    //if a 100-continue header is expected we should send one
                    if(input.contains("Expect: 100-continue")){
                        out.write("HTTP/2 100 Continue\r\n");
                        out.flush();
                    }

                    //break will occure when we recived all headers. After thatthe file will be send that we'll have to take care of differently.
                    if(input.isEmpty()){
                        break;
                    }
                }

                //get & write the file (not very good, but it works)
                if(authorized && infilesize){

                    //basic preparations
                    byte[] bytes = new byte[filesize];
                    OutputStream fos = new FileOutputStream(storagepath+filename+".wav");
                    int count;
                    int currentsize = 0;

                    //write file step by step until we wrote the whole file
                    while((count = din.read(bytes))>0){
                        fos.write(bytes,0,count);
                        currentsize += count;
                        if (currentsize == filesize){
                            break;
                        }
                    }
                    fos.flush();
                    fos.close();
                    System.out.println("[INFO]> File ("+filesize+" byte) recived from user: "+username+".");

                    //do different work here but signalize that we do work
                    out.write("HTTP/2 102 Processing\r\n");
                    out.flush();
                    String tfs = " ";
                    //Wörk :D

                    //STT stt = new STT(username,filename);
                    //tfs = stt.speechtotext();

                    //send response
                    output(out,"HTTP/2 200 OK\r\n","{\"status\":\"200\",\"info\":\"OK\",\"tfs\":\""+tfs+"\"}");
                }else{
                    if(!authorized){
                        output(out,"HTTP/2 401 Unauthorized\n","{\"status\":\"401\",\"info\":\"Wrong User/Password\"}");
                    }
                    if(authorized && !infilesize){
                        output(out,"HTTP/2 413 Request Entity Too Large\n","{\"status\":\"413\",\"info\":\"File Larger Than"+config.load("api_maxuploadsize")+" Byte\"}");
                    }
                }
            }else if(input.contains("GET")){
                //we keep this here to process feedback (if answer nr # was correct or not)
                output(out,"HTTP/2 200 OK\r\n","{\"status\":\"200\",\"info\":\"OK\"}");
            }else{
                output(out,"HTTP/2 405 Method Not Allowed","{\"status\":\"405\",\"info\":\"PUT (or GET) Required\"}");
            }

            end(out, in, clientSocket);

        }catch(Exception e){
            System.out.println("[Error] "+e);
        }
    }

    private void output(BufferedWriter out,String responsecode, String answer) throws Exception{
        try{
            out.write(responsecode+"\r\n");
            out.write("Date:"+new Date()+"\r\n");
            out.write("Server: XeniaSTTAPI\r\n");
            out.write("Content-Type: application/json\r\n");
            out.write("Content-Length: "+answer.length()+"\r\n");
            out.write("\r\n");
            if(!answer.isEmpty()){
                out.write(answer);
            }
            out.flush();
        }catch (Exception e){
            System.out.println("[ERROR] "+e);
        }
    }
    //Closing everything
    private void end(BufferedWriter out, BufferedReader in,Socket clientSocket) throws Exception {
        try{
            out.close();
            in.close();
            clientSocket.close();
        }catch (Exception e){
            System.out.println("[ERROR] "+e);
        }
    }
}
