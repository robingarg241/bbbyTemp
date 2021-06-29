package com.bbby.aem.core.util;

import org.slf4j.Logger;

import java.util.Calendar;

public class LogUtils {

    public static void debugLog(Logger log, String message, String clue) {
        try {
            String logMessage = "\n###########  Debug: " + clue + " ############\n\n\n"
                + message
                + "\n\n===============================================================================\n";

            //log.error(logMessage);
        } catch (Exception e) {}
    }

    public static void debugLog(Logger log, Integer message, String clue) {
        try {
            String _message = new Integer(message).toString();
            debugLog(log, _message, clue);
        } catch (Exception e) {}
    }

    public static void debugLog(Logger log, Long message, String clue) {
        try {
            String _message = new Long(message).toString();
            debugLog(log, _message, clue);
        } catch (Exception e) {}
    }

    public static void debugLog(Logger log, Calendar message, String clue) {
        try {
            String _message = message.getTime().toString();
            debugLog(log, _message, clue);
        } catch (Exception e) {}
    }


    public static void debugLog(Logger log, Boolean message, String clue) {
        try {
            String _message = new Boolean(message).toString();
            debugLog(log, _message, clue);
        } catch (Exception e) {}
    }

    public static void debugLocation(Logger log, String className , String lineNumber) {
        try {
            String logMessage = "\n#################################################################"
                + "\n################# " + className + " : " + lineNumber + " ###################"
                + "\n#################################################################";

            //log.error(logMessage);
        } catch (Exception e) {}
    }


}
