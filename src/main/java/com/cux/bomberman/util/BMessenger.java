package com.cux.bomberman.util;

import javax.swing.JDialog;
import javax.swing.JOptionPane;


public class BMessenger {

    // tipurile de popUp
    public static Integer LEVEL_FINE = 0;
    public static Integer LEVEL_INFO = 1;
    public static Integer LEVEL_WARNING = 2;
    public static Integer LEVEL_EXCEPTION = 3;
    public static Integer LEVEL_ERROR = 4;

    // unicul obiect de tip Messenger
    private static BMessenger instance = null;

    // constructor privat pentru a preveni instantierea directa
    private BMessenger(){}

    // evita instantierea mai multor obiecte de acest tip si in cazul thread-urilor
    public static synchronized BMessenger getInstance(){
        if (instance == null){
            instance = new BMessenger();
        }
        return instance;
    }

    @Override
    // obiectul nu poate fi clonat
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    // metoda generala pt. afisarea unui popUP
    public void popUp(String message, Integer level){
       if (level.equals(LEVEL_FINE)) popUpFine(message, "FINE");
       if (level.equals(LEVEL_INFO)) popUpInfo(message, "INFO");
       if (level.equals(LEVEL_WARNING)) popUpWarning(message, "WARNING");
       if (level.equals(LEVEL_EXCEPTION)) popUpException(message, "EXCEPTION");
       if (level.equals(LEVEL_ERROR)) popUpError(message, "ERROR");
    }

    ////////////////////////////////////////////////////////////
    // METODE PRIVATE PENTRU AFISAREA POP-URILOR
    ////////////////////////////////////////////////////////////

    // deschide popUp pt. mesaj [nivelul FINE]
    private void popUpFine(String fineMessage, String fineTitle){
        JOptionPane fine = new JOptionPane(fineMessage,
                                           JOptionPane.PLAIN_MESSAGE,
                                           JOptionPane.DEFAULT_OPTION);
        JDialog fine2 = fine.createDialog(null, fineTitle);
        fine2.setVisible(true);
        fine2.setAlwaysOnTop(true);
    }

    // deschide popUp pt. mesaj [nivelul INFO]
    private void popUpInfo(String infoMessage, String infoTitle){
        JOptionPane info = new JOptionPane(infoMessage,
                                           JOptionPane.INFORMATION_MESSAGE,
                                           JOptionPane.DEFAULT_OPTION);
        JDialog info2 = info.createDialog(null, infoTitle);
        info2.setVisible(true);
        info2.setAlwaysOnTop(true);
    }

    // deschide popUp pt. mesaj [nivelul WARNING]
    private void popUpWarning(String warningMessage, String warningTitle){
        JOptionPane warning = new JOptionPane(warningMessage,
                                              JOptionPane.WARNING_MESSAGE,
                                              JOptionPane.DEFAULT_OPTION);
        JDialog warning2 = warning.createDialog(null, warningTitle);
        warning2.setVisible(true);
        warning2.setAlwaysOnTop(true);
    }

    // deschide popUp pt. mesaj [nivelul EXCEPTION]
    private void popUpException(String exceptionMessage, String exceptionTitle){
        JOptionPane exception = new JOptionPane(exceptionMessage,
                                            JOptionPane.ERROR_MESSAGE,
                                            JOptionPane.DEFAULT_OPTION);
        JDialog exception2 = exception.createDialog(null, exceptionTitle);
        exception2.setVisible(true);
        exception2.setAlwaysOnTop(true);
    }

    // deschide popUp pt. mesaj [nivelul ERROR]
    private void popUpError(String errorMessage, String errorTitle){
        JOptionPane error = new JOptionPane(errorMessage,
                                            JOptionPane.ERROR_MESSAGE,
                                            JOptionPane.DEFAULT_OPTION);
        JDialog error2 = error.createDialog(null, errorTitle);
        error2.setVisible(true);
        error2.setAlwaysOnTop(true);
    }

    // deschide popUp pt. confirmarea unei actiuni
    public int popUpConfirm(String confirmMessage, String popUpTitle, int confirmOptions){
        return JOptionPane.showConfirmDialog(null, confirmMessage, popUpTitle, confirmOptions);
    }

    // deschide popUp cu input
    public String popUpPrompt(String promptFor, String inputValue){
        return JOptionPane.showInputDialog(promptFor, inputValue);
    }

    /*
    public static void main(String[] args){
        Messenger mess = Messenger.getInstance();
        mess.popUpWarning("The program is already runing!", "");
        System.exit(0);
    }
    */

}
