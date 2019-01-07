package de.netbeacon.xsttapi;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;

import java.io.File;
import java.io.FileInputStream;

class STT {

    private String defaultstoragepath;
    private String username;
    private String filename;
    private String acousticmodelpath;
    private String dictionary;
    private String languagemodel;
    private int maxexectime = 60;

    STT(String usern, String filen, String language){
        Config config = new Config();
        defaultstoragepath = config.load("api_defaultstoragelocation");
        acousticmodelpath = config.load("stt_acousticmodelpath");
        dictionary = config.load("stt_dictionary");
        languagemodel = config.load("stt_languagemodel");
        maxexectime = Integer.parseInt(config.load("api_maxprocesstime"));
        username = usern;
        filename = filen;
        language = "/"+language+"/";

        File directorya = new File(acousticmodelpath);
        if (!directorya.exists()) {
            directorya.mkdir();
        }
        File directoryb = new File(acousticmodelpath+language);
        if (!directoryb.exists()) {
            fbm();
        }else{
            File directoryc = new File(acousticmodelpath+language+dictionary);
            if (!directoryc.exists()) {
                //fallback to default
                fbm();

            }
            File directoryd = new File(acousticmodelpath+language+languagemodel);
            if (!directoryd.exists()) {
                //fallback to default
                fbm();
            }
        }
    }

    void fbm(){
        acousticmodelpath = "resource:/edu/cmu/sphinx/models/en-us/en-us";
        dictionary = "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict";
        languagemodel = "resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin";
    }

    String speechtotext(){
        System.out.println("[INFO]> Processing request from "+username+" ("+filename+")");
        String hypothesis = " ";
        String file = defaultstoragepath+"/"+username+"/"+filename+".wav";
        if (new File(file).exists()){
            try{

                Configuration configuration = new Configuration();

                configuration.setAcousticModelPath(acousticmodelpath);
                configuration.setDictionaryPath(dictionary);
                configuration.setLanguageModelPath(languagemodel);

                //should only be allowed to take max 60 seconds
                Thread breaker = new Thread(new Runnable() {
                    public void run() {
                        try{
                            Thread.sleep(maxexectime*1000);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });

                StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(configuration);
                recognizer.startRecognition(new FileInputStream(new File(file)));
                breaker.start();
                SpeechResult result;


                while ((result = recognizer.getResult()) != null && breaker.isAlive()) {
                    hypothesis = hypothesis+" "+result.getHypothesis().trim();
                    System.out.println("[INFO]> Hypothesis from "+username+" ("+filename+") updated...");
                }
                recognizer.stopRecognition();

            }catch (Exception e){
                System.out.println("[ERROR] "+e);
                e.printStackTrace();
            }
        }else {
            System.out.println("[ERROR] File: "+filename+" from user "+username+" missing.");
        }
        System.out.println("[INFO]> Finished request from "+username);
        return hypothesis.trim();
    }
}
