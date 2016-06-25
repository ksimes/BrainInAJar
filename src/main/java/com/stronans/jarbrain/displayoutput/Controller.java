package com.stronans.jarbrain.displayoutput;

import com.pi4j.component.lcd.LCDTextAlignment;

/**
 * Created by S.King on 19/02/2016.
 */
public interface Controller {
    void clear();

    void home();

    void write(int row, String text);

    void write(String text);

    void writeJustified(int row, String text, LCDTextAlignment align);

    void shutdown();
}
