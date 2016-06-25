package com.stronans.jarbrain;

import com.stronans.jarbrain.displayoutput.DisplayComment;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * Test Driven Development
 * Created by S.King on 13/12/2015.
 */
public class TestDisplayComment {
    /**
     * The <code>Logger</code> to be used.
     */
    private static Logger log = Logger.getLogger(DisplayComment.class);

    @Test
    public void multiplicationOfZeroIntegersShouldReturnZero() {

        try {

            // MyClass is tested
//            DisplayComment tester = new DisplayComment();

            // assert statements
//            assertEquals("10 x 0 must be 0", 0, tester.multiply(10, 0));
//            assertEquals("0 x 10 must be 0", 0, tester.multiply(0, 10));
//            assertEquals("0 x 0 must be 0", 0, tester.multiply(0, 0));
        }
        catch( Exception e)
        {
            log.error("" + e.getMessage());
        }
    }

}
