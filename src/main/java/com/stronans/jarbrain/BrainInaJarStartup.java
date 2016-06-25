package com.stronans.jarbrain;


import com.stronans.jarbrain.checkshutdown.ShutdownCheck;
import com.stronans.jarbrain.displayoutput.DisplayComment;
import com.stronans.jarbrain.tempcheck.CheckTemp;
import com.stronans.jarbrain.tempcheck.SerialComms;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.util.Properties;

/**
 * Starting point for Jar In a Brain program.
 * <p/>
 * Created by S.King on 11/04/2015.
 */
public class BrainInaJarStartup {
    private static final long PAUSE = 40000;            // 40 seconds
    /**
     * The <code>Logger</code> to be used.
     */
    private static Logger log = Logger.getLogger(DisplayComment.class);
    private static volatile boolean finished = false;
    private static SerialComms comms;
    private static DisplayComment displayComment;
    private static CheckTemp tempCheck;
    private static ShutdownCheck shutdownCheck;

    /**
     * Handles the loading of the log4j configuration. properties file must be
     * on the classpath.
     *
     * @throws RuntimeException
     */
    private static void initLogging() throws RuntimeException {
        try {
            Properties properties = new Properties();
            properties.load(BrainInaJarStartup.class.getClassLoader().getResourceAsStream("log4j.properties"));
            PropertyConfigurator.configure(properties);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to load logging properties for System");
        }
    }

    public synchronized static boolean finished() {
        return finished;
    }

    public static void main(String args[]) {

        // Setup initial logging (setup rollover and discard)
        try {
            initLogging();
        } catch (RuntimeException ex) {
            System.out.println("Error setting up log4j logging");
            System.out.println("Application will continue but without any logging.");
        }

        try {
            comms = new SerialComms("/dev/ttyACM0");

            comms.startComms();

            displayComment = new DisplayComment(comms);
            // Spawn off display comment thread for LCD.
            log.info("Starting message display");
            Thread display = new Thread(displayComment);
            display.start();

            // Watch for temperature check (comms with comment thread & ANano)
            tempCheck = new CheckTemp(comms);
            log.info("Starting temperature check");
            Thread temp = new Thread(tempCheck);
            temp.start();

            // Check for shutdown signal from IR (Signal ANano and shutdown).
            shutdownCheck = new ShutdownCheck();
            log.info("Starting IR Shutdown check");
            Thread shutdown = new Thread(shutdownCheck);
            shutdown.start();

            Runtime.getRuntime().addShutdownHook(new Thread() {
                                                     @Override
                                                     public void run() {
                                                         finished = true;
                                                         comms.write("END\n");
                                                         comms.endComms();

                                                         displayComment.shutdown();
                                                         log.info("shutdown LCD screen.");
                                                         log.info("Exiting program.");
                                                     }
                                                 }
            );

            log.info("Eveything now setup and running");

            while (!BrainInaJarStartup.finished()) {
                Thread.sleep(PAUSE);

                switch (shutdownCheck.lastKey()) {
                    case "KEY_SUSPEND":
                        break;

                    case "KEY_EXIT":
                        finished = true;
                        break;
                }

                if (tempCheck.currentTemp() > 35) {
                    displayComment.overHeating(true);
                } else {
                    displayComment.overHeating(false);
                }
            }

            displayComment.shuttingDown();
            Thread.sleep(PAUSE);

            displayComment.shutdown();
            Runtime.getRuntime().exec("sudo shutdown -h -P now");
            System.exit(0);

        } catch (InterruptedException e) {
            log.error(" ==>> PROBLEMS WITH SERIAL COMMUNICATIONS: " + e.getMessage(), e);
        } catch (IOException ioe) {
            log.error(" ==>> PROBLEMS WITH CALLING SUDO SHUTDOWN: " + ioe.getMessage(), ioe);
        }
    }
}
