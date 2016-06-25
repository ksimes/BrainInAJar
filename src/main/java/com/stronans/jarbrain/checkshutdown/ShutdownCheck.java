package com.stronans.jarbrain.checkshutdown;

import com.stronans.jarbrain.BrainInaJarStartup;
import org.apache.log4j.Logger;
import org.lirc.LIRCException;

/**
 * Created by S.King on 27/05/2016.
 */
public class ShutdownCheck implements Runnable {
    /**
     * The <code>Logger</code> to be used.
     */
    private final static Logger log = Logger.getLogger(ShutdownCheck.class);
    private static IRConnection irConnection;
    private static String lastKey = "";

    public ShutdownCheck() {
    }

    // [0000000000ff30cf 01 KEY_SUSPEND test1.conf]
    String decode(String message) {
        String result = "";

        if (message != null && !message.isEmpty()) {
            String temp = message.substring(17);    // First space

            int h_index = temp.indexOf(" ");       // Is there anything else in the string?
            if (h_index != -1) {
                String keyPressTypeString = temp.substring(0, h_index);
                int keypress = Integer.parseInt(keyPressTypeString);

                if (keypress == 1) {      // 00 is keydown, 01 is keyup, 02 is repeat
                    String keyPressString = temp.substring(h_index + 1);
                    int e_index = keyPressString.indexOf(" ");       // Is there anything else in the string?
                    if (e_index != -1) {
                        result = keyPressString.substring(0, e_index);
                    }
                }
            }
        }

        return result;
    }

    public String lastKey() {
        return this.lastKey;
    }

    @Override
    public void run() {
        try {
            irConnection = new IRConnection();

            while (!BrainInaJarStartup.finished()) {
                String message = irConnection.keyPress();

                String translation = decode(message);

                if (!"".equals(translation)) {
                    lastKey = translation;
                    // print out the data received to the console and logfile
                    log.info("IR Signal [" + translation + "]");
                }

                Thread.sleep(1000);
            }
        } catch (InterruptedException ie) {
            log.error("Thread interrupted " + ie.getMessage());
        } catch (LIRCException le) {
            log.error("LIRCException " + le.getMessage());
        }
    }
}
