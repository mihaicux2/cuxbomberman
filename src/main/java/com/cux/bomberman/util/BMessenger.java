package com.cux.bomberman.util;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author mihaicux
 */
public class BMessenger {

    // tipurile de popUp
    public static Integer LEVEL_FINE = 0;
    public static Integer LEVEL_INFO = 1;
    public static Integer LEVEL_WARNING = 2;
    public static Integer LEVEL_EXCEPTION = 3;
    public static Integer LEVEL_ERROR = 4;

    /**
     * The only allowed instance of the BMessenger class
     */
    private static BMessenger instance = null;

    /**
     * Private constructor to disallow direct instantiation
     */
    private BMessenger(){}

    // evita instantierea mai multor obiecte de acest tip si in cazul thread-urilor
    public static synchronized BMessenger getInstance(){
        if (instance == null){
            instance = new BMessenger();
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
     * Public method to raise a given popUP
     * @param message The message to be raised
     * @param level The level of security for the given message
     */
    public void popUp(String message, Integer level){
       if (level.equals(LEVEL_FINE)) popUpFine(message, "FINE");
       if (level.equals(LEVEL_INFO)) popUpInfo(message, "INFO");
       if (level.equals(LEVEL_WARNING)) popUpWarning(message, "WARNING");
       if (level.equals(LEVEL_EXCEPTION)) popUpException(message, "EXCEPTION");
       if (level.equals(LEVEL_ERROR)) popUpError(message, "ERROR");
    }

    ////////////////////////////////////////////////////////////
    // Private methods for the popUps
    ////////////////////////////////////////////////////////////

    /**
     * Open a popUp window [normal state]
     * @param fineMessage The message to be showed
     * @param fineTitle The popUp's title
     */
    private void popUpFine(String fineMessage, String fineTitle){
        JOptionPane fine = new JOptionPane(fineMessage,
                                           JOptionPane.PLAIN_MESSAGE,
                                           JOptionPane.DEFAULT_OPTION);
        JDialog fine2 = fine.createDialog(null, fineTitle);
        fine2.setVisible(true);
        fine2.setAlwaysOnTop(true);
    }

    /**
     * Open a popUp window [information state]
     * @param infoMessage The message to be showed
     * @param infoTitle The popUp's title
     */
    private void popUpInfo(String infoMessage, String infoTitle){
        JOptionPane info = new JOptionPane(infoMessage,
                                           JOptionPane.INFORMATION_MESSAGE,
                                           JOptionPane.DEFAULT_OPTION);
        JDialog info2 = info.createDialog(null, infoTitle);
        info2.setVisible(true);
        info2.setAlwaysOnTop(true);
    }

    /**
     * Open a popUp window [warning state]
     * @param warningMessage The message to be showed
     * @param warningTitle The popUp's title
     */
    private void popUpWarning(String warningMessage, String warningTitle){
        JOptionPane warning = new JOptionPane(warningMessage,
                                              JOptionPane.WARNING_MESSAGE,
                                              JOptionPane.DEFAULT_OPTION);
        JDialog warning2 = warning.createDialog(null, warningTitle);
        warning2.setVisible(true);
        warning2.setAlwaysOnTop(true);
    }

    /**
     * Open a popUp window [error state]
     * @param exceptionMessage The message to be showed
     * @param exceptionTitle The popUp's title
     */
    private void popUpException(String exceptionMessage, String exceptionTitle){
        JOptionPane exception = new JOptionPane(exceptionMessage,
                                            JOptionPane.ERROR_MESSAGE,
                                            JOptionPane.DEFAULT_OPTION);
        JDialog exception2 = exception.createDialog(null, exceptionTitle);
        exception2.setVisible(true);
        exception2.setAlwaysOnTop(true);
    }

    /**
     * Open a popUp window [error state]
     * @param errorMessage The message to be showed
     * @param errorTitle The popUp's title
     */
    private void popUpError(String errorMessage, String errorTitle){
        JOptionPane error = new JOptionPane(errorMessage,
                                            JOptionPane.ERROR_MESSAGE,
                                            JOptionPane.DEFAULT_OPTION);
        JDialog error2 = error.createDialog(null, errorTitle);
        error2.setVisible(true);
        error2.setAlwaysOnTop(true);
    }

    /**
     * Open a confirm window
     * @param confirmMessage The message to confirm
     * @param popUpTitle The popUp's title
     * @param confirmOptions The confirmation buttons
     * @return 0 if not accepted, > 0  if the confirmation is accepted
     */
    public int popUpConfirm(String confirmMessage, String popUpTitle, int confirmOptions){
        return JOptionPane.showConfirmDialog(null, confirmMessage, popUpTitle, confirmOptions);
    }

    /**
     * Open a prompt window
     * @param promptFor The window's title
     * @param inputValue The default input value
     * @return The inputed value
     */
    public String popUpPrompt(String promptFor, String inputValue){
        return JOptionPane.showInputDialog(promptFor, inputValue);
    }

}
