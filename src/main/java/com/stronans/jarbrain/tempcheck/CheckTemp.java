package com.stronans.jarbrain.tempcheck;

import com.stronans.jarbrain.BrainInaJarStartup;
import org.apache.log4j.Logger;

/**
 * Checks the temperature of the Jar
 * Created by S.King on 21/05/2016.
 */
public class CheckTemp implements Runnable {
    /**
     * The <code>Logger</code> to be used.
     */
    private final static Logger log = Logger.getLogger(CheckTemp.class);
    private double currentTemp = 0.0;
    private double currentHumidity = 0.0;
    private final SerialComms comms;

    public CheckTemp(SerialComms comms) {
        this.comms = comms;
    }

    public double currentTemp() {
        return this.currentTemp;
    }

    public double currentHumidity() {
        return this.currentHumidity;
    }

    private void processMessage(String message)
    {
        try {
            log.info("Message [" + message + "]" );

            if (message.startsWith("H")) {
                int h_index = message.indexOf(" ");       // Is there anything else in the string?
                if(h_index != -1) {
                    String substring = message.substring(1, h_index);
                    currentHumidity = Double.parseDouble(substring);
                    message = message.substring(h_index + 1);
                }
            }

            if (message.startsWith("T")) {
                int index = message.indexOf(" ");       // Is there anything else in the string?
                if(index != -1) {
                    String substring = message.substring(1, index);
                    currentTemp = Double.parseDouble(substring);
                }
            }
        }catch(NumberFormatException nfe)
        {
            log.error(nfe);
        }
    }

    @Override
    public void run() {
        try {

            while (!BrainInaJarStartup.finished()) {
                String message = comms.messages().take();

                processMessage(message);

                // print out the data received to the console and logfile
                log.info("Current Temp:" + currentTemp() );
            }

        } catch (InterruptedException e) {
            log.error(" ==>> PROBLEMS WITH SERIAL COMMUNICATIONS: " + e.getMessage(), e);
        }
    }
}
