package com.stronans.jarbrain.displayoutput;

/*
 * **********************************************************************
 * FILENAME      :  LcdController.java
 * **********************************************************************
 *
 * Pinout Connections
 *
 * Pin No Symbol Level Description
 *   1 VSS 0V Ground
 *   2 VDD 5V Supply Voltage for logic
 *   3 VO (Variable) Operating voltage for LCD
 *   4 RS H/L H: DATA, L: Instruction code
 *   5 R/W H/L H: Read(MPU?Module) L: Write(MPU?Module)
 *   6 E H,H->L Chip enable signal
 *   7 DB0 H/L Data bus line
 *   8 DB1 H/L Data bus line
 *   9 DB2 H/L Data bus line
 *   10 DB3 H/L Data bus line
 *   11 DB4 H/L Data bus line
 *   12 DB5 H/L Data bus line
 *   13 DB6 H/L Data bus line
 *   14 DB7 H/L Data bus line
 *   15 A 5V LED +
 *   16 K 0V LED-
 */

import com.pi4j.component.lcd.LCDTextAlignment;
import com.pi4j.component.lcd.impl.GpioLcdDisplay;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;
import org.apache.log4j.Logger;

import java.io.IOException;

public class LCDController implements Controller {
    /**
     * The <code>Logger</code> to be used.
     */
    private static final Logger logger = Logger.getLogger(LCDController.class);

    public final static int LCD_ROWS = 4;
    public final static int LCD_COLUMNS = 20;

    private final GpioController gpio;
    private final GpioLcdDisplay lcd;

    public LCDController() throws InterruptedException, IOException {
        this(LCD_ROWS, LCD_COLUMNS);
    }

    public LCDController(int rows, int columns) throws InterruptedException, IOException {
        // create gpio controller
        gpio = GpioFactory.getInstance();
        // initialize LCD
        lcd = new GpioLcdDisplay(rows,  // number of row supported by LCD
                columns,       // number of columns supported by LCD
                RaspiPin.GPIO_11,  // LCD RS pin
                RaspiPin.GPIO_10,  // LCD strobe pin
                RaspiPin.GPIO_06,  // LCD data bit 1
                RaspiPin.GPIO_05,  // LCD data bit 2
                RaspiPin.GPIO_04,  // LCD data bit 3
                RaspiPin.GPIO_01); // LCD data bit 4
    }

    @Override
    public void clear() {
        // clear LCD
        logger.debug("lcd clear");
        lcd.clear();
    }

    @Override
    public void home() {
        // Home to top left corner
        logger.debug("lcd setCursorHome");
        lcd.setCursorHome();
    }

    @Override
    public void write(int row, String text) {
        // write line to LCD
        logger.info("Output: [" + text + "] on row " + row);
        lcd.write(row, text);
    }

    @Override
    public void write(String text) {
        // write ALL TEXT to LCD
        logger.info("Output: [" + text + "]");
        lcd.write(text);
    }

    @Override
    public void writeJustified(int row, String text, LCDTextAlignment align)
    {
        // write line to LCD
        logger.debug("Output: [" + text + "] on row " + row);
        lcd.write(row, text, align);
    }

    @Override
    public void shutdown()
    {
        logger.debug("lcd shutdown");
        // stop all GPIO activity/threads by shutting down the GPIO controller
        // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        gpio.shutdown();   // <--- implement this method call if you wish to terminate the Pi4J GPIO controller
    }
}