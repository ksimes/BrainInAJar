package com.stronans.jarbrain.checkshutdown;

import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by S.King on 27/05/2016.
 */
public class ShutdownCheckTest {
    /**
     * The <code>Logger</code> to be used.
     */
    private final static Logger log = Logger.getLogger(ShutdownCheck.class);

    private ShutdownCheck shutDownCheck;

    @Test
    public void firstTest() {
        shutDownCheck = new ShutdownCheck();

        String test = shutDownCheck.decode("0000000000ff30cf 01 KEY_SUSPEND test1.conf");

        assertEquals(test, "KEY_SUSPEND");
    }

}