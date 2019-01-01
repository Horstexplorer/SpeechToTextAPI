package de.netbeacon.xsttapi;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

class STT {

    private String defaultstoragepath;
    private String username;
    private String filename;
    private Config config;
    private String acousticmodelpath;
    private String dictionary;
    private String languagemodel;
    private int error;

    STT(String usern, String filen){
        error = 0;
        config = new Config();
        defaultstoragepath = config.load("api_defaultstoragelocation");
        acousticmodelpath = config.load("stt_acousticmodelpath");
        dictionary = config.load("stt_dictionary");
        languagemodel = config.load("stt_languagemodel");

        username = usern;
        filename = filen;

        File directorya = new File(acousticmodelpath);
        if (!directorya.exists()) {
            directorya.mkdir();
        }
        File directoryb = new File(acousticmodelpath+dictionary);
        if (!directoryb.exists()) {
            error++;
        }
        File directoryc = new File(acousticmodelpath+languagemodel);
        if (!directoryc.exists()) {
            error++;
        }
    }

    int checkerror(){
        return error;
    }

    String speechtotext(){
        String hypothesis = " ";
        String file = defaultstoragepath+"/"+username+"/"+filename+".wav";
        if (new File(file).exists()){
            try{

                Configuration configuration = new Configuration();

                configuration.setAcousticModelPath(acousticmodelpath);
                configuration.setDictionaryPath(acousticmodelpath+dictionary);
                configuration.setLanguageModelPath(acousticmodelpath+languagemodel);

                StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(configuration);
                InputStream stream = new FileInputStream(new File(file));

                recognizer.startRecognition(stream);
                SpeechResult result;
                while ((result = recognizer.getResult()) != null) {
                    hypothesis = result.getHypothesis();
                }
                recognizer.stopRecognition();

            }catch (Exception e){
                System.out.println("[ERROR] "+e);
            }
        }else {
            System.out.println("[ERROR] File: "+filename+" from user "+username+" missing.");
        }

        return hypothesis;
    }
}
