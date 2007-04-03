package com.xerox.amazonws.tools;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * A place to put a static method that looks for a Log4j.xml file
 * and does the configureAndWatch stuff to configure log4j.
 * @author John Walsh
 *
 */
public class LoggingConfigurator {
    private static boolean loggingConfigured = false;
    private static final String LOGGER_CONFIG = "Log4j.xml";
    
    public static Logger configureLogging(Class callingClass) {
        if (!loggingConfigured) {
            URL configFileURL = callingClass.getClassLoader().getResource(LOGGER_CONFIG);
            if (configFileURL == null){
                System.err.println("The log4j configuration file \""+LOGGER_CONFIG+
                    "\" was not found on the classpath.");
            }
            File configFile = null;
            try {   
                configFile = new File(configFileURL.toURI());
            }catch (URISyntaxException e) {
                System.err.println("Log4J config file URL "+configFileURL+
                        " couldn't be converted to a URI.");
                e.printStackTrace(System.err);
            }
            DOMConfigurator.configureAndWatch(configFile.getAbsolutePath());
            loggingConfigured = true;
            System.out.println("\n\nLogging initial configuration complete according to file "+configFileURL);
        }
        System.out.println("Log4j logger created.");
        return Logger.getLogger(callingClass.getName());
    }
}
