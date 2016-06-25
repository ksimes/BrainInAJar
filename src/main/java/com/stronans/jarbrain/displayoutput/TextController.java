package com.stronans.jarbrain.displayoutput;

import com.pi4j.component.lcd.LCDTextAlignment;
import org.apache.log4j.Logger;

/**
 * Used for testing purposes
 *
 * Created by S.King on 19/02/2016.
 */
public class TextController implements Controller {
    /**
     * The <code>Logger</code> to be used.
     */
    private static Logger logger = Logger.getLogger(TextController.class);

    @Override
    public void clear() {
        logger.debug("lcd clear");
    }

    @Override
    public void home() {
        logger.debug("lcd setCursorHome");
    }

    @Override
    public void write(int row, String text) {
        logger.debug("Output: [" + text + "] on row " + row);
    }

    @Override
    public void write(String text) {
        logger.debug("Output: [" + text + "]");
    }

    @Override
    public void writeJustified(int row, String text, LCDTextAlignment align) {
        // write line to LCD
        logger.debug("Output: [" + text + "] on row " + row);
    }

    @Override
    public void shutdown() {
        logger.debug("lcd shutdown");
    }
}
