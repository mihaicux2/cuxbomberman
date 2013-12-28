package com.cux.bomberman.util;

//import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
//import java.io.PrintStream;
import java.io.IOException;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

//import javax.swing.JDialog;
//import javax.swing.JOptionPane;

public final class BLogger {

    private static BLogger instance = null;
    private Boolean echoErrors = false;

    // nivelul de severitate al mesajului de salvat
    public static Integer LEVEL_FINE = 0;
    public static Integer LEVEL_INFO = 1;
    public static Integer LEVEL_WARNING = 2;
    public static Integer LEVEL_EXCEPTION = 3;
    public static Integer LEVEL_ERROR = 4;

    // constructor ce duce la evitarea instantierii directe
    private BLogger(){}

    // evita instantierea mai multor obiecte de acest tip si in cazul thread-urilor
    public static synchronized BLogger getInstance(){
        if (instance == null){
            instance = new BLogger();
        }
        return instance;
    }

    // nu permite clonarea obiectului
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    // seteaza daca la salvarea unui mesaj in log, afiseaza mesajul printr-un popUp
    public void setEcho(Boolean echo){
        echoErrors = echo;
    }

    // afla data curenta
    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    // salveaza mesaje in log
    public void log(Integer level, String message){
       if (level.equals(LEVEL_FINE)) logFine(message+"\r\n");
       if (level.equals(LEVEL_INFO)) logInfo(message+"\r\n");
       if (level.equals(LEVEL_WARNING)) logWarning(message+"\r\n");
       if (level.equals(LEVEL_EXCEPTION)) logException(message+"\r\n");
       if (level.equals(LEVEL_ERROR)) logError(message+"\r\n");
    }

    // afiseaza mesaje [daca e setat in config.ini, deschide popUp]
    private void echo(String message, Integer level){
        System.out.println(message);
        if (echoErrors.equals(true)){
            BMessenger.getInstance().popUp(message, level);
        }
    }

    // salveaza in log exceptiile [cu tot cu tracking - fisierul, linia etc.]
    public void logException2(Exception e){
        StackTraceElement[] tracker = e.getStackTrace();
        String exceptionStr = "";
        for (StackTraceElement tr : tracker){
            exceptionStr += tr.toString()+"\r\n";
        }
        log(BMessenger.LEVEL_EXCEPTION, exceptionStr);
        //echo(e.getMessage(), LEVEL_EXCEPTION);
        echo(e.getMessage(), LEVEL_EXCEPTION);
    }

    // salveaza in log erorile Throwable [cu tot cu tracking - fisierul, linia etc.]
    public void logThrowable(Throwable e){
        StackTraceElement[] tracker = e.getStackTrace();
        String exceptionStr = "";
        for (StackTraceElement tr : tracker){
            exceptionStr += tr.toString()+"\r\n";
        }
        log(BMessenger.LEVEL_EXCEPTION, exceptionStr);
        //echo(e.getMessage(), LEVEL_EXCEPTION);
        echo(e.getMessage(), LEVEL_EXCEPTION);
    }

    ////////////////////////////////////////////////////////////
    // METODE PRIVATE PENTRU SALVAREA MESAJELOR
    ////////////////////////////////////////////////////////////

    // salveaza mesaj in log [nivelul FINE]
    private void logFine(String fineMessage){
        String str = "FINE [ "+getDateTime()+" ] : "+fineMessage;
        try{
            FileWriter fstream = new FileWriter("log.txt", true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(str);
            out.newLine();
            out.close();
        }catch (IOException ioe){//Catch exception if any
            System.err.println("Error creating/loading log file: " + ioe.getMessage()
                    + "\n FINE message to be logged : "+str);
        }
        echo(str, LEVEL_FINE);
    }

    // salveaza mesaj in log [nivelul INFO]
    private void logInfo(String infoMessage){
        String str = "INFO [ "+getDateTime()+" ] : "+infoMessage;
        try{
            FileWriter fstream = new FileWriter("log.txt", true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(str);
            out.newLine();
            out.close();
        }catch (IOException ioe){//Catch exception if any
            System.err.println("Error creating/loading log file: " + ioe.getMessage()
                    + "\n INFO message to be logged : "+str);
        }
        echo(str, LEVEL_INFO);
    }

    // salveaza mesaj in log [nivelul WARNING]
    private void logWarning(String warningMessage){
        String str = "WARNING [ "+getDateTime()+" ] : "+warningMessage;
        try{
            FileWriter fstream = new FileWriter("log.txt", true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(str);
            out.newLine();
            out.close();
        }catch (IOException ioe){//Catch exception if any
            System.err.println("Error creating/loading log file: " + ioe.getMessage()
                    + "\n WARNING message to be logged : "+str);
        }
        echo(str, LEVEL_WARNING);
    }

    // salveaza mesaj in log [nivelul EXCEPTION]
    private void logException(String errorMessage){
        String str = "EXCEPTION [ "+getDateTime()+" ] : "+errorMessage;
        try{
            FileWriter fstream = new FileWriter("log.txt", true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(str);
            out.newLine();
            out.close();
        }catch (IOException ioe){//Catch exception if any
            System.err.println("Error creating/loading log file: " + ioe.getMessage()
                    + "\n EXCEPTION message to be logged : "+str);
        }
        echo(str, LEVEL_EXCEPTION);
    }

    // salveaza mesaj in log [nivelul ERROR]
    private void logError(String errorMessage){
        String str = "ERROR [ "+getDateTime()+" ] : "+errorMessage;
        try{
            FileWriter fstream = new FileWriter("log.txt", true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(str);
            out.newLine();
            out.close();
        }catch (IOException ioe){//Catch exception if any
            System.err.println("Error creating/loading log file: " + ioe.getMessage()
                    + "\n ERROR message to be logged : "+str);
        }
        echo(str, LEVEL_ERROR);
    }

}
