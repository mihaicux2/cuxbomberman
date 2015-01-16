package com.cux.bomberman.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class is used as a general purpose logger throughout the whole
 * application 
 * 
 * @version 1.0
 * @author  Mihail Cuculici (mihai.cuculici@gmail.com)
 * @author  http://www.
 */
public final class BLogger {

    /**
     * The only allowed instance of the BLogger class
     */
    private static BLogger instance = null;
    
    /**
     * Simple check to see if the messages to be stored should be echoed to the server's output
     */
    private Boolean echoErrors = false;

    /**
     * Logged message security lever
     */
    public static Integer LEVEL_FINE = 0;
    public static Integer LEVEL_INFO = 1;
    public static Integer LEVEL_WARNING = 2;
    public static Integer LEVEL_EXCEPTION = 3;
    public static Integer LEVEL_ERROR = 4;

    /**
     * Private constructor to disallow direct instantiation
     */
    private BLogger(){}

    /**
     * Public static method used to implement the Singleton pattern. Only one instance of this class is allowed
     * @return The only allowed instance of this class
     */
    public static synchronized BLogger getInstance(){
        if (instance == null){
            instance = new BLogger();
        }
        return instance;
    }

    /**
     * Overwritten method to disallow cloning of the instantiated object. [Singleton pattern]
     * @return
     * @throws CloneNotSupportedException 
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Public method used to tell the logger if it should also raise a popup during the logged messages
     * @param echo boolean to set the echoErrors property
     */
    public void setEcho(Boolean echo){
        echoErrors = echo;
    }

    /**
     * Private method used to get the current time, used for logging the messages
     * @return The curent datetime, in a precise method
     */
    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * Public method used to store the messages
     * @param level the security level of the stored messages
     * @param message the message to be stored
     */
    public void log(Integer level, String message){
       if (level.equals(LEVEL_FINE)) logFine(message+"\r\n");
       if (level.equals(LEVEL_INFO)) logInfo(message+"\r\n");
       if (level.equals(LEVEL_WARNING)) logWarning(message+"\r\n");
       if (level.equals(LEVEL_EXCEPTION)) logException(message+"\r\n");
       if (level.equals(LEVEL_ERROR)) logError(message+"\r\n");
    }

    /**
     * Private method used to show the logged messages on the screen. Also, if the echoErrors property<br />
     * is set to true, a pop-up will appear foreach logged message
     * @param message the message to be shown
     * @param level the security level of the given message
     */
    private void echo(String message, Integer level){
        System.out.println(message);
        if (echoErrors.equals(true)){
            BMessenger.getInstance().popUp(message, level);
        }
    }

    /**
     * Public method used to log raised exceptions
     * @param e The exception to be logged
     */
    public void logException2(Exception e){
        StackTraceElement[] tracker = e.getStackTrace();
        String exceptionStr = "";
        for (StackTraceElement tr : tracker){
            exceptionStr += tr.toString()+"\r\n";
        }
        log(BMessenger.LEVEL_EXCEPTION, exceptionStr);
        //echo(e.getMessage(), LEVEL_EXCEPTION);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        echo(sw.toString(), LEVEL_EXCEPTION);
    }

    /**
     * Public method used to log throwables
     * @param e The throwable to be logged
     */
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
    // PRIVATE METHODS FOR SAVING THE LOG MESSAGES
    ////////////////////////////////////////////////////////////

    /**
     * Private method used to log a normal message
     * @param fineMessage the message to be logged
     */
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

    /**
     * Private method used to log an information message
     * @param infoMessage the message to be logged
     */
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

    /**
     * Private method used to log a warning message
     * @param warningMessage the message to be logged
     */
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

    /**
     * Private method used to log an exception message
     * @param exceptionMessage the message to be logged
     */
    private void logException(String exceptionMessage){
        String str = "EXCEPTION [ "+getDateTime()+" ] : "+exceptionMessage;
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

    /**
     * Private method used to log an error message
     * @param errorMessage the message to be logged
     */
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
